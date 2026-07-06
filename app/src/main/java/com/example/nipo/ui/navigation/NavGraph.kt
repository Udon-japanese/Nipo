package com.example.nipo.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.nipo.ui.login.LoginScreen
import com.example.nipo.ui.postcreate.CreatePostScreen
import com.example.nipo.ui.postdetail.PostDetailScreen
import com.example.nipo.ui.postlist.PostListScreen
import com.example.nipo.ui.settings.SettingsScreen
import com.google.firebase.auth.FirebaseAuth

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomTabs = listOf(
    BottomTab("home", "ホーム", Icons.Default.Home),
    BottomTab("settings", "設定", Icons.Default.Settings)
)

@Composable
fun AppNavGraph(navController: NavHostController) {
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser != null) "home" else "login"

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == "home" || currentRoute == "settings"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                })
            }
            composable("home") {
                PostListScreen(
                    onCreatePost = { navController.navigate("createPost") },
                    onOpenPost = { postId -> navController.navigate("postDetail/$postId") }
                )
            }
            composable("settings") {
                SettingsScreen(onLoggedOut = {
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                })
            }
            composable("createPost") {
                CreatePostScreen(onDone = { navController.popBackStack() })
            }
            composable(
                "postDetail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry2 ->
                val postId = backStackEntry2.arguments?.getString("postId") ?: return@composable
                PostDetailScreen(postId = postId)
            }
        }
    }
}