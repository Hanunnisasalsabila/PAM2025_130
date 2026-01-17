package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AssetItem
import com.clipvault.clipvault.data.model.AssetResponse
import com.clipvault.clipvault.ui.theme.*
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    onNavigateToUpload: () -> Unit,
    onVideoClick: (Int, String, String, String, String, Long, String, String) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    var assetList by remember { mutableStateOf<List<AssetItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(false) }

    val serverIp = "http://10.78.80.195:3000/"

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .crossfade(true)
            .build()
    }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getAllAssets().enqueue(object : Callback<AssetResponse> {
            override fun onResponse(call: Call<AssetResponse>, response: Response<AssetResponse>) {
                isLoading = false
                if (response.isSuccessful && response.body()?.error == false) {
                    assetList = response.body()?.assets ?: emptyList()
                    isVisible = true
                } else {
                    Toast.makeText(context, "Gagal ambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AssetResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        topBar = {
            // === BAGIAN INI SUDAH DIBERSIHKAN ===
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ClipVault",
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 1.sp,
                        fontSize = 22.sp
                    )
                },
                // Block 'actions = { ... }' SUDAH DIHAPUS DISINI
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BrightBlue // Tetap pakai warna biru logo barumu
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(OffWhite)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrightBlue)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(assetList) { index, asset ->
                        AnimatedVideoCard(
                            asset = asset,
                            serverIp = serverIp,
                            imageLoader = imageLoader,
                            index = index,
                            isVisible = isVisible,
                            onClick = {
                                val cleanPath = asset.file_path.replace("\\", "/")
                                onVideoClick(
                                    asset.asset_id, cleanPath, asset.title,
                                    asset.description ?: "", asset.username,
                                    asset.file_size ?: 0L, asset.photo ?: "",
                                    asset.tags ?: ""
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedVideoCard(
    asset: AssetItem,
    serverIp: String,
    imageLoader: ImageLoader,
    index: Int,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_press"
    )

    val cleanPath = asset.file_path.replace("\\", "/")
    val fullUrl = serverIp + cleanPath

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = 50 * index
            )
        ) + slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = 50 * index
            )
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    onClick()
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(fullUrl)
                        .videoFrameMillis(2000)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(DeepPurple.copy(alpha = 0.1f))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(White)
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = asset.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = DeepPurple
                        )
                        Text(
                            text = "@${asset.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGray,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}