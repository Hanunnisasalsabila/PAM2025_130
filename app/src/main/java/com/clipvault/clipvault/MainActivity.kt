package com.clipvault.clipvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.clipvault.clipvault.ui.screens.*
import com.clipvault.clipvault.ui.theme.ClipVaultTheme
import com.clipvault.clipvault.utils.SessionManager // Pastikan Import ini ada
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext) // Init Session Manager

        setContent {
            ClipVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Start Destination dimulai dari Splash
    NavHost(navController = navController, startDestination = "splash") {

        // 1. SPLASH SCREEN
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    // === LOGIKA BARU: CEK SESI USER ===
                    val token = SessionManager.getToken()
                    val userId = SessionManager.getUserId()

                    if (!token.isNullOrEmpty() && userId != 0) {
                        // KASUS 1: SUDAH LOGIN -> Langsung ke MainScreen
                        navController.navigate("main/$userId") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        // KASUS 2: BELUM LOGIN -> Ke Onboarding
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. ONBOARDING (Updated: Ada 2 Pilihan Login/Register)
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 3. LOGIN
        composable("login") {
            LoginScreen(
                onLoginSuccess = { userId ->
                    // Masuk ke Main Screen (Bawa User ID)
                    navController.navigate("main/$userId") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 4. REGISTER
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") // Sukses daftar -> Login
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        // 5. MAIN SCREEN
        composable(
            route = "main/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            MainScreen(
                userId = userId,
                onLogout = {
                    // SAAT LOGOUT: HAPUS SESI
                    SessionManager.clearSession()

                    // Balik ke Login
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true } // Hapus semua stack activity sebelumnya
                    }
                },
                onEditProfileClick = {
                    // Navigasi dilakukan oleh MainActivity
                    navController.navigate("edit-profile/$userId")
                },
                onVideoClick = { id, path, title, desc, username, size, photo, tags ->
                    val encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8.toString())
                    val safeDesc = if (desc.isEmpty()) "No Description" else URLEncoder.encode(desc, StandardCharsets.UTF_8.toString())
                    val safePhoto = if (photo.isEmpty() || photo == "no_photo") "no_photo"
                    else URLEncoder.encode(photo, StandardCharsets.UTF_8.toString())
                    val safeTags = if (tags.isEmpty()) " " else URLEncoder.encode(tags, StandardCharsets.UTF_8.toString())

                    navController.navigate("detail/$id/$encodedPath/$title/$safeDesc/$username/$size/$safePhoto/$safeTags")
                }
            )
        }

        // 6. DETAIL VIDEO (PLAYER) - Tetap Top Level biar nutupin navbar
        composable(
            route = "detail/{assetId}/{videoUrl}/{title}/{description}/{username}/{fileSize}/{photoUrl}/{tags}",
            arguments = listOf(
                navArgument("assetId") { type = NavType.IntType },
                navArgument("videoUrl") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType },
                navArgument("fileSize") { type = NavType.LongType },
                navArgument("photoUrl") { type = NavType.StringType },
                navArgument("tags") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            // ===DECODE DESKRIPSI & TAGS===
            val rawDesc = args?.getString("description") ?: ""
            val rawTags = args?.getString("tags") ?: ""

            val decodedDesc = try {
                URLDecoder.decode(rawDesc, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) { rawDesc }

            val decodedTags = try {
                URLDecoder.decode(rawTags, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) { rawTags }
            DetailScreen(
                assetId = args?.getInt("assetId") ?: 0,
                videoUrl = args?.getString("videoUrl") ?: "",
                title = args?.getString("title") ?: "",
                description = decodedDesc,
                username = args?.getString("username") ?: "Unknown",
                fileSize = args?.getLong("fileSize") ?: 0L,
                userPhoto = args?.getString("photoUrl") ?: "",
                tags = decodedTags,
                onBack = { navController.popBackStack() }
            )
        }

        // 7. EDIT PROFILE
        composable(
            route = "edit-profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            EditProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onSuccessUpdate = { navController.popBackStack() }
            )
        }
    }
}