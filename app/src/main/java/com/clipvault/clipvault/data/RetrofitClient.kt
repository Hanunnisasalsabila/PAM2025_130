package com.clipvault.clipvault.data

import com.clipvault.clipvault.utils.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Pastikan IP ini benar
    private const val BASE_URL = "http://10.78.80.195:3000/"

    // 1. Bikin Client Khusus (Interceptor)
    // Tugasnya: Mencegat request -> Tempel Token -> Kirim ke Server
    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val original = chain.request()
        val token = SessionManager.getToken()

        // --- PASANG LOG DISINI (CCTV) ---
        android.util.Log.d("CCTV_TOKEN", "Token di Dompet: $token")
        // --------------------------------

        val requestBuilder = original.newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
            // Log Header yang dikirim
            android.util.Log.d("CCTV_HEADER", "Header dikirim: Bearer $token")
        } else {
            android.util.Log.e("CCTV_ERROR", "GAWAT! Token KOSONG saat mau request!")
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }.build()

    // 2. Pasang Client tadi ke Retrofit
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client) // <--- PENTING: Gunakan client yang sudah dimodifikasi
            .build()

        retrofit.create(ApiService::class.java)
    }
}