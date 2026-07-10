package com.example.nipo.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nipo.ui.auth.NicknameScreen
import com.example.nipo.ui.common.BottomTabItem
import com.example.nipo.ui.common.ScreenWithTabBar
import com.example.nipo.ui.filter.FilterScreen
import com.example.nipo.ui.home.HomeScreen
import com.example.nipo.ui.login.LoginScreen
import com.example.nipo.ui.mypage.MyPageScreen
import com.example.nipo.ui.permission.LocationPermissionScreen
import com.example.nipo.ui.postcreate.CreatePostScreen
import com.example.nipo.ui.postcreate.MapPickerScreen
import com.example.nipo.ui.postdetail.PostDetailScreen
import com.example.nipo.ui.settings.SettingsScreen
import com.example.nipo.ui.soscreate.CreateSosScreen
import com.example.nipo.ui.sosdetail.SosDetailScreen
import com.example.nipo.ui.splash.SplashDestination
import com.example.nipo.ui.splash.SplashScreen
import com.google.android.libraries.places.api.Places
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    fun tabItems(currentRoute: String?) = listOf(
        BottomTabItem(
            label = "ホーム",
            icon = Icons.Default.Home,
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
        ),
        BottomTabItem(
            label = "マイページ",
            icon = Icons.Default.Person,
            selected = currentRoute == "myPage",
            onClick = {
                navController.navigate("myPage") {
                    popUpTo("home")
                }
            },
        ),
    )

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(220)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(220)) },
        ) {
            composable("splash") {
                SplashScreen(onResolved = { destination ->
                    val route = when (destination) {
                        SplashDestination.LocationPermission -> "locationPermission"
                        SplashDestination.Login -> "login"
                        SplashDestination.Nickname -> "nickname"
                        SplashDestination.Home -> "home"
                    }
                    navController.navigate(route) { popUpTo("splash") { inclusive = true } }
                })
            }
            composable("locationPermission") {
                LocationPermissionScreen(onFinished = {
                    navController.navigate("login") { popUpTo("locationPermission") { inclusive = true } }
                })
            }
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate("nickname") { popUpTo("login") { inclusive = true } }
                })
            }
            composable("nickname") {
                NicknameScreen(onFinished = {
                    navController.navigate("home") { popUpTo("nickname") { inclusive = true } }
                })
            }
            composable("home") {
                ScreenWithTabBar(tabItems = tabItems("home")) {
                    HomeScreen(
                        onCreatePost = { navController.navigate("createPost") },
                        onOpenPost = { postId -> navController.navigate("postDetail/$postId") },
                        onCreateSos = { navController.navigate("createSos") },
                        onOpenSos = { sosId -> navController.navigate("sosDetail/$sosId") },
                        onOpenFilter = { navController.navigate("filter") },
                        navController = navController,
                    )
                }
            }
            composable("filter") {
                FilterScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("myPage") {
                ScreenWithTabBar(tabItems = tabItems("myPage")) {
                    MyPageScreen(
                        onOpenSettings = { navController.navigate("settings") },
                        onLoggedOut = {
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        },
                        onOpenPost = { postId -> navController.navigate("postDetail/$postId") },
                        onOpenSos = { sosId -> navController.navigate("sosDetail/$sosId") },
                    )
                }
            }
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLoggedOut = {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                )
            }
            composable("createPost") {
                CreatePostScreen(
                    onDone = { navController.popBackStack() },
                    onPickLocation = { navController.navigate("mapPicker") },
                    navController = navController,
                )
            }
            composable(
                "editPost/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry3 ->
                val postId = backStackEntry3.arguments?.getString("postId") ?: return@composable
                CreatePostScreen(
                    onDone = { navController.popBackStack() },
                    onPickLocation = { navController.navigate("mapPicker") },
                    navController = navController,
                    editingPostId = postId,
                )
            }
            composable("mapPicker") {
                MapPickerScreen(
                    placesClient = placesClient,
                    onLocationPicked = { latLng, name ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("pickedLat", latLng.latitude)
                        navController.previousBackStackEntry?.savedStateHandle?.set("pickedLng", latLng.longitude)
                        navController.previousBackStackEntry?.savedStateHandle?.set("pickedPlaceName", name)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(
                "postDetail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry2 ->
                val postId = backStackEntry2.arguments?.getString("postId") ?: return@composable
                PostDetailScreen(
                    postId = postId,
                    onBack = { post ->
                        post?.geo?.let {
                            navController.previousBackStackEntry?.savedStateHandle?.set("focusLat", it.latitude)
                            navController.previousBackStackEntry?.savedStateHandle?.set("focusLng", it.longitude)
                        }
                        navController.popBackStack()
                    },
                    onEdit = { navController.navigate("editPost/$postId") },
                    onDeleted = { navController.popBackStack() },
                )
            }
            composable("createSos") {
                CreateSosScreen(onDone = { navController.popBackStack() })
            }
            composable(
                "editSos/{sosId}",
                arguments = listOf(navArgument("sosId") { type = NavType.StringType })
            ) { backStackEntry5 ->
                val sosId = backStackEntry5.arguments?.getString("sosId") ?: return@composable
                CreateSosScreen(
                    onDone = { navController.popBackStack() },
                    editingSosId = sosId,
                )
            }
            composable(
                "sosDetail/{sosId}",
                arguments = listOf(navArgument("sosId") { type = NavType.StringType })
            ) { backStackEntry4 ->
                val sosId = backStackEntry4.arguments?.getString("sosId") ?: return@composable
                SosDetailScreen(
                    sosId = sosId,
                    onBack = { sos ->
                        sos?.location?.let {
                            navController.previousBackStackEntry?.savedStateHandle?.set("focusLat", it.latitude)
                            navController.previousBackStackEntry?.savedStateHandle?.set("focusLng", it.longitude)
                        }
                        navController.popBackStack()
                    },
                    onEdit = { navController.navigate("editSos/$sosId") },
                    onDeleted = { navController.popBackStack() },
                )
            }
        }
    }
}
