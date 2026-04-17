package com.example.meetingmemo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.meetingmemo.ui.detail.MemoDetailScreen
import com.example.meetingmemo.ui.home.HomeScreen
import com.example.meetingmemo.ui.importaudio.AudioImportScreen
import com.example.meetingmemo.ui.record.RecordMemoScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
    ) {
        composable(AppDestination.Home.route) {
            HomeScreen(
                onCreateMemo = { navController.navigate(AppDestination.Record.route) },
                onImportAudio = { navController.navigate(AppDestination.AudioImport.route) },
                onOpenMemo = { memoId ->
                    navController.navigate(AppDestination.Detail.createRoute(memoId))
                },
            )
        }

        composable(AppDestination.AudioImport.route) {
            AudioImportScreen(
                onBack = { navController.popBackStack() },
                onImportCompleted = {
                    navController.navigate(AppDestination.Record.route) {
                        popUpTo(AppDestination.AudioImport.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(AppDestination.Record.route) {
            RecordMemoScreen(
                onBack = { navController.popBackStack() },
                onSaved = { memoId ->
                    navController.navigate(AppDestination.Detail.createRoute(memoId)) {
                        popUpTo(AppDestination.Home.route)
                    }
                },
            )
        }

        composable(
            route = AppDestination.Detail.route,
            arguments = listOf(navArgument("memoId") { type = NavType.LongType }),
        ) {
            MemoDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
