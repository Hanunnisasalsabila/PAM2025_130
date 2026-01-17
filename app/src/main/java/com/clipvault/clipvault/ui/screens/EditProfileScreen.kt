package com.clipvault.clipvault.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import coil.compose.AsyncImage
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.EditProfileRequest
import com.clipvault.clipvault.data.model.ProfileResponse
import com.clipvault.clipvault.utils.FileUtils
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userId: Int,
    onBack: () -> Unit,
    onSuccessUpdate: () -> Unit
) {
    val context = LocalContext.current

    // State Data User
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var currentServerPhotoPath by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) } // Loading khusus pas tombol simpan
    var isUploadingPhoto by remember { mutableStateOf(false) }

    // === STATE DIALOG (SATPAM) ===
    var showSaveDialog by remember { mutableStateOf(false) }        // Satpam Simpan
    var showDeletePhotoDialog by remember { mutableStateOf(false) } // Satpam Hapus Foto
    var showErrorDialog by remember { mutableStateOf(false) }       // Popup Error
    var errorMessage by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    val serverIp = "http://10.78.80.195:3000/"

    // 1. Ambil Data Profil Saat Ini
    LaunchedEffect(userId) {
        RetrofitClient.instance.getUserProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                isLoading = false
                if (response.isSuccessful && response.body()?.user != null) {
                    val user = response.body()!!.user
                    fullName = user.full_name ?: ""
                    username = user.username
                    bio = user.bio ?: ""
                    location = user.location ?: ""
                    currentServerPhotoPath = user.photo
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // === FOTO PROFIL ===
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF00897B), CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!currentServerPhotoPath.isNullOrEmpty()) {
                        AsyncImage(
                            model = serverIp + currentServerPhotoPath,
                            contentDescription = "Server Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(80.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                        enabled = !isSaving
                    ) { Text("Ubah Foto") }

                    if (!currentServerPhotoPath.isNullOrEmpty() || selectedImageUri != null) {
                        OutlinedButton(
                            onClick = {
                                if (selectedImageUri != null) {
                                    // Kalau baru milih lokal, langsung batalin aja gapapa
                                    selectedImageUri = null
                                } else {
                                    // Kalau mau hapus foto server, TANYA DULU!
                                    showDeletePhotoDialog = true
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            enabled = !isSaving
                        ) { Text("Hapus") }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // FORM INPUT
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(30.dp))

                // TOMBOL SIMPAN (Cuma Memicu Dialog, Gak Langsung Simpan)
                Button(
                    onClick = { showSaveDialog = true }, // <--- PANGGIL SATPAM
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menyimpan...")
                    } else {
                        Text("💾 SIMPAN SEMUA PERUBAHAN")
                    }
                }
            }
        }
    }

    // === DIALOG 1: KONFIRMASI SIMPAN ===
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Simpan Perubahan?") },
            text = { Text("Pastikan data yang Anda masukkan sudah benar.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    onClick = {
                        showSaveDialog = false
                        isSaving = true // Mulai Loading

                        // LOGIC SIMPAN PINDAH KESINI
                        val request = EditProfileRequest(fullName, username, bio, location)
                        RetrofitClient.instance.updateProfile(userId, request).enqueue(object : Callback<AuthResponse> {
                            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                if (response.isSuccessful && response.body()?.error == false) {
                                    if (selectedImageUri != null) {
                                        uploadPhotoToServer(context, userId, selectedImageUri!!) {
                                            isSaving = false
                                            onSuccessUpdate()
                                        }
                                    } else {
                                        isSaving = false
                                        Toast.makeText(context, "Profil Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
                                        onSuccessUpdate()
                                    }
                                } else {
                                    isSaving = false
                                    // Handle Error Username Kembar dll
                                    val errorMsg = try {
                                        val errorBody = response.errorBody()?.string()
                                        val gson = Gson()
                                        val errorResponse = gson.fromJson(errorBody, AuthResponse::class.java)
                                        errorResponse.message
                                    } catch (e: Exception) {
                                        "Gagal Update: ${response.message()}"
                                    }
                                    errorMessage = errorMsg
                                    showErrorDialog = true
                                }
                            }
                            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                isSaving = false
                                errorMessage = "Koneksi Error: ${t.message}"
                                showErrorDialog = true
                            }
                        })
                    }
                ) { Text("Ya, Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Batal") }
            }
        )
    }

    // === DIALOG 2: KONFIRMASI HAPUS FOTO ===
    if (showDeletePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePhotoDialog = false },
            title = { Text("Hapus Foto Profil?") },
            text = { Text("Foto profil Anda akan dihapus permanen. Lanjutkan?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        showDeletePhotoDialog = false
                        isUploadingPhoto = true

                        // LOGIC HAPUS FOTO PINDAH KESINI
                        RetrofitClient.instance.deleteProfilePhoto(userId).enqueue(object : Callback<AuthResponse>{
                            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                isUploadingPhoto = false
                                if(response.isSuccessful) {
                                    currentServerPhotoPath = null
                                    Toast.makeText(context, "Foto berhasil dihapus", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                isUploadingPhoto = false
                                Toast.makeText(context, "Gagal menghapus foto", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePhotoDialog = false }) { Text("Batal") }
            }
        )
    }

    // === DIALOG 3: ERROR MESSAGE (USERNAME KEMBAR, DLL) ===
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Gagal Update") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) { Text("Oke") }
            }
        )
    }
}

// FUNGSI HELPER UPLOAD FOTO (TETAP SAMA)
fun uploadPhotoToServer(context: android.content.Context, userId: Int, uri: Uri, onFinished: () -> Unit) {
    val file = FileUtils.getFileFromUri(context, uri)
    if (file == null) {
        Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        onFinished()
        return
    }
    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
    val photoPart = MultipartBody.Part.createFormData("photo", file.name, requestFile)

    RetrofitClient.instance.uploadProfilePhoto(userId, photoPart).enqueue(object : Callback<AuthResponse>{
        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
            onFinished()
        }
        override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
            onFinished()
        }
    })
}