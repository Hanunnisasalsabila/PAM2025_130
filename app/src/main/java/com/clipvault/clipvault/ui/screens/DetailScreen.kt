package com.clipvault.clipvault.ui.screens

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.clipvault.clipvault.ui.theme.*
import com.clipvault.clipvault.utils.SessionManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

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

    var showDownloadConfirm by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var downloadButtonPressed by remember { mutableStateOf(false) }

    val baseUrl = "http://10.78.80.195:3000/"
    val cleanVideoPath = videoUrl.replace("\\", "/")
    val encodedPath = cleanVideoPath.split("/").joinToString("/") { Uri.encode(it) }
    val fullVideoUrl = if (encodedPath.startsWith("http")) encodedPath else baseUrl + encodedPath
    val fullPhotoUrl = if (userPhoto == "no_photo" || userPhoto.isEmpty()) ""
    else baseUrl + userPhoto.replace("\\", "/")

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            try {
                setMediaItem(MediaItem.fromUri(Uri.parse(fullVideoUrl)))
                prepare()
                playWhenReady = true
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val downloadButtonScale by animateFloatAsState(
        targetValue = if (downloadButtonPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "download_scale"
    )

    LaunchedEffect(downloadButtonPressed) {
        if (downloadButtonPressed) {
            delay(100)
            downloadButtonPressed = false
        }
    }

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                TopAppBar(
                    title = { Text("Detail Video", color = White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrightBlue)
                )
            }
        }
    ) { innerPadding ->
        if (isFullscreen) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                AndroidView(
                    factory = {
                        StyledPlayerView(it).apply {
                            player = exoPlayer
                            useController = true
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
                    .background(OffWhite)
                    .verticalScroll(scrollState)
            ) {
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
                                setFullscreenButtonClickListener {
                                    isFullscreen = true
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                }
                            }
                        },
                        modifier = Modifier.matchParentSize()
                    )
                }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600)) +
                            slideInVertically(initialOffsetY = { 100 })
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepPurple,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        downloadButtonPressed = true
                                        showDownloadConfirm = true
                                    },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .scale(downloadButtonScale)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                colors = listOf(BrightBlue, ElectricCyan)
                                            ),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MediumGray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ukuran: ${formatFileSize(fileSize)}",
                                    fontSize = 13.sp,
                                    color = MediumGray
                                )
                            }

                            if (tags.isNotEmpty() && tags != "null") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    tags.split(",").take(3).forEach { tag ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(tag.trim(), fontSize = 12.sp) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = BrightBlue.copy(alpha = 0.1f),
                                                labelColor = BrightBlue
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(thickness = 1.dp, color = LightGray)
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "profile")
                                val profileScale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.05f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "profile_pulse"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .scale(profileScale)
                                        .clip(CircleShape)
                                        .background(BrightBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (fullPhotoUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = fullPhotoUrl,
                                            contentDescription = "User Photo",
                                            modifier = Modifier.size(52.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = BrightBlue,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "@$username",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = DeepPurple
                                    )
                                    Text(
                                        text = "Uploader",
                                        fontSize = 13.sp,
                                        color = MediumGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(thickness = 1.dp, color = LightGray)
                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                "Deskripsi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepPurple
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (description.trim().isEmpty())
                                    "Tidak ada deskripsi." else description,
                                color = DarkGray,
                                lineHeight = 24.sp,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDownloadConfirm) {
        AlertDialog(
            onDismissRequest = { showDownloadConfirm = false },
            title = { Text("Unduh Video?", fontWeight = FontWeight.Bold) },
            text = { Text("Video akan disimpan ke folder Movies di HP kamu. Lanjutkan?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDownloadConfirm = false
                        downloadVideoWithTracking(context, fullVideoUrl, title, assetId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue)
                ) {
                    Text("Ya, Unduh", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadConfirm = false }) {
                    Text("Batal", color = MediumGray)
                }
            }
        )
    }
}

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

        Toast.makeText(context, "Download dimulai! 🔥", Toast.LENGTH_SHORT).show()

        val myUserId = SessionManager.getUserId()
        val body = mapOf("user_id" to myUserId)

        RetrofitClient.instance.trackDownload(assetId, body).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    android.util.Log.d("DownloadTracker", "Server updated successfully")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                android.util.Log.e("DownloadTracker", "Failed to update server: ${t.message}")
            }
        })
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal download: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun formatFileSize(size: Long): String {
    if (size <= 0) return "Unknown"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}