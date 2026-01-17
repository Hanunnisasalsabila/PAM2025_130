package com.clipvault.clipvault.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.clipvault.clipvault.ui.theme.BrightBlue
import com.clipvault.clipvault.ui.theme.White

@Composable
fun MainScreen(
    userId: Int,
    onLogout: () -> Unit,
    onVideoClick: (Int, String, String, String, String, Long, String, String) -> Unit,
    onEditProfileClick: () -> Unit
) {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem("Home", "home", Icons.Default.Home),
        BottomNavItem("Search", "search", Icons.Default.Search),
        BottomNavItem("Upload", "upload", Icons.Default.AddCircle),
        BottomNavItem("Profile", "profile", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = White,
                contentColor = BrightBlue,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "icon_scale_${item.route}"
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.scale(scale)
                            )
                        },
                        label = {
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Text(item.title)
                            }
                        },
                        selected = isSelected,
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
                            selectedIconColor = BrightBlue,
                            selectedTextColor = BrightBlue,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = BrightBlue.copy(alpha = 0.15f)
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
            composable("home") {
                HomeScreen(
                    userId = userId,
                    onVideoClick = onVideoClick,
                    onNavigateToUpload = { navController.navigate("upload") },
                    onSearchClick = { navController.navigate("search") },
                    onProfileClick = { navController.navigate("profile") }
                )
            }

            composable("search") {
                SearchScreen(
                    onBack = { navController.navigate("home") },
                    onVideoClick = onVideoClick
                )
            }

            composable("upload") {
                UploadScreen(
                    userId = userId,
                    onUploadSuccess = { navController.navigate("home") }
                )
            }

            composable("profile") {
                ProfileScreen(
                    userId = userId,
                    onBack = { },
                    onVideoClick = onVideoClick,
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