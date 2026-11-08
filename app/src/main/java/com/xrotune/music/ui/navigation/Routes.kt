package com.xrotune.music.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val PROFILE = "profile"
    const val DOWNLOADS = "downloads"
    const val PLAYLISTS = "playlists"
    const val LOGIN = "login"
    const val SETTINGS = "settings"
    const val PLAYER = "player"

    const val DETAIL = "detail/{songId}"
    fun detail(songId: String) = "detail/$songId"

    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    fun playlistDetail(id: Long) = "playlist/$id"
}

enum class BottomTab(val route: String, val label: String) {
    HOME(Routes.HOME, "Home"),
    SEARCH(Routes.SEARCH, "Search"),
    LIBRARY(Routes.LIBRARY, "Library"),
    PROFILE(Routes.PROFILE, "Profile"),
}
