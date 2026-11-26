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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF101B31), Night, Color(0xFF090B14))))
            .padding(horizontal = 22.dp, vertical = 28.dp)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("HiVPN", fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, color = Color.White)
                Text("PRIVATE NETWORK", color = Blue, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(48.dp))
            Box(Modifier.size(236.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(224.dp).alpha(glow).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Blue.copy(alpha = 0.42f), Red.copy(alpha = 0.10f), Color.Transparent)))
                )
                Box(
                    Modifier.size(174.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF142B4D), Color(0xFF090E1A), Color(0xFF241321))))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hi", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                    Text("VPN", color = Blue, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 5.sp)
                }
            }

            Spacer(Modifier.height(26.dp))
            Text("NOT CONNECTED", color = Red, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(9.dp))
            Text("Your connection is not protected by HiVPN.", color = Color(0xFFB7C3D7), fontSize = 13.sp)

            Spacer(Modifier.height(32.dp))
            Surface(Modifier.fillMaxWidth(), color = Panel.copy(alpha = 0.94f), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("SERVER NETWORK", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No gateways deployed yet", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Real country locations will appear here after our first WireGuard server is deployed and verified.",
                        color = Color(0xFF9EABC0), fontSize = 12.sp, lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Surface(Modifier.fillMaxWidth(), color = Color(0xFF171D2A), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("VPN ENGINE", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(7.dp))
                    Text("Integration pending", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Connection controls will be enabled only after a real tunnel and gateway pass testing.",
                        color = Color(0xFF9EABC0), fontSize = 12.sp, lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Text("HI VPN  •  BUILD 0.1.0  •  DEVELOPMENT", color = Color(0xFF66758D), fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}
