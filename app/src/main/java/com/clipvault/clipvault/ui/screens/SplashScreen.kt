package com.clipvault.clipvault.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.clipvault.clipvault.R
import com.clipvault.clipvault.ui.components.ClipVaultGradientBackground
import com.clipvault.clipvault.ui.theme.White

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Animasi Pulsing untuk Logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Animasi Fade In untuk Text
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Timer 2.5 Detik
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    ClipVaultGradientBackground {
        // GANTI Column JADI Box (Supaya bisa tumpuk posisi bebas)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // === 1. GROUP LOGO & JUDUL (Tepat di Tengah Layar) ===
            Column(
                modifier = Modifier.align(Alignment.Center), // KUNCI: Align Center
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ClipVault",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = White,
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(alpha)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your Gaming Assets",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.alpha(alpha)
                )
            }

            // === 2. COPYRIGHT (Tepat di Bawah Layar) ===
            Text(
                text = "v1.0 by Hanun Nisa Salsabila",
                modifier = Modifier
                    .align(Alignment.BottomCenter) // KUNCI: Align Bottom
                    .padding(bottom = 48.dp),      // Kasih jarak dikit dari bawah HP
                color = White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}