package com.hivpn.app

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64

data class VpnGateServer(
    val hostName: String,
    val ipAddress: String,
    val countryName: String,
    val countryCode: String,
    val score: Long,
    val pingMs: Long,
    val speedBitsPerSecond: Long,
    val activeSessions: Int,
    val uptimeSeconds: Long,
    val loggingPolicy: String,
    /** Kept in memory only. Never log or persist volunteer credentials/configuration. */
    val openVpnConfig: String
) {
    val speedMbps: Double get() = speedBitsPerSecond / 1_000_000.0
}

class VpnGateRepository(
    private val apiUrl: String = API_URL,
    private val connectTimeoutMs: Int = 12_000,
    private val readTimeoutMs: Int = 20_000
) {
    fun fetchServers(): List<VpnGateServer> {
        val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = true
            setRequestProperty("Accept", "text/plain,text/csv")
            setRequestProperty("User-Agent", "HiVPN-Android/0.1")
        }

        try {
            val status = connection.responseCode
            if (status !in 200..299) {
                throw IllegalStateException("VPN Gate API returned HTTP $status")
            }
            val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readTextLimited(MAX_RESPONSE_CHARS) }
            return parseCsv(body)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val API_URL = "https://www.vpngate.net/api/iphone/"
        private const val MAX_RESPONSE_CHARS = 20 * 1024 * 1024

        internal fun parseCsv(csvText: String): List<VpnGateServer> {
            require(csvText.length <= MAX_RESPONSE_CHARS) { "VPN Gate response is unexpectedly large" }
            val records = CsvParser.parse(csvText)
            val headerRowIndex = records.indexOfFirst { row ->
                row.firstOrNull()?.trim()?.removePrefix("#") == "HostName"
            }
            require(headerRowIndex >= 0) { "VPN Gate CSV header was not found" }

            val headers = records[headerRowIndex].mapIndexed { index, value ->
                val normalized = value.trim().removePrefix("#")
                if (index == 0) normalized else normalized
            }
            val indexes = headers.withIndex().associate { it.value to it.index }
            val required = listOf(
                "HostName", "IP", "Score", "Ping", "Speed", "CountryLong",
                "CountryShort", "NumVpnSessions", "Uptime", "LogType",
                "OpenVPN_ConfigData_Base64"
            )
            require(required.all { indexes.containsKey(it) }) { "VPN Gate CSV is missing required columns" }

            fun field(row: List<String>, name: String): String =
                row.getOrNull(indexes.getValue(name))?.trim().orEmpty()

            return records.drop(headerRowIndex + 1).mapNotNull { row ->
                if (row.size < headers.size) return@mapNotNull null

                val host = field(row, "HostName")
                val ip = field(row, "IP")
                val country = field(row, "CountryLong")
                val countryCode = field(row, "CountryShort").uppercase()
                val configEncoded = field(row, "OpenVPN_ConfigData_Base64")
                if (!isSafeHost(host) || !isIpv4(ip) || country.isBlank() ||
                    !countryCode.matches(Regex("[A-Z]{2}")) || configEncoded.isBlank()
                ) return@mapNotNull null

                val config = decodeAndValidateConfig(configEncoded) ?: return@mapNotNull null
                val score = field(row, "Score").toLongOrNull() ?: return@mapNotNull null
                val ping = field(row, "Ping").toLongOrNull() ?: return@mapNotNull null
                val speed = field(row, "Speed").toLongOrNull() ?: return@mapNotNull null
                val sessions = field(row, "NumVpnSessions").toIntOrNull() ?: return@mapNotNull null
                val uptime = field(row, "Uptime").toLongOrNull() ?: return@mapNotNull null

                if (score < 0 || ping < 0 || speed < 0 || sessions < 0 || uptime < 0) {
                    return@mapNotNull null
                }

                VpnGateServer(
                    hostName = host,
                    ipAddress = ip,
                    countryName = country,
                    countryCode = countryCode,
                    score = score,
                    pingMs = ping,
                    speedBitsPerSecond = speed,
                    activeSessions = sessions,
                    uptimeSeconds = uptime,
                    loggingPolicy = field(row, "LogType"),
                    openVpnConfig = config
                )
            }.distinctBy { it.hostName to it.ipAddress }
                .sortedWith(compareBy<VpnGateServer> { it.pingMs }.thenByDescending { it.speedBitsPerSecond })
        }

        private fun decodeAndValidateConfig(encoded: String): String? {
            return try {
                val decoded = Base64.getMimeDecoder().decode(encoded)
                if (decoded.size !in 128..262_144) return null
                val config = String(decoded, StandardCharsets.UTF_8)
                if (!config.contains(Regex("(?im)^\\s*client\\s*$"))) return null
                if (!config.contains(Regex("(?im)^\\s*dev\\s+tun\\s*$"))) return null
                if (!config.contains(Regex("(?im)^\\s*remote\\s+\\S+\\s+[1-9][0-9]{0,4}(?:\\s+\\S+)?\\s*$"))) return null
                if (!config.contains(Regex("(?im)^\\s*<ca>\\s*$")) ||
                    !config.contains(Regex("(?im)^\\s*</ca>\\s*$"))
                ) return null

                // The public profile is untrusted input. Reject directives that can launch
                // scripts, load plugins, include other files, or alter process management.
                val forbidden = Regex(
                    "(?im)^\\s*(up|down|route-up|route-pre-down|ipchange|tls-verify|plugin|config|management|cd|daemon|script-security|user|group)\\b"
                )
                if (config.contains(forbidden)) return null
                config
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        private fun isSafeHost(host: String): Boolean =
            host.length in 1..253 && host.matches(Regex("[A-Za-z0-9.-]+")) &&
                !host.startsWith(".") && !host.endsWith(".")

        private fun isIpv4(value: String): Boolean {
            val parts = value.split(".")
            return parts.size == 4 && parts.all { part ->
                part.isNotEmpty() && part.length <= 3 && part.all(Char::isDigit) &&
                    part.toIntOrNull()?.let { it in 0..255 } == true
            }
        }
    }
}

private fun BufferedReader.readTextLimited(maxChars: Int): String {
    val result = StringBuilder()
    val buffer = CharArray(8192)
    while (true) {
        val count = read(buffer)
        if (count < 0) break
        require(result.length + count <= maxChars) { "VPN Gate response exceeds the size limit" }
        result.append(buffer, 0, count)
    }
    return result.toString()
}

/** Small RFC-4180-style parser supporting quoted commas, escaped quotes and multiline fields. */
internal object CsvParser {
    fun parse(input: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var index = 0

        while (index < input.length) {
            val char = input[index]
            when {
                quoted && char == '"' && index + 1 < input.length && input[index + 1] == '"' -> {
                    field.append('"')
                    index++
                }
                char == '"' -> quoted = !quoted
                char == ',' && !quoted -> {
                    row.add(field.toString())
                    field.setLength(0)
                }
                (char == '\n' || char == '\r') && !quoted -> {
                    row.add(field.toString())
                    field.setLength(0)
                    if (row.any { it.isNotBlank() }) rows.add(row)
                    row = mutableListOf()
                    if (char == '\r' && index + 1 < input.length && input[index + 1] == '\n') index++
                }
                else -> field.append(char)
            }
            index++
        }
        require(!quoted) { "Malformed CSV: unterminated quoted field" }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            if (row.any { it.isNotBlank() }) rows.add(row)
        }
        return rows
    }
}
