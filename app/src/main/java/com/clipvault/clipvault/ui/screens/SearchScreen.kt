package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Folder // Icon default buat kategori
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.clipvault.clipvault.data.model.CategoryItem
import com.clipvault.clipvault.data.model.CategoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onVideoClick: (Int, String, String, String, String, Long, String, String) -> Unit
) {
    val context = LocalContext.current

    // State
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }

    var searchResults by remember { mutableStateOf<List<AssetItem>>(emptyList()) }
    var categoryList by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val serverIp = "http://10.78.80.195:3000/"

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .crossfade(true)
            .build()
    }

    // Fetch Kategori saat buka halaman
    LaunchedEffect(Unit) {
        RetrofitClient.instance.getCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                if (response.isSuccessful) categoryList = response.body()?.categories ?: emptyList()
            }
            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {}
        })
    }

    // Fungsi Search
    fun performSearch(text: String, catId: Int?) {
        isLoading = true
        RetrofitClient.instance.searchAssets(text, catId).enqueue(object : Callback<AssetResponse> {
            override fun onResponse(call: Call<AssetResponse>, response: Response<AssetResponse>) {
                isLoading = false
                if (response.isSuccessful && response.body()?.error == false) {
                    searchResults = response.body()?.assets ?: emptyList()
                } else {
                    searchResults = emptyList()
                }
            }
            override fun onFailure(call: Call<AssetResponse>, t: Throwable) {
                isLoading = false
            }
        })
    }

    // Logic Tampilan: Apakah sedang mode cari/filter?
    val isSearchingOrFiltering = query.isNotEmpty() || selectedCategory != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari Aset") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // 1. SEARCH BAR (SRS: Search bar di atas)
            OutlinedTextField(
                value = query,
                onValueChange = { newText ->
                    query = newText
                    if (newText.isEmpty() && selectedCategory == null) {
                        searchResults = emptyList() // Reset kalau kosong
                    } else {
                        performSearch(newText, selectedCategory?.id)
                    }
                },
                placeholder = { Text("Cari judul atau user...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = {
                            query = ""
                            if (selectedCategory == null) searchResults = emptyList()
                            else performSearch("", selectedCategory?.id)
                        }) { Icon(Icons.Default.Close, null) }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00897B),
                    focusedLabelColor = Color(0xFF00897B)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === LOGIC TAMPILAN GANTI-GANTIAN ===

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00897B))
                }
            }
            // 2. TAMPILAN GRID KATEGORI (Kalau belum cari apa-apa)
            // Sesuai SRS: "Grid kategori dalam bentuk card dengan icon dan nama"
            else if (!isSearchingOrFiltering) {

                Text("Jelajahi Kategori", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // 3 Kolom biar rapi
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryList) { category ->
                        CategoryCard(
                            category = category,
                            onClick = {
                                selectedCategory = category
                                performSearch(query, category.id)
                            }
                        )
                    }
                }
            }
            // 3. TAMPILAN HASIL PENCARIAN (Kalau sedang cari/filter)
            else {
                // Info Filter Aktif
                if (selectedCategory != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            "Kategori: ${selectedCategory!!.name}",
                            color = Color(0xFF00897B),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Tombol Hapus Filter Kategori
                        IconButton(
                            onClick = {
                                selectedCategory = null
                                if (query.isEmpty()) searchResults = emptyList()
                                else performSearch(query, null)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                }

                // Empty State (SRS: Empty state jika tidak ada hasil)
                if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ditemukan hasil.", color = Color.Gray)
                        }
                    }
                } else {
                    // Grid Hasil Pencarian
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(searchResults) { asset ->
                            // Kartu Video
                            Card(
                                modifier = Modifier
                                    .height(160.dp)
                                    .clickable {
                                        onVideoClick(
                                            asset.asset_id,
                                            asset.file_path,
                                            asset.title,
                                            asset.description ?: "",
                                            asset.username,
                                            asset.file_size ?: 0L,
                                            asset.photo ?: "",
                                            asset.tags ?: ""
                                        )
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
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
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = asset.title,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "@${asset.username}",
                                                color = Color.LightGray,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// === KOMPONEN CARD KATEGORI (SESUAI SRS) ===
@Composable
fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Biar kotak persegi
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Kategori (Kalau icon null, pakai icon folder default)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00897B).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.icon ?: "📂", // Icon Emoji atau teks
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nama Kategori
            Text(
                text = category.name,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}