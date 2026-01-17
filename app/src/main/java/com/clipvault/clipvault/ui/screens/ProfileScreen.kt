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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex // Import zIndex buat maksa di layer paling atas
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AssetItem
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.ProfileResponse
import com.clipvault.clipvault.data.model.UserData
import com.clipvault.clipvault.ui.theme.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ProfileScreen(
    userId: Int,
    isMyProfile: Boolean = false,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onLogout: () -> Unit,
    onVideoClick: (Int, String, String, String, String, Long, String, String) -> Unit
) {
    val context = LocalContext.current
    var userData by remember { mutableStateOf<UserData?>(null) }
    var userAssets by remember { mutableStateOf<List<AssetItem>>(emptyList()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var assetToDeleteId by remember { mutableStateOf<Int?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val serverIp = "http://10.78.80.195:3000/"

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .crossfade(true)
            .build()
    }

    val totalDownloads = userAssets.sumOf { it.download_count }

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
            CircularProgressIndicator(color = BrightBlue)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(OffWhite)) {

            // === HEADER PROFILE ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrightBlue, DeepPurple)
                        )
                    )
            ) {
                // 1. KONTEN PROFILE (Ditulis duluan biar jadi layer bawah)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 60.dp, bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userData?.photo.isNullOrEmpty()) {
                            val cleanPath = userData!!.photo!!.replace("\\", "/")
                            val fullUrl = serverIp + cleanPath

                            AsyncImage(
                                model = fullUrl,
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = DeepPurple,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userData?.full_name ?: "User",
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${userData?.username}",
                        color = White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = userData?.email ?: "-",
                        color = White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    if (!userData?.bio.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userData?.bio ?: "",
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!userData?.location.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 ${userData?.location}",
                            color = White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.height(35.dp),
                            border = BorderStroke(1.dp, White),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = White)
                        ) {
                            Text("✏️ Edit Profil", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.height(35.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("🚪 Keluar", fontSize = 12.sp, color = White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                        StatItem(count = userAssets.size.toString(), label = "Upload")
                        StatItem(count = totalDownloads.toString(), label = "Downloads")
                    }
                }

                // 2. TOMBOL KEMBALI (DIPINDAH KE SINI: LAYER PALING ATAS)
                // Ditambah modifier .zIndex(10f) biar maksa paling depan
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .statusBarsPadding()
                        .zIndex(10f) // KUNCI UTAMA: Layer Paling Atas
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // === LIST KOLEKSI SAYA ===
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Koleksi Saya",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepPurple
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
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val cleanPath = asset.file_path.replace("\\", "/")
                                val fullUrl = serverIp + cleanPath

                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(fullUrl)
                                        .videoFrameMillis(2000)
                                        .build(),
                                    imageLoader = imageLoader,
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
                                                userData?.username ?: "Unknown",
                                                asset.file_size ?: 0L,
                                                userData?.photo ?: "",
                                                asset.tags ?: ""
                                            )
                                        }
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = asset.title,
                                        color = White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        assetToDeleteId = asset.asset_id
                                        showDeleteDialog = true
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(White.copy(alpha = 0.7f), CircleShape),
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

    // ... (Dialog logic tetap sama di bawah) ...
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
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Keluar Akun?") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi ClipVault?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
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
        Text(text = count, color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}