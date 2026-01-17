package com.clipvault.clipvault.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,    // Callback buat tombol Masuk
    onNavigateToRegister: () -> Unit  // Callback buat tombol Daftar
) {
    // State halaman aktif (0, 1, 2)
    var currentPage by remember { mutableStateOf(0) }

    // Data Onboarding (Judul, Deskripsi, Icon)
    // Fokus: Menjelaskan CARA pakai fitur (Search, Upload, Download)
    val pages = listOf(
        OnboardingData(
            "Cari & Filter Aset",
            "Gunakan kolom pencarian untuk menemukan klip spesifik, atau klik tombol Kategori (WuWa, HSR) untuk menyaring hasil.",
            Icons.Default.Search // Icon Kaca Pembesar
        ),
        OnboardingData(
            "Upload Klip Kamu",
            "Tekan tombol (+) di halaman utama, pilih video dari galeri, dan tentukan kategori untuk membagikan karyamu.",
            Icons.Default.AddCircle // Icon Tambah (+)
        ),
        OnboardingData(
            "Download ke Galeri",
            "Buka preview video untuk menonton, lalu tekan tombol 'Download' untuk menyimpan klip HD langsung ke HP-mu.",
            Icons.Default.ArrowDropDown // Icon Download/Panah Bawah
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // ICON
        Icon(
            imageVector = pages[currentPage].icon,
            contentDescription = null,
            modifier = Modifier.size(160.dp), // Sedikit lebih besar
            tint = Color(0xFF00897B)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // JUDUL
        Text(
            text = pages[currentPage].title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // DESKRIPSI tutorial
        Text(
            text = pages[currentPage].desc,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 24.sp // Biar lebih enak dibaca kalau teksnya panjang
        )

        Spacer(modifier = Modifier.weight(1f))

        // === LOGIKA TOMBOL (Sesuai SRS Poin 3) ===
        if (currentPage < pages.size - 1) {
            // Kalo belum halaman terakhir, tampilkan tombol "LANJUT" biasa
            Button(
                onClick = { currentPage++ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "LANJUT")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        } else {
            // === HALAMAN TERAKHIR: PILIHAN MASUK ATAU DAFTAR ===
            // 1. Tombol MASUK (Primary Color)
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text(text = "MASUK (LOGIN)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Tombol DAFTAR (Outlined / Border Only)
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00897B))
            ) {
                Text(text = "DAFTAR AKUN BARU")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

data class OnboardingData(val title: String, val desc: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)