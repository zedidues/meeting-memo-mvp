package com.example.meetingmemo.navigation

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object Record : AppDestination("record")
    data object AudioImport : AppDestination("audio-import")
    data object Detail : AppDestination("detail/{memoId}") {
        fun createRoute(memoId: Long): String = "detail/$memoId"
    }
}
