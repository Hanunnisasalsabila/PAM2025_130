package com.clipvault.clipvault.viewmodel

import com.google.gson.Gson
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.LoginRequest
import com.clipvault.clipvault.data.model.RegisterRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel : ViewModel() {

    // Fungsi Login
    fun login(email: String, pass: String, context: Context, onSuccess: () -> Unit) {
        val request = LoginRequest(email, pass)

        Log.d("DEBUG_LOGIN", "Mencoba Login dengan Email: $email")

        RetrofitClient.instance.loginUser(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                Log.d("DEBUG_LOGIN", "Respon Server Diterima! Code: ${response.code()}")

                val body = response.body()

                // === 1. CEK ISI BODY ===
                if (body != null) {
                    Log.d("DEBUG_LOGIN", "Isi Body: $body")
                    Log.d("DEBUG_LOGIN", "Token di Body: ${body.token}")
                } else {
                    Log.e("DEBUG_LOGIN", "Body KOSONG! (Mungkin error parsing Gson)")
                    // Coba baca error body kalau ada
                    val errorBody = response.errorBody()?.string()
                    Log.e("DEBUG_LOGIN", "Error Body Server: $errorBody")
                }

                // === 2. LOGIKA PENYIMPANAN ===
                if (response.isSuccessful && body != null && !body.error) {
                    Toast.makeText(context, "Login Berhasil! 🔓", Toast.LENGTH_SHORT).show()

                    if (body.token != null) {
                        Log.d("DEBUG_LOGIN", "Menyimpan Token: ${body.token}")
                        com.clipvault.clipvault.utils.SessionManager.saveToken(body.token)
                    } else {
                        Log.e("DEBUG_LOGIN", "GAWAT! Login Sukses tapi Token NULL!")
                    }

                    onSuccess()
                } else {
                    Toast.makeText(context, body?.message ?: "Login Gagal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("DEBUG_LOGIN", "Koneksi Gagal Total", t)
            }
        })
    }

    fun register(
        username: String,
        email: String,
        pass: String,
        fullName: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit // <--- TAMBAHAN: Biar Screen tau kalau gagal
    ) {
        val request = RegisterRequest(username, email, pass, fullName)

        RetrofitClient.instance.registerUser(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val body = response.body()

                if (response.isSuccessful && body != null && !body.error) {
                    // SUKSES
                    Toast.makeText(context, "Register Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
                    onSuccess()
                } else {
                    // GAGAL (Tapi kita bongkar pesan aslinya)
                    val errorMsg = try {
                        // Coba baca pesan error dari server (errorBody)
                        val errorBody = response.errorBody()?.string()
                        val gson = Gson() // Pastikan import Gson
                        val errorResponse = gson.fromJson(errorBody, AuthResponse::class.java)
                        errorResponse.message // "Username/Email sudah terdaftar!"
                    } catch (e: Exception) {
                        // Kalau gagal baca, pakai pesan default
                        "Register Gagal: ${response.code()}"
                    }
                    // Kirim pesan error ke Screen buat dijadiin Popup
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                onError("Koneksi Error: ${t.message}")
            }
        })
    }
}