package com.clipvault.clipvault.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(
    userId: Int,
    onLogout: () -> Unit,
    // UPDATE BAGIAN INI (Jadi 7 Parameter)
    onVideoClick: (Int, String, String, String, String, Long, String, String) -> Unit,
    onEditProfileClick: () -> Unit
) {
    val navController = rememberNavController()

    // Daftar Menu Bawah
    val items = listOf(
        BottomNavItem("Home", "home", Icons.Default.Home),
        BottomNavItem("Search", "search", Icons.Default.Search),
        BottomNavItem("Upload", "upload", Icons.Default.AddCircle),
        BottomNavItem("Profile", "profile", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF00897B)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00897B),
                            selectedTextColor = Color(0xFF00897B),
                            indicatorColor = Color(0xFF00897B).copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // === 1. HOME SCREEN ===
            composable("home") {
                HomeScreen(
                    userId = userId, // PERBAIKAN: Masukkan userId
                    onVideoClick = onVideoClick,
                    // Tombol-tombol di dalam Home kita sambungkan ke Tab Bawah
                    onNavigateToUpload = { navController.navigate("upload") },
                    onSearchClick = { navController.navigate("search") },
                    onProfileClick = { navController.navigate("profile") }
                )
            }

            // === 2. SEARCH SCREEN ===
            composable("search") {
                SearchScreen(
                    onBack = { navController.navigate("home") },
                    onVideoClick = onVideoClick // Teruskan Function7
                )
            }

            // === 3. UPLOAD SCREEN (Tidak ada perubahan) ===
            composable("upload") {
                UploadScreen(
                    userId = userId,
                    onUploadSuccess = { navController.navigate("home") }
                )
            }

            // === 4. PROFILE SCREEN ===
            composable("profile") {
                ProfileScreen(
                    userId = userId,
                    onBack = { },
                    onVideoClick = onVideoClick, // Teruskan Function7
                    onEditClick = onEditProfileClick,
                    onLogout = onLogout
                )
            }
        }
    }
}
data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)