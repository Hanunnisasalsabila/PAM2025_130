package com.clipvault.clipvault.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.CategoryItem
import com.clipvault.clipvault.data.model.CategoryResponse
import com.clipvault.clipvault.ui.components.ClipVaultButton
import com.clipvault.clipvault.ui.components.ClipVaultInput
import com.clipvault.clipvault.ui.theme.*
import com.clipvault.clipvault.utils.FileUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    userId: Int,
    onUploadSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var categoryList by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                if (response.isSuccessful && response.body()?.error == false) {
                    categoryList = response.body()?.categories ?: emptyList()
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                Toast.makeText(context, "Gagal memuat kategori", Toast.LENGTH_SHORT).show()
            }
        })
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedVideoUri = uri }

    fun doUpload() {
        if (selectedVideoUri != null && title.isNotEmpty() && selectedCategory != null) {
            val fileSize = FileUtils.getFileSize(context, selectedVideoUri!!)
            if (fileSize > 100 * 1024 * 1024) {
                errorMessage = "Ukuran file terlalu besar! Maksimal 100MB."
                return
            }

            isLoading = true
            errorMessage = null

            uploadVideoToServer(
                context, userId, selectedVideoUri!!, title, description,
                tags, selectedCategory!!.id
            ) { success, msg ->
                isLoading = false
                if (success) {
                    onUploadSuccess()
                } else {
                    errorMessage = if (msg.contains("Failed to connect") ||
                        msg.contains("Unable to resolve")
                    ) {
                        "Gagal terhubung ke server. Periksa koneksi internet / WiFi."
                    } else {
                        "Gagal Upload: $msg"
                    }
                }
            }
        } else {
            Toast.makeText(context, "Lengkapi Video, Judul, dan Kategori!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // === JUDUL ANIMATED ===
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -50 })
        ) {
            Text(
                "Upload Aset Video",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DeepPurple
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === TOMBOL PILIH VIDEO (ANIMATED) ===
        var buttonScale by remember { mutableStateOf(1f) }
        val animatedScale by animateFloatAsState(
            targetValue = buttonScale,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "button_scale"
        )

        Button(
            onClick = {
                buttonScale = 0.9f
                videoPickerLauncher.launch("video/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .scale(animatedScale),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedVideoUri != null) SuccessGreen else MediumGray
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (selectedVideoUri != null) Icons.Default.CheckCircle
                    else Icons.Default.VideoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (selectedVideoUri != null) "✅ Video Terpilih"
                    else "📹 Pilih Video dari Galeri",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LaunchedEffect(buttonScale) {
            if (buttonScale != 1f) {
                kotlinx.coroutines.delay(100)
                buttonScale = 1f
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === FORM FIELDS (STAGGERED ANIMATION) ===
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                    slideInHorizontally(initialOffsetX = { 100 })
        ) {
            ClipVaultInput(
                value = title,
                onValueChange = { title = it },
                label = "Judul Aset (Wajib)"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dropdown Kategori
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 300)) +
                    slideInHorizontally(initialOffsetX = { 100 })
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Pilih Kategori",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown, null,
                            Modifier.clickable { expandedDropdown = !expandedDropdown }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDropdown = true },
                    enabled = false,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = BlackText,
                        disabledBorderColor = MediumGray,
                        disabledLabelColor = MediumGray
                    )
                )
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable { expandedDropdown = true })
                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    categoryList.forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.icon ?: "📂"} ${category.name}") },
                            onClick = {
                                selectedCategory = category
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 400)) +
                    slideInHorizontally(initialOffsetX = { 100 })
        ) {
            ClipVaultInput(
                value = tags,
                onValueChange = { tags = it },
                label = "Tags (Pisahkan koma)"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 500)) +
                    slideInHorizontally(initialOffsetX = { 100 })
        ) {
            ClipVaultInput(
                value = description,
                onValueChange = { description = it },
                label = "Deskripsi (Opsional)"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // === STATUS INDICATORS ===
        AnimatedContent(
            targetState = when {
                isLoading -> "loading"
                errorMessage != null -> "error"
                else -> "idle"
            },
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "status"
        ) { status ->
            when (status) {
                "loading" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            color = BrightBlue,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Sedang mengupload...",
                            color = DeepPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                "error" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { doUpload() },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("COBA LAGI")
                        }
                    }
                }

                else -> {
                    Column {
                        ClipVaultButton(
                            text = "UPLOAD KE SERVER 🚀",
                            gradient = true,
                            onClick = { doUpload() }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                selectedVideoUri = null
                                title = ""
                                description = ""
                                tags = ""
                                selectedCategory = null
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ErrorRed
                            )
                        ) {
                            Text("BATAL & BERSIHKAN", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

fun uploadVideoToServer(
    context: Context, userId: Int, uri: Uri, title: String, desc: String,
    tags: String, categoryId: Int, onResult: (Boolean, String) -> Unit
) {
    val file = FileUtils.getFileFromUri(context, uri)
    if (file == null) {
        onResult(false, "File tidak ditemukan / Corrupt")
        return
    }

    val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
    val videoPart = MultipartBody.Part.createFormData("video", file.name, requestFile)
    val userIdPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
    val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
    val catPart = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    val tagsPart = tags.toRequestBody("text/plain".toMediaTypeOrNull())

    RetrofitClient.instance.uploadVideo(
        videoPart, userIdPart, titlePart, descPart, catPart, tagsPart
    ).enqueue(object : Callback<AuthResponse> {
        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
            if (response.isSuccessful && response.body()?.error == false) {
                Toast.makeText(context, "Upload Berhasil!", Toast.LENGTH_LONG).show()
                onResult(true, "Sukses")
            } else {
                val msg = response.body()?.message ?: response.message()
                onResult(false, msg)
            }
        }

        override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
            onResult(false, t.message ?: "Unknown Error")
        }
    })
}