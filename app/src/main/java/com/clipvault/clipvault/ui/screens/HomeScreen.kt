package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    onNavigateToUpload: () -> Unit,
    // UPDATE DISINI: Tambahkan satu String lagi di akhir untuk Tags
    onVideoClick: (Int, String, String, String, String, Long, String, String) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    var assetList by remember { mutableStateOf<List<AssetItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
            CenterAlignedTopAppBar(
                title = { Text("ClipVault", fontWeight = FontWeight.Bold, color = Color(0xFF00897B)) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color(0xFF00897B))
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profil", tint = Color(0xFF00897B))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToUpload,
                containerColor = Color(0xFF00897B)
            ) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp)) { // Padding kiri kanan dikit aja

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // === UBAH JADI GRID 2 KOLOM (SESUAI SRS) ===
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Biar gak ketutupan FAB
                ) {
                    items(assetList) { asset ->
                        VideoGridCard(
                            asset = asset,
                            serverIp = serverIp,
                            imageLoader = imageLoader,
                            onClick = {
                                val cleanPath = asset.file_path.replace("\\", "/")
                                onVideoClick(
                                    asset.asset_id, cleanPath, asset.title, asset.description ?: "",
                                    asset.username, asset.file_size ?: 0L, asset.photo ?: "", asset.tags ?: ""
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
fun VideoGridCard(
    asset: AssetItem,
    serverIp: String,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    val cleanPath = asset.file_path.replace("\\", "/")
    val fullUrl = serverIp + cleanPath

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Tinggi fix biar rapi
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Thumbnail
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
                    .height(140.dp) // Gambar menuhin atas
                    .background(Color.Black)
            )

            // Icon Play di tengah gambar
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp) // Posisikan agak ke tengah area gambar
            ) {
                Icon(
                    imageVector = Icons.Default.Search, // Pake icon play kalau ada, atau search sementara
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.0f) // Invisible aja, cuma buat layout
                )
            }

            // 2. Info di Bawah (Judul & Username)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(60.dp) // Sisa tinggi buat teks
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Text(
                    text = asset.title,
                    style = MaterialTheme.typography.titleSmall, // Font lebih kecil
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${asset.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}