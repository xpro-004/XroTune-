package com.xrotune.music.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.xrotune.music.ui.screens.detail.SongDetailScreen
import com.xrotune.music.ui.screens.downloads.DownloadManagerScreen
import com.xrotune.music.ui.screens.home.HomeScreen
import com.xrotune.music.ui.screens.library.LibraryScreen
import com.xrotune.music.ui.screens.login.LoginScreen
import com.xrotune.music.ui.screens.player.FullPlayerScreen
import com.xrotune.music.ui.screens.playlist.PlaylistDetailScreen
import com.xrotune.music.ui.screens.playlist.PlaylistsScreen
import com.xrotune.music.ui.screens.profile.ProfileScreen
import com.xrotune.music.ui.screens.search.SearchScreen
import com.xrotune.music.ui.screens.settings.SettingsScreen
import com.xrotune.music.ui.screens.splash.SplashScreen

@Composable
fun XroTuneNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                onSongClick = { navController.navigate(Routes.detail(it)) },
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onPlaylistsClick = { navController.navigate(Routes.PLAYLISTS) },
                onLibraryClick = { navController.navigate(Routes.LIBRARY) },
                onDownloadsClick = { navController.navigate(Routes.DOWNLOADS) },
                onPlayerClick = { navController.navigate(Routes.PLAYER) },
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onSongClick = { navController.navigate(Routes.detail(it)) },
                onPlayerClick = { navController.navigate(Routes.PLAYER) },
            )
        }

        composable(Routes.LIBRARY) {
            LibraryScreen(
                onSongClick = { navController.navigate(Routes.detail(it)) },
                onPlaylistsClick = { navController.navigate(Routes.PLAYLISTS) },
                onDownloadsClick = { navController.navigate(Routes.DOWNLOADS) },
                onPlayerClick = { navController.navigate(Routes.PLAYER) },
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLoginClick = { navController.navigate(Routes.LOGIN) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onPlaylistsClick = { navController.navigate(Routes.PLAYLISTS) },
                onDownloadsClick = { navController.navigate(Routes.DOWNLOADS) },
            )
        }

        composable(Routes.PLAYLISTS) {
            PlaylistsScreen(
                onBack = { navController.popBackStack() },
                onOpenPlaylist = { navController.navigate(Routes.playlistDetail(it)) },
            )
        }

        composable(
            Routes.PLAYLIST_DETAIL,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
        ) { entry ->
            val playlistId = entry.arguments?.getLong("playlistId") ?: 0L
            PlaylistDetailScreen(
                playlistId = playlistId,
                onBack = { navController.popBackStack() },
                onSongClick = { navController.navigate(Routes.detail(it)) },
            )
        }

        composable(Routes.DOWNLOADS) {
            DownloadManagerScreen(
                onBack = { navController.popBackStack() },
                onSongClick = { navController.navigate(Routes.detail(it)) },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoggedIn = { navController.popBackStack() },
            )
        }

        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("songId") { type = NavType.StringType }),
            enterTransition = { slideInVertically(tween(260)) { it / 6 } + fadeIn(tween(260)) },
            exitTransition = { fadeOut(tween(160)) },
        ) { entry ->
            SongDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenPlayer = { navController.navigate(Routes.PLAYER) },
            )
        }

        composable(
            Routes.PLAYER,
            enterTransition = {
                slideInVertically(tween(320)) { (it * 0.25).toInt() } + fadeIn(tween(260))
            },
            exitTransition = { slideOutVertically(tween(280)) { (it * 0.25).toInt() } + fadeOut(tween(200)) },
        ) {
            FullPlayerScreen(onBack = { navController.popBackStack() })
        }
    }
}
