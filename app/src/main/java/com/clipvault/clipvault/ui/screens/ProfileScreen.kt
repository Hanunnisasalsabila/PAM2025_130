package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AssetItem
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.ProfileResponse
import com.clipvault.clipvault.data.model.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.material.icons.filled.ExitToApp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ProfileScreen(
    userId: Int,
    isMyProfile: Boolean = false,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onLogout: () -> Unit,
    // Update parameternya:
    onVideoClick: (Int, String, String, String, String, Long, String, String) -> Unit
) {
    val context = LocalContext.current
    var userData by remember { mutableStateOf<UserData?>(null) }
    var userAssets by remember { mutableStateOf<List<AssetItem>>(emptyList()) }

    // State untuk Dialog Konfirmasi Hapus Video
    var showDeleteDialog by remember { mutableStateOf(false) }
    var assetToDeleteId by remember { mutableStateOf<Int?>(null) }

    // State untuk Dialog Konfirmasi Logout (BARU!)
    var showLogoutDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }

    // IP Server (Pastikan sama dengan RetrofitClient)
    val serverIp = "http://10.78.80.195:3000/"

    // === 1. KONFIGURASI COIL UNTUK VIDEO (WAJIB ADA BIAR THUMBNAIL MUNCUL) ===
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }

    // Hitung total download dari list aset
    // Kita jumlahkan semua 'download_count' dari setiap video
    val totalDownloads = userAssets.sumOf { it.download_count }

    // Fetch Data Profil
    LaunchedEffect(userId) {
        RetrofitClient.instance.getUserProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                isLoading = false
                if (response.isSuccessful && response.body()?.error == false) {
                    userData = response.body()?.user
                    userAssets = response.body()?.assets ?: emptyList()
                } else {
                    Toast.makeText(context, "Gagal ambil profil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. HEADER PROFIL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        // Cek apakah user punya foto di database?
                        if (!userData?.photo.isNullOrEmpty()) {
                            // FIX: Ganti Backslash Windows (\) jadi Slash (/)
                            val cleanPath = userData!!.photo!!.replace("\\", "/")
                            val fullUrl = serverIp + cleanPath

                            AsyncImage(
                                model = fullUrl,
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop // Biar gambarnya penuh di lingkaran
                            )
                        } else {
                            // KALAU TIDAK ADA (NULL): Tampilkan Icon Placeholder lama
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF764BA2),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nama & Info
                    Text(
                        text = userData?.full_name ?: "User",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${userData?.username}",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = userData?.email ?: "-",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    // BIO & LOKASI
                    if (!userData?.bio.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userData?.bio ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!userData?.location.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 ${userData?.location}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ROW TOMBOL
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tombol Edit
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.height(35.dp),
                            border = BorderStroke(1.dp, Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("✏️ Edit Profil", fontSize = 12.sp)
                        }

                        // Tombol Logout (UPDATE: Munculkan Dialog dulu)
                        Button(
                            onClick = { showLogoutDialog = true }, // <--- Ganti jadi ini
                            modifier = Modifier.height(35.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("🚪 Keluar", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Statistik
                    Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                        StatItem(count = userAssets.size.toString(), label = "Upload")

                        // GUNAKAN VARIABEL totalDownloads DISINI
                        StatItem(count = totalDownloads.toString(), label = "Downloads")
                    }
                }

                // Tombol Kembali
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.align(Alignment.TopStart).offset(x = -10.dp, y = -10.dp)
                ) {
                    Text("←", color = Color.White)
                }
            }

            // 2. KONTEN (GRID VIDEO)
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Koleksi Saya",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(userAssets) { asset ->
                        Card(
                            modifier = Modifier.height(160.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {

                                // 1. GAMBAR THUMBNAIL (GANTI TEXT KAMERA DENGAN INI)
                                val cleanPath = asset.file_path.replace("\\", "/")
                                val fullUrl = serverIp + cleanPath

                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(fullUrl)
                                        .videoFrameMillis(2000) // Ambil gambar di detik ke-2
                                        .build(),
                                    imageLoader = imageLoader, // Pakai loader yang tadi dibuat
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            onVideoClick(
                                                asset.asset_id,
                                                asset.file_path,
                                                asset.title,
                                                asset.description ?: "",
                                                userData?.username ?: "Unknown", // Ambil dari Profile Header
                                                asset.file_size ?: 0L,
                                                userData?.photo ?: "",            // Ambil dari Profile Header
                                                asset.tags ?: ""
                                            )
                                        }
                                )

                                // 2. JUDUL DI BAWAH (Biar kebaca jelas)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = asset.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }

                                // 3. TOMBOL SAMPAH (Tetap di pojok kanan atas)
                                IconButton(
                                    onClick = {
                                        assetToDeleteId = asset.asset_id
                                        showDeleteDialog = true
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    // Kasih background putih dikit biar ikon sampah kelihatan jelas
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color.White.copy(alpha = 0.7f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🗑️", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // === DIALOG KONFIRMASI HAPUS VIDEO ===
    if (showDeleteDialog && assetToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Video?") },
            text = { Text("Video ini akan dihapus permanen. Tindakan tidak bisa dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = assetToDeleteId!!
                        RetrofitClient.instance.deleteAsset(id).enqueue(object : Callback<AuthResponse> {
                            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                if (response.isSuccessful && response.body()?.error == false) {
                                    Toast.makeText(context, "Video berhasil dihapus!", Toast.LENGTH_SHORT).show()
                                    userAssets = userAssets.filter { it.asset_id != id }
                                } else {
                                    Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                                }
                                showDeleteDialog = false
                            }

                            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                Toast.makeText(context, "Error koneksi", Toast.LENGTH_SHORT).show()
                                showDeleteDialog = false
                            }
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // === DIALOG KONFIRMASI LOGOUT (BARU!) ===
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Keluar Akun?") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi ClipVault?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout() // Panggil fungsi logout asli di sini
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)) // Merah
                ) {
                    Text("Ya, Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}