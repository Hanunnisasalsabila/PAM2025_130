package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clipvault.clipvault.ui.components.ClipVaultButton
import com.clipvault.clipvault.ui.components.ClipVaultGradientBackground
import com.clipvault.clipvault.ui.components.ClipVaultInput
import com.clipvault.clipvault.ui.theme.*
import com.clipvault.clipvault.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // States
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Animasi Fade In untuk Form
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    ClipVaultGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // === JUDUL ANIMATED ===
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(initialOffsetY = { -40 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Daftar Akun Baru",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mulai petualangan gaming-mu!",
                        fontSize = 14.sp,
                        color = White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // === FORM FIELDS (Animasi Staggered) ===
            val fields = listOf(
                "fullName" to fullName,
                "username" to username,
                "email" to email,
                "password" to password,
                "confirmPassword" to confirmPassword
            )

            fields.forEachIndexed { index, (fieldName, fieldValue) ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = 100 * (index + 1)
                        )
                    ) + slideInHorizontally(
                        initialOffsetX = { 100 },
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = 100 * (index + 1)
                        )
                    )
                ) {
                    when (fieldName) {
                        "fullName" -> ClipVaultInput(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "Nama Lengkap",
                            enabled = !isLoading
                        )
                        "username" -> ClipVaultInput(
                            value = username,
                            onValueChange = { username = it },
                            label = "Username",
                            enabled = !isLoading
                        )
                        "email" -> ClipVaultInput(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !isLoading
                        )
                        "password" -> ClipVaultInput(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            visualTransformation = if (isPasswordVisible)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !isLoading,
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible)
                                            Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = White
                                    )
                                }
                            }
                        )
                        "confirmPassword" -> ClipVaultInput(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Ulangi Password",
                            visualTransformation = if (isConfirmPasswordVisible)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !isLoading,
                            trailingIcon = {
                                IconButton(onClick = {
                                    isConfirmPasswordVisible = !isConfirmPasswordVisible
                                }) {
                                    Icon(
                                        imageVector = if (isConfirmPasswordVisible)
                                            Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = White
                                    )
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === TOMBOL DAFTAR ===
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 700)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(600, delayMillis = 700))
            ) {
                Column {
                    ClipVaultButton(
                        text = if (isLoading) "MEMPROSES..." else "DAFTAR SEKARANG",
                        isLoading = isLoading,
                        gradient = true,
                        onClick = {
                            // Validasi
                            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() ||
                                password.isEmpty() || confirmPassword.isEmpty()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Mohon lengkapi semua data!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (username.length < 3) {
                                Toast.makeText(
                                    context,
                                    "Username minimal 3 karakter!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                                    .matches()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Format email tidak valid!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (password.length < 8) {
                                Toast.makeText(
                                    context,
                                    "Password minimal 8 karakter!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (password != confirmPassword) {
                                Toast.makeText(
                                    context,
                                    "Konfirmasi password tidak cocok!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                isLoading = true
                                authViewModel.register(
                                    username, email, password, fullName, context,
                                    onSuccess = {
                                        isLoading = false
                                        onRegisterSuccess()
                                    },
                                    onError = { errorMsg ->
                                        isLoading = false
                                        errorMessage = errorMsg
                                        showErrorDialog = true
                                    }
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onNavigateToLogin,
                        enabled = !isLoading
                    ) {
                        Text(
                            "Sudah punya akun? Masuk",
                            color = White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }

    // === ERROR DIALOG ===
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Gagal Daftar") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue)
                ) {
                    Text("Coba Lagi")
                }
            }
        )
    }
}