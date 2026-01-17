package com.clipvault.clipvault.data

import com.clipvault.clipvault.data.model.AssetResponse
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.CategoryResponse
import com.clipvault.clipvault.data.model.EditProfileRequest
import com.clipvault.clipvault.data.model.LoginRequest
import com.clipvault.clipvault.data.model.ProfileResponse
import com.clipvault.clipvault.data.model.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // --- 1. FITUR LOGIN ---
    // Mengirim email & password (JSON)
    @POST("login")
    fun loginUser(@Body request: LoginRequest): Call<AuthResponse>

    // --- 2. FITUR REGISTER ---
    // Mengirim data pendaftaran (JSON)
    @POST("register")
    fun registerUser(@Body request: RegisterRequest): Call<AuthResponse>

    // --- 3. FITUR UPLOAD VIDEO ---
    // Sesuai SRS REQ-12: Upload file fisik + metadata (judul, deskripsi, dll)
    // Kita wajib pakai @Multipart karena mengirim File + Text sekaligus
    @Multipart
    @POST("upload")
    fun uploadVideo(
        // @Part -> Digunakan untuk setiap bagian data dalam Multipart

        // 1. File Video (Binary)
        @Part video: MultipartBody.Part,

        // 2. Metadata (Text) - Harus dibungkus RequestBody
        @Part("user_id") userId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("tags") tags: RequestBody

    ): Call<AuthResponse>
    // Kita menggunakan AuthResponse karena format respon server mirip:
    // { error: false, message: "Upload Berhasil", ... }
    // --- AMBIL DAFTAR VIDEO (HOME) ---
    @GET("assets")
    fun getAllAssets(): Call<AssetResponse>

    // --- AMBIL PROFIL ---
    @GET("users/{id}")
    fun getUserProfile(@Path("id") id: Int): Call<ProfileResponse>

    // --- SEARCH ASET ---
    // Update parameter: Tambah categoryId (bisa null)
    @GET("search")
    fun searchAssets(
        @Query("q") query: String,
        @Query("category_id") categoryId: Int?
    ): Call<AssetResponse>

    // --- AMBIL KATEGORI ---
    @GET("categories")
    fun getCategories(): Call<CategoryResponse>

    // --- UPDATE PROFIL ---
    @PUT("users/{id}")
    fun updateProfile(
        @Path("id") id: Int,
        @Body request: EditProfileRequest
    ): Call<AuthResponse>

    // --- HAPUS ASET ---
    @DELETE("assets/{id}")
    fun deleteAsset(@Path("id") id: Int): Call<AuthResponse>

    // --- UPLOAD FOTO PROFIL (BARU) ---
    @Multipart
    @POST("users/{id}/photo")
    fun uploadProfilePhoto(
        @Path("id") id: Int,
        @Part photo: MultipartBody.Part
    ): Call<AuthResponse> // Kita pakai AuthResponse karena strukturnya mirip (error & message)

    // --- HAPUS FOTO PROFIL (BARU) ---
    @DELETE("users/{id}/photo")
    fun deleteProfilePhoto(@Path("id") id: Int): Call<AuthResponse>

    // --- LAPOR DOWNLOAD ---
    @POST("assets/{id}/download")
    fun trackDownload(
        @Path("id") assetId: Int,
        @Body body: Map<String, Int> // Kita kirim User ID lewat body JSON
    ): Call<AuthResponse>
}