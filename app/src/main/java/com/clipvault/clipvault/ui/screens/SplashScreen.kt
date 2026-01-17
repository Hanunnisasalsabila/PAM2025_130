package com.clipvault.clipvault.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.clipvault.clipvault.R // Pastikan import R resource kamu

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Timer 2.5 Detik
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Background Putih Bersih
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Ganti R.drawable.ic_launcher_foreground dengan logo kamu sendiri kalau ada
            // Misal: painterResource(id = R.drawable.logo_clipvault)
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ClipVault",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B) // Hijau Tosca Khas
            )
        }

        // Copyright di bawah
        Text(
            text = "v1.0 by Hanun",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            color = Color.Gray
        )
    }
}