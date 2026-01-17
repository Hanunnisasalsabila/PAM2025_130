package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipvault.clipvault.R
import com.clipvault.clipvault.data.RetrofitClient
import com.clipvault.clipvault.data.model.AuthResponse
import com.clipvault.clipvault.data.model.LoginRequest
import com.clipvault.clipvault.ui.components.ClipVaultButton
import com.clipvault.clipvault.ui.components.ClipVaultGradientBackground
import com.clipvault.clipvault.ui.components.ClipVaultInput
import com.clipvault.clipvault.ui.components.ClipVaultOutlinedButton
import com.clipvault.clipvault.utils.SessionManager
import com.clipvault.clipvault.ui.theme.*
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // === GRADIENT BACKGROUND ===
    ClipVaultGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // === LOGO ===
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === JUDUL DENGAN STYLE BARU ===
            Text(
                text = "ClipVault",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = White,
                letterSpacing = 1.sp
            )

            Text(
                text = "Your Gaming Assets Hub",
                fontSize = 14.sp,
                color = White.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // === INPUTS (PAKAI KOMPONEN BARU) ===
            ClipVaultInput(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ClipVaultInput(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                visualTransformation = if (isPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = MediumGray
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // === TOMBOL LOGIN (DENGAN GRADIENT) ===
            ClipVaultButton(
                text = "MASUK SEKARANG",
                isLoading = isLoading,
                gradient = true, // Gradient aktif
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Isi email dan password!", Toast.LENGTH_SHORT).show()
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        val request = LoginRequest(email, password)

                        RetrofitClient.instance.loginUser(request).enqueue(object : Callback<AuthResponse> {
                            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                isLoading = false
                                val body = response.body()

                                if (response.isSuccessful && body != null && !body.error) {
                                    Toast.makeText(context, "Login Berhasil! 🎮", Toast.LENGTH_SHORT).show()
                                    body.token?.let { SessionManager.saveToken(it) }
                                    SessionManager.saveUserId(body.user?.id ?: 0)
                                    onLoginSuccess(body.user?.id ?: 0)
                                } else {
                                    val errorMsg = try {
                                        val gson = Gson()
                                        val err = gson.fromJson(
                                            response.errorBody()?.string(),
                                            AuthResponse::class.java
                                        )
                                        err.message
                                    } catch (e: Exception) { "Login Gagal" }

                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                isLoading = false
                                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === TOMBOL DAFTAR ===
            ClipVaultOutlinedButton(
                text = "BELUM PUNYA AKUN? DAFTAR",
                onClick = onNavigateToRegister
            )
        }
    }
}