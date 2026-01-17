package com.clipvault.clipvault.data.model

import com.google.gson.annotations.SerializedName

// 1. Format data yang dikirim saat Login
data class LoginRequest(
    val email: String,
    val password: String
)

// 2. Format data yang dikirim saat Register
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("full_name")
    val full_name: String
)

// 3. Format respon dari Server (Login & Register)
data class AuthResponse(
    @SerializedName("error")
    val error: Boolean,

    @SerializedName("message")
    val message: String,

    // === PERBAIKAN DISINI: PAKSA BACA "token" ===
    @SerializedName("token")
    val token: String?,
    // ============================================

    @SerializedName("user")
    val user: UserData?
)

// 4. DETAIL DATA USER (BAGIAN PENTING!)
data class UserData(
    // Pakai alternate biar aman: Login server kirim 'id', tapi Profile kirim 'user_id'
    @SerializedName("user_id", alternate = ["id"])
    val id: Int,

    val username: String,
    val email: String,

    @SerializedName("full_name")
    val full_name: String?,

    // === PERBAIKAN UTAMA ===
    // Server kirim "profile_photo", Android baca sebagai "photo"
    @SerializedName("profile_photo")
    val photo: String?,
    // =======================

    val bio: String?,
    val location: String?
)

// 5. Format respon daftar aset (Home & Search)
data class AssetResponse(
    val error: Boolean,
    val message: String,
    val assets: List<AssetItem>
)

// 6. Detail per video
data class AssetItem(
    @SerializedName("asset_id")
    val asset_id: Int,

    val title: String,
    val description: String?,

    @SerializedName("file_path")
    val file_path: String, // Path relatif, misal: "uploads/assets/..."

    val username: String,  // Nama uploader (dari hasil JOIN tabel users)
    val created_at: String?,

    // === TAMBAHAN BARU ===
    @SerializedName("download_count")
    val download_count: Int = 0, // Default 0 kalau null

    @SerializedName("file_size")
    val file_size: Long?,  // Tangkap ukuran file dari server

    @SerializedName("profile_photo")
    val photo: String?,     // Tangkap foto profil dari server

    val tags: String?

)

// 7. Respon untuk Profile Page
data class ProfileResponse(
    val error: Boolean,
    val message: String,
    val user: UserData,         // Info User
    val assets: List<AssetItem> // Daftar Video User
)

// 8. Data yang dikirim saat Edit Profil
data class EditProfileRequest(
    @SerializedName("full_name")
    val full_name: String,
    val username: String,
    val bio: String,
    val location: String
)

// Model Kategori
data class CategoryItem(
    @SerializedName("category_id") val id: Int,
    @SerializedName("category_name") val name: String,
    val icon: String?
)

// Respon API Kategori
data class CategoryResponse(
    val error: Boolean,
    val categories: List<CategoryItem>
)