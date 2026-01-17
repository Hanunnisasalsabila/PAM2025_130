package com.clipvault.clipvault.ui.screens

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AuthResponse
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import com.clipvault.clipvault.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    assetId: Int,
    videoUrl: String,
    title: String,
    description: String,
    username: String,
    fileSize: Long,
    userPhoto: String,
    tags: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    // State untuk Dialog Download
    var showDownloadConfirm by remember { mutableStateOf(false) }

    // State untuk Fullscreen
    var isFullscreen by remember { mutableStateOf(false) }

    // 1. IP Server (Pastikan sama dengan IP Laptop kamu)
    val baseUrl = "http://10.78.80.195:3000/"

    // === PERBAIKAN URL VIDEO (Encode Spasi) ===
    val cleanVideoPath = videoUrl.replace("\\", "/")

    // Encode setiap bagian path biar spasi jadi %20 (ExoPlayer wajib ini!)
    val encodedPath = cleanVideoPath.split("/").joinToString("/") { Uri.encode(it) }

    val fullVideoUrl = if (encodedPath.startsWith("http")) encodedPath else baseUrl + encodedPath

    // Logic Foto
    val fullPhotoUrl = if (userPhoto == "no_photo" || userPhoto.isEmpty()) "" else baseUrl + userPhoto.replace("\\", "/")

    // Setup ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            try {
                setMediaItem(MediaItem.fromUri(Uri.parse(fullVideoUrl)))
                prepare()
                playWhenReady = true
            } catch (e: Exception) {}
        }
    }

    // Handle Tombol Back HP: Kalau lagi fullscreen -> keluar fullscreen, kalau enggak -> back normal
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
        // Kembalikan orientasi ke Portrait saat keluar fullscreen
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            // Reset orientasi saat keluar dari screen
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Scaffold(
        // Kalau Fullscreen, sembunyikan TopBar
        topBar = {
            if (!isFullscreen) {
                TopAppBar(
                    title = { Text("Detail Video") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        }
    ) { innerPadding ->
        // Logic Layout: Kalau Fullscreen, Box Video menuhi layar. Kalau enggak, pakai Column biasa.
        if (isFullscreen) {
            // === LAYOUT FULLSCREEN ===
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = {
                        StyledPlayerView(it).apply {
                            player = exoPlayer
                            useController = true
                            // Tombol Fullscreen ditekan -> Keluar Fullscreen
                            setFullscreenButtonClickListener {
                                isFullscreen = false
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // === 1. VIDEO PLAYER ===
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = {
                            StyledPlayerView(it).apply {
                                player = exoPlayer
                                useController = true
                                // Tombol Fullscreen ditekan -> Masuk Fullscreen
                                setFullscreenButtonClickListener {
                                    isFullscreen = true
                                    // Opsional: Paksa Landscape biar kayak YouTube
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                }
                            }
                        },
                        modifier = Modifier.matchParentSize()
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {

                    // === 2. JUDUL & DOWNLOAD ===
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        // TOMBOL DOWNLOAD (Munculin Dialog)
                        IconButton(
                            onClick = { showDownloadConfirm = true }, // <--- Ganti jadi true
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF00897B), CircleShape)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // === 3. SIZE & TAGS INFO ===
                    Text(
                        text = "Ukuran: ${formatFileSize(fileSize)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // === 2. TAMBAHKAN TAMPILAN TAGS DISINI (Sebelum/Sesudah Size) ===
                    if (tags.isNotEmpty() && tags != "null") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tags: $tags",
                            fontSize = 12.sp,
                            color = Color(0xFF00897B), // Warna hijau biar cantik
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // === 4. PROFIL UPLOADER ===
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = fullPhotoUrl,
                            contentDescription = "User Photo",
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "@$username",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Uploader",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // === 5. DESKRIPSI ===
                    Text("Deskripsi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if(description.trim().isEmpty()) "Tidak ada deskripsi." else description,
                        color = Color(0xFF444444),
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
    // === DIALOG KONFIRMASI DOWNLOAD ===
    if (showDownloadConfirm) {
        AlertDialog(
            onDismissRequest = { showDownloadConfirm = false },
            title = { Text("Unduh Video?") },
            text = { Text("Video akan disimpan ke folder Movies di HP kamu. Lanjutkan?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDownloadConfirm = false
                        // Panggil Fungsi Download Asli
                        downloadVideoWithTracking(context, fullVideoUrl, title, assetId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text("Ya, Unduh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// === FUNGSI DOWNLOAD ===
fun downloadVideoWithTracking(context: Context, url: String, title: String, assetId: Int) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("Mengunduh aset ClipVault...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "ClipVault_$title.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "Download dimulai! 📥", Toast.LENGTH_SHORT).show()

        // 2. LAPOR KE SERVER (BAGIAN INI YANG DIPERBAIKI)
        // Ambil User ID dari SessionManager yang tadi kita buat
        val myUserId = SessionManager.getUserId()

        // Bungkus User ID ke dalam Map JSON
        val body = mapOf("user_id" to myUserId)

        RetrofitClient.instance.trackDownload(assetId, body).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    // Berhasil lapor server (Optional: Bisa log di Logcat)
                    android.util.Log.d("DownloadTracker", "Server updated successfully")
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                // Gagal lapor server (Mungkin koneksi), tapi file fisik tetep kedownload
                android.util.Log.e("DownloadTracker", "Failed to update server: ${t.message}")
            }
        })

    } catch (e: Exception) {
        Toast.makeText(context, "Gagal download: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// Helper Format Size
fun formatFileSize(size: Long): String {
    if (size <= 0) return "Unknown"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}