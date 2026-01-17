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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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

        // === ANIMATED PAGE INDICATORS ===
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.indices.forEach { index ->
                val isSelected = index == currentPage

                // Animasi untuk ukuran dan warna
                val width by animateDpAsState(
                    targetValue = if (isSelected) 32.dp else 8.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "width"
                )

                val color by animateColorAsState(
                    targetValue = if (isSelected) BrightBlue else MediumGray,
                    animationSpec = tween(300),
                    label = "color"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width, 8.dp)
                        .background(color, shape = CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // === ANIMATED CONTENT WITH CROSSFADE ===
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width } + fadeIn(
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut(
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )
                    )
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn(
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut(
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )
                    )
                }.using(SizeTransform(clip = false))
            },
            label = "content"
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        Spacer(modifier = Modifier.weight(1f))

        // === ANIMATED NAVIGATION BUTTONS ===
        AnimatedContent(
            targetState = currentPage < pages.size - 1,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith
                        fadeOut(animationSpec = tween(400))
            },
            label = "buttons"
        ) { isNotLastPage ->
            if (isNotLastPage) {
                ClipVaultButton(
                    text = "LANJUT",
                    gradient = true,
                    onClick = { currentPage++ }
                )
            } else {
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
    // === MULTIPLE ANIMATION LAYERS ===
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    // Floating animation
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Pulse animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Glow effect animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // === MULTI-LAYERED ANIMATED ICON ===
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale)
                    .alpha(glowAlpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BrightBlue.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Middle ring with rotation
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .rotate(rotation * 2)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricCyan.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Main icon container with all animations
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(y = offsetY.dp)
                    .rotate(rotation)
                    .scale(scale)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                BrightBlue.copy(alpha = 0.15f),
                                ElectricCyan.copy(alpha = 0.15f)
                            )
                        ),
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

            // Inner pulse
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(1f + (scale - 1f) * 0.5f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BrightBlue.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // === ANIMATED TEXT ===
        Text(
            text = data.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DeepPurple,
            textAlign = TextAlign.Center,
            modifier = Modifier.animateContentSize()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = data.desc,
            fontSize = 16.sp,
            color = DarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize()
        )
    }
}

data class OnboardingData(
    val title: String,
    val desc: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)