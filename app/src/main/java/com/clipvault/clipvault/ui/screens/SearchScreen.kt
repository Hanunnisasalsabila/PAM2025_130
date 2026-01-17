package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.clipvault.clipvault.ui.theme.*
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
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var searchResults by remember { mutableStateOf<List<AssetItem>>(emptyList()) }
    var categoryList by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showCategories by remember { mutableStateOf(true) }

    val serverIp = "http://10.78.80.195:3000/"

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .crossfade(true)
            .build()
    }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                if (response.isSuccessful) categoryList = response.body()?.categories ?: emptyList()
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {}
        })
    }

    fun performSearch(text: String, catId: Int?) {
        isLoading = true
        showCategories = false
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

    val isSearchingOrFiltering = query.isNotEmpty() || selectedCategory != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari Aset", color = White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrightBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(OffWhite)
                .padding(16.dp)
        ) {
            // === ANIMATED SEARCH BAR ===
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { newText ->
                        query = newText
                        if (newText.isEmpty() && selectedCategory == null) {
                            searchResults = emptyList()
                            showCategories = true
                        } else {
                            performSearch(newText, selectedCategory?.id)
                        }
                    },
                    placeholder = { Text("Cari judul atau user...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = BrightBlue) },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                query = ""
                                if (selectedCategory == null) {
                                    searchResults = emptyList()
                                    showCategories = true
                                } else performSearch("", selectedCategory?.id)
                            }) {
                                Icon(Icons.Default.Close, null, tint = MediumGray)
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightBlue,
                        focusedLabelColor = BrightBlue,
                        unfocusedBorderColor = MediumGray,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === CONTENT WITH ANIMATIONS ===
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrightBlue)
                }
            } else if (showCategories && !isSearchingOrFiltering) {
                // === CATEGORY GRID ===
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column {
                        Text(
                            "Jelajahi Kategori",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepPurple
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(categoryList) { index, category ->
                                AnimatedCategoryCard(
                                    category = category,
                                    index = index,
                                    onClick = {
                                        selectedCategory = category
                                        performSearch(query, category.id)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // === SEARCH RESULTS ===
                AnimatedVisibility(
                    visible = isSearchingOrFiltering,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })
                ) {
                    Column {
                        // Filter Chip
                        if (selectedCategory != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                AssistChip(
                                    onClick = {
                                        selectedCategory = null
                                        if (query.isEmpty()) {
                                            searchResults = emptyList()
                                            showCategories = true
                                        } else performSearch(query, null)
                                    },
                                    label = { Text("${selectedCategory!!.name}") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = BrightBlue.copy(alpha = 0.1f),
                                        labelColor = BrightBlue
                                    )
                                )
                            }
                        }

                        // Results Grid
                        if (searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔍", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tidak ditemukan hasil.", color = MediumGray)
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(searchResults) { index, asset ->
                                    AnimatedSearchResultCard(
                                        asset = asset,
                                        serverIp = serverIp,
                                        imageLoader = imageLoader,
                                        index = index,
                                        onClick = {
                                            onVideoClick(
                                                asset.asset_id, asset.file_path, asset.title,
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
        }
    }
}

@Composable
private fun AnimatedCategoryCard(
    category: CategoryItem,
    index: Int,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "category_press"
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(400, delayMillis = 50 * index)) +
                scaleIn(initialScale = 0.8f, animationSpec = tween(400, delayMillis = 50 * index))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .scale(scale)
                .clickable {
                    isPressed = true
                    onClick()
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrightBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.icon ?: "📂", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = DeepPurple
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun AnimatedSearchResultCard(
    asset: AssetItem,
    serverIp: String,
    imageLoader: ImageLoader,
    index: Int,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "result_press"
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300, delayMillis = 30 * index)) +
                slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(300, delayMillis = 30 * index)
                )
    ) {
        Card(
            modifier = Modifier
                .height(160.dp)
                .scale(scale)
                .clickable {
                    isPressed = true
                    onClick()
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val cleanPath = asset.file_path.replace("\\", "/")
                val fullUrl = serverIp + cleanPath

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
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
                        .background(Color.Black.copy(alpha = 0.7f))
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

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}