package com.hivpn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val Night = Color(0xFF050812)
private val Panel = Color(0xFF101827)
private val Blue = Color(0xFF2589FF)
private val Red = Color(0xFFFF365D)
private val Gold = Color(0xFFFFC857)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { HiVpnHome() } }
    }
}

@Composable
private fun HiVpnHome() {
    val transition = rememberInfiniteTransition(label = "hivpn-glow")
    val glow by transition.animateFloat(
        initialValue = 0.24f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "glow-alpha"
    )
    val scope = rememberCoroutineScope()
    var servers by remember { mutableStateOf<List<VpnGateServer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastUpdated by remember { mutableStateOf<String?>(null) }

    fun refreshServers() {
        if (isLoading) return
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = withContext(Dispatchers.IO) { VpnGateRepository().fetchServers() }
                servers = result
                lastUpdated = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())
                if (result.isEmpty()) errorMessage = "The API responded, but no valid OpenVPN profiles passed validation."
            } catch (error: Exception) {
                errorMessage = error.message?.take(180) ?: "Unable to load the VPN Gate server list."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshServers() }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF101B31), Night, Color(0xFF090B14))))
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("HiVPN", fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, color = Color.White)
                    Text("COMMUNITY NETWORK", color = Blue, fontSize = 9.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Box(Modifier.size(154.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(146.dp).alpha(glow).clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Blue.copy(alpha = 0.42f), Red.copy(alpha = 0.10f), Color.Transparent)))
                    )
                    Box(
                        Modifier.size(116.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF142B4D), Color(0xFF090E1A), Color(0xFF241321))))
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hi", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
                        Text("VPN", color = Blue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                    }
                }
            }
            item {
                Text("NOT CONNECTED", color = Red, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(5.dp))
                Text("Server discovery is ready. VPN connection is not enabled yet.", color = Color(0xFFB7C3D7), fontSize = 12.sp)
            }
            item {
                Surface(Modifier.fillMaxWidth(), color = Panel.copy(alpha = 0.96f), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("VPN GATE COMMUNITY RELAYS", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Button(onClick = { refreshServers() }, enabled = !isLoading) {
                                Text(if (isLoading) "Loading" else "Refresh")
                            }
                        }
                        Text(
                            if (isLoading && servers.isEmpty()) "Fetching live server list…" else "${servers.size} validated server profiles",
                            color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold
                        )
                        if (lastUpdated != null) Text("Last updated $lastUpdated", color = Color(0xFF9EABC0), fontSize = 11.sp)
                        if (isLoading) CircularProgressIndicator(color = Blue, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        if (errorMessage != null) Text(errorMessage!!, color = Color(0xFFFF9AAE), fontSize = 12.sp)
                        Text(
                            "Community relays are operated by independent volunteers. Their availability and logging practices vary. HiVPN does not own these servers.",
                            color = Color(0xFF9EABC0), fontSize = 11.sp, lineHeight = 16.sp
                        )
                    }
                }
            }
            items(servers, key = { it.hostName + it.ipAddress }) { server ->
                ServerCard(server)
            }
            item {
                Text(
                    "M1 SERVER DISCOVERY • OPENVPN ENGINE NOT YET INTEGRATED",
                    color = Color(0xFF66758D), fontSize = 9.sp, letterSpacing = 0.6.sp
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ServerCard(server: VpnGateServer) {
    Surface(Modifier.fillMaxWidth(), color = Color(0xFF141D2C), shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${server.countryName} (${server.countryCode})", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text("${server.pingMs} ms", color = Blue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text(server.hostName, color = Color(0xFFB7C3D7), fontSize = 11.sp)
            Text(
                String.format(Locale.getDefault(), "%.1f Mbps  •  %d active sessions", server.speedMbps, server.activeSessions),
                color = Color(0xFF9EABC0), fontSize = 11.sp
            )
            Text("Volunteer relay · VPN Gate · Logging: ${server.loggingPolicy}", color = Gold, fontSize = 10.sp)
        }
    }
}
