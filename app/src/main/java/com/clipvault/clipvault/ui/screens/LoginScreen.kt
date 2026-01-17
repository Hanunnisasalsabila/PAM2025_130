package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.LoginRequest
import com.clipvault.clipvault.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    onLoginSuccess: (Int) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current

    // State Input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State Tampilan
    var isLoading by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) } // State mata password

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ClipVault",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00897B)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // INPUT EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // INPUT PASSWORD (DENGAN TOGGLE VISIBILITY)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            // Logic Show/Hide Password
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading,
            trailingIcon = {
                val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle Password")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF00897B))
        } else {
            Button(
                onClick = {
                    // === PERBAIKAN VALIDASI SESUAI SRS 1.1.3 ===

                    // 1. Cek Kosong
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Isi email dan password!", Toast.LENGTH_SHORT).show()
                    }
                    // 2. Cek Format Email (Harus ada @ dan domain)
                    else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
                    }
                    // 3. Kalau Aman, Lanjut Login ke Server
                    else {
                        isLoading = true
                        val request = LoginRequest(email, password)

                        RetrofitClient.instance.loginUser(request).enqueue(object : Callback<AuthResponse> {
                            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                isLoading = false
                                val body = response.body()

                                if (response.isSuccessful && body != null && !body.error) {
                                    Toast.makeText(context, "Login Berhasil! 🔓", Toast.LENGTH_SHORT).show()

                                    // Simpan Token
                                    body.token?.let { token ->
                                        SessionManager.saveToken(token)
                                    }

                                    // Pindah Halaman
                                    val userId = body.user?.id ?: 0
                                    SessionManager.saveUserId(userId)
                                    onLoginSuccess(userId)

                                } else {
                                    // Handle Error Message dari Server
                                    val errorMsg = try {
                                        val errorString = response.errorBody()?.string()
                                        val gson = Gson()
                                        val errorResponse = gson.fromJson(errorString, AuthResponse::class.java)
                                        errorResponse.message
                                    } catch (e: Exception) {
                                        "Login Gagal (${response.code()})"
                                    }
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                isLoading = false
                                Toast.makeText(context, "Koneksi Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text("Masuk (Login)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
            Text("Belum punya akun? Daftar", color = Color.Gray)
        }
    }
}