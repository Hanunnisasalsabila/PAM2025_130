package com.clipvault.clipvault.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.CategoryItem
import com.clipvault.clipvault.data.model.CategoryResponse
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
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // State Loading & Error
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State Kategori
    var categoryList by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    // 1. Ambil Data Kategori
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

    // --- FUNGSI UTAMA UPLOAD (Dengan Logika Error Handling & Size Check) ---
    fun doUpload() {
        if (selectedVideoUri != null && title.isNotEmpty() && selectedCategory != null) {

            // 1. REQ-9: Cek Ukuran File (Max 100MB)
            val fileSize = FileUtils.getFileSize(context, selectedVideoUri!!)
            if (fileSize > 100 * 1024 * 1024) {
                errorMessage = "Ukuran file terlalu besar! Maksimal 100MB."
                return
            }

            isLoading = true
            errorMessage = null // Reset error dulu

            // Panggil Fungsi Server
            uploadVideoToServer(context, userId, selectedVideoUri!!, title, description, tags, selectedCategory!!.id) { success, msg ->
                isLoading = false

                if (success) {
                    // Jika Sukses
                    onUploadSuccess()
                } else {
                    // Jika Gagal (REQ-14: Translate Error)
                    errorMessage = if (msg.contains("Failed to connect") || msg.contains("Unable to resolve")) {
                        "Gagal terhubung ke server. Periksa koneksi internet / WiFi."
                    } else {
                        "Gagal Upload: $msg"
                    }
                }
            }
        } else {
            Toast.makeText(context, "Lengkapi Video, Judul, dan Kategori!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Upload Aset Video", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Tombol Pilih Video (REQ-8)
        Button(
            onClick = { videoPickerLauncher.launch("video/*") },
            colors = ButtonDefaults.buttonColors(containerColor = if (selectedVideoUri != null) Color(0xFF00897B) else Color.Gray)
        ) {
            Text(if (selectedVideoUri != null) "✅ Video Terpilih" else "📁 Pilih Video dari Galeri")
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Judul Aset (Wajib)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Dropdown Kategori
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Pilih Kategori", onValueChange = {}, readOnly = true,
                label = { Text("Kategori") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { expandedDropdown = !expandedDropdown }) },
                modifier = Modifier.fillMaxWidth().clickable { expandedDropdown = true }, enabled = false,
                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray)
            )
            Box(modifier = Modifier.matchParentSize().clickable { expandedDropdown = true })
            DropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                categoryList.forEach { category ->
                    DropdownMenuItem(text = { Text("${category.icon ?: "📂"} ${category.name}") }, onClick = { selectedCategory = category; expandedDropdown = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = tags, onValueChange = { tags = it },
            label = { Text("Tags (Pisahkan koma)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Deskripsi (Opsional)") }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        // === LOGIKA UI (REQ-10 & REQ-14) ===
        if (isLoading) {
            // TAMPILAN LOADING BAR (GARIS)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF00897B))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sedang mengupload...", color = Color.Gray)
            }
        }
        else if (errorMessage != null) {
            // TAMPILAN ERROR + RETRY
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)).padding(16.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                Text(text = errorMessage!!, color = Color.Red, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { doUpload() }, // Panggil ulang fungsi upload
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("COBA LAGI (RETRY)")
                }
            }
        }
        else {
            // TOMBOL NORMAL
            Button(
                onClick = { doUpload() }, // Panggil fungsi doUpload, JANGAN nulis logika manual lagi disini
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text("UPLOAD KE SERVER 🚀")
            }
            Spacer(modifier = Modifier.height(12.dp))

            // TOMBOL BATAL
            OutlinedButton(
                onClick = {
                    // Logic Batal: Reset semua field
                    selectedVideoUri = null
                    title = ""
                    description = ""
                    tags = ""
                    selectedCategory = null
                    errorMessage = null
                    // Atau kalau mau keluar halaman: (tapi butuh parameter onBack)
                    // onUploadSuccess() // Pura-pura sukses biar balik ke home
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("BATAL & BERSIHKAN")
            }
        }
    }
}

// === FUNGSI HELPER UPLOAD (FIXED PARAMETER) ===
fun uploadVideoToServer(
    context: Context, userId: Int, uri: Uri, title: String, desc: String, tags: String, categoryId: Int,
    // PERBAIKAN: Callback harus terima (Boolean, String) agar bisa kirim pesan error
    onResult: (Boolean, String) -> Unit
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

    RetrofitClient.instance.uploadVideo(videoPart, userIdPart, titlePart, descPart, catPart, tagsPart)
        .enqueue(object : Callback<AuthResponse> {
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
                // Kirim pesan error asli ke UI agar bisa ditranslate
                onResult(false, t.message ?: "Unknown Error")
            }
        })
}