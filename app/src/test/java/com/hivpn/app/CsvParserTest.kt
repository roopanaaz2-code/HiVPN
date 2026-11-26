package com.hivpn.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvParserTest {
    @Test
    fun parsesQuotedCommasAndEscapedQuotes() {
        val rows = CsvParser.parse("a,b,c\n1,\"hello, world\",\"say \"\"hi\"\"\"\n")
        assertEquals("hello, world", rows[1][1])
        assertEquals("say \"hi\"", rows[1][2])
    }

    @Test
    fun parsesMultilineQuotedFieldsAndCrLf() {
        val rows = CsvParser.parse("a,b\r\n1,\"line one\r\nline two\"\r\n")
        assertEquals("line one\r\nline two", rows[1][1])
    }

    @Test
    fun identifiesVpnGateHeaderAndSkipsInvalidRecords() {
        val config = java.util.Base64.getEncoder().encodeToString(
            """
            client
            dev tun
            proto tcp
            remote relay.example.net 443
            <ca>
            -----BEGIN CERTIFICATE-----
            dGVzdA==
            -----END CERTIFICATE-----
            </ca>
            """.trimIndent().toByteArray()
        )
        val csv = """*vpn_servers
#HostName,IP,Score,Ping,Speed,CountryLong,CountryShort,NumVpnSessions,Uptime,TotalUsers,TotalTraffic,LogType,Operator,Message,OpenVPN_ConfigData_Base64
relay-1,192.0.2.10,100,35,25000000,Japan,JP,2,3600,10,1000,2weeks,Volunteer,"test, relay",$config
bad-host,not-an-ip,10,10,10,Japan,JP,1,100,1,1,2weeks,Volunteer,invalid,$config
"""
        val servers = VpnGateRepository.parseCsv(csv)
        assertEquals(1, servers.size)
        assertEquals("relay-1", servers.single().hostName)
        assertEquals("JP", servers.single().countryCode)
        assertTrue(servers.single().openVpnConfig.contains("remote relay.example.net 443"))
    }
}
