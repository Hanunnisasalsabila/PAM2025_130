package com.clipvault.clipvault.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.delay
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
    var isVisible by remember { mutableStateOf(false) }

    // === ANIMATED ENTRANCE ===
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // === INFINITE ANIMATIONS FOR LOGO ===
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val logoRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    ClipVaultGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // === ANIMATED LOGO WITH GLOW EFFECT ===
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(800))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp)
                ) {
                    // Outer glow
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(logoScale)
                            .alpha(glowAlpha * 0.5f)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )

                    // Logo with animations
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(140.dp)
                            .scale(logoScale)
                            .rotate(logoRotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === ANIMATED TITLE ===
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                        slideInVertically(
                            initialOffsetY = { -50 },
                            animationSpec = tween(600, delayMillis = 300)
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // === ANIMATED FORM FIELDS (STAGGERED) ===
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 500)) +
                        slideInHorizontally(
                            initialOffsetX = { 100 },
                            animationSpec = tween(500, delayMillis = 500)
                        )
            ) {
                ClipVaultInput(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 650)) +
                        slideInHorizontally(
                            initialOffsetX = { 100 },
                            animationSpec = tween(500, delayMillis = 650)
                        )
            ) {
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
            }

            Spacer(modifier = Modifier.height(32.dp))

            // === ANIMATED BUTTONS ===
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 800)) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
            ) {
                Column {
                    ClipVaultButton(
                        text = "MASUK SEKARANG",
                        isLoading = isLoading,
                        gradient = true,
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

                    ClipVaultOutlinedButton(
                        text = "BELUM PUNYA AKUN? DAFTAR",
                        onClick = onNavigateToRegister
                    )
                }
            }
        }
    }
}