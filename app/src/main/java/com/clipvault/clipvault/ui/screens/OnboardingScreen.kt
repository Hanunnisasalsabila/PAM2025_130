package com.clipvault.clipvault.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipvault.clipvault.ui.components.ClipVaultButton
import com.clipvault.clipvault.ui.components.ClipVaultOutlinedButton
import com.clipvault.clipvault.ui.theme.*

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }

    // Animasi untuk perubahan halaman
    val animatedPage by animateIntAsState(
        targetValue = currentPage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "page"
    )

    val pages = listOf(
        OnboardingData(
            "Cari & Filter Aset",
            "Gunakan kolom pencarian untuk menemukan klip spesifik, atau klik tombol Kategori (WuWa, HSR) untuk menyaring hasil.",
            Icons.Default.Search
        ),
        OnboardingData(
            "Upload Klip Kamu",
            "Tekan tombol (+) di halaman utama, pilih video dari galeri, dan tentukan kategori untuk membagikan karyamu.",
            Icons.Default.AddCircle
        ),
        OnboardingData(
            "Download ke Galeri",
            "Buka preview video untuk menonton, lalu tekan tombol 'Download' untuk menyimpan klip HD langsung ke HP-mu.",
            Icons.Default.Download
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // === PAGE INDICATORS (ANIMATED) ===
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.indices.forEach { index ->
                val isSelected = index == currentPage
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "indicator_scale"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 32.dp else 8.dp, 8.dp)
                        .scale(scale)
                        .background(
                            color = if (isSelected) BrightBlue else MediumGray,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // === ANIMATED CONTENT ===
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    // Slide dari kanan ke kiri
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    // Slide dari kiri ke kanan
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "content"
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        Spacer(modifier = Modifier.weight(1f))

        // === TOMBOL NAVIGASI ===
        AnimatedContent(
            targetState = currentPage < pages.size - 1,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "buttons"
        ) { isNotLastPage ->
            if (isNotLastPage) {
                // Tombol LANJUT
                ClipVaultButton(
                    text = "LANJUT",
                    gradient = true,
                    onClick = { currentPage++ }
                )
            } else {
                // Halaman Terakhir - 2 Tombol
                Column {
                    ClipVaultButton(
                        text = "MASUK (LOGIN)",
                        gradient = true,
                        onClick = onNavigateToLogin
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ClipVaultOutlinedButton(
                        text = "DAFTAR AKUN BARU",
                        onClick = onNavigateToRegister
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun OnboardingPageContent(data: OnboardingData) {
    // Animasi Floating untuk Icon
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // === ANIMATED ICON ===
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(y = offsetY.dp)
                .background(
                    color = BrightBlue.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = BrightBlue
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // === JUDUL ===
        Text(
            text = data.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DeepPurple,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // === DESKRIPSI ===
        Text(
            text = data.desc,
            fontSize = 16.sp,
            color = DarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

data class OnboardingData(
    val title: String,
    val desc: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)