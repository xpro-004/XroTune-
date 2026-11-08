package com.xrotune.music

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xrotune.music.data.player.PlaybackService
import com.xrotune.music.ui.components.MiniPlayer
import com.xrotune.music.ui.components.XroBottomBar
import com.xrotune.music.ui.navigation.BottomTab
import com.xrotune.music.ui.navigation.Routes
import com.xrotune.music.ui.navigation.XroTuneNavHost
import com.xrotune.music.ui.theme.XroTuneTheme
import com.xrotune.music.ui.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            XroTuneTheme {
                XroTuneRoot()
            }
        }
    }
}

private val mainRoutes = setOf(Routes.HOME, Routes.SEARCH, Routes.LIBRARY, Routes.PROFILE)

@Composable
private fun XroTuneRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showChrome = currentRoute in mainRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showChrome) {
                Column {
                    MiniPlayerHost(navController)
                    XroBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { tab -> navController.switchTab(tab) },
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            XroTuneNavHost(navController)
        }
    }
}

@Composable
private fun MiniPlayerHost(navController: NavHostController) {
    val context = LocalContext.current
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val song by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val position by playerViewModel.position.collectAsState()
    val duration by playerViewModel.duration.collectAsState()

    // Bring up the MediaSession service as soon as something is loaded, so playback
    // and the MediaStyle notification survive leaving the app. The app is in the
    // foreground here, so a plain start is allowed; MediaSessionService promotes
    // itself to a foreground service once playback begins.
    LaunchedEffect(song?.id) {
        if (song != null) {
            val intent = Intent(context, PlaybackService::class.java)
            runCatching { context.startService(intent) }
        }
    }

    val current = song ?: return
    MiniPlayer(
        song = current,
        isPlaying = isPlaying,
        progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
        onClick = { navController.navigate(Routes.PLAYER) },
        onPlayPause = { playerViewModel.togglePlayPause() },
        onNext = { playerViewModel.next() },
    )
}

private fun NavHostController.switchTab(tab: BottomTab) {
    navigate(tab.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
