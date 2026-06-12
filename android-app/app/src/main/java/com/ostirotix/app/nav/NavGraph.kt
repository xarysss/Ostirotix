package com.ostirotix.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ostirotix.app.ui.screens.HomeScreen
import com.ostirotix.app.ui.screens.LeaderboardScreen
import com.ostirotix.app.ui.screens.LibraryScreen
import com.ostirotix.app.ui.screens.LobbyScreen
import com.ostirotix.app.ui.screens.MultiGameScreen
import com.ostirotix.app.ui.screens.MultiModeScreen
import com.ostirotix.app.ui.screens.ProfileScreen
import com.ostirotix.app.ui.screens.RankedResultScreen
import com.ostirotix.app.ui.screens.ResultScreen
import com.ostirotix.app.ui.screens.SettingsScreen
import com.ostirotix.app.ui.screens.SoloGameScreen
import com.ostirotix.app.vm.AccountViewModel
import com.ostirotix.app.vm.MultiViewModel
import com.ostirotix.app.vm.SoloMode
import com.ostirotix.app.vm.SoloViewModel

object Routes {
    const val HOME = "home"
    const val SOLO_DAILY = "solo/daily"
    const val SOLO_TRAINING = "solo/training"
    const val RESULT = "result"
    const val MULTI = "multi"
    const val LOBBY = "lobby"
    const val MULTI_GAME = "multigame"
    const val RANKED_RESULT = "rankedresult"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val LIBRARY = "library"
}

@Composable
fun OstirotixNavGraph(navController: NavHostController = rememberNavController()) {
    // ViewModels partagés entre écrans (créés au niveau du graphe)
    val soloVm: SoloViewModel = viewModel()
    val multiVm: MultiViewModel = viewModel()
    val accountVm: AccountViewModel = viewModel()

    // L'app s'ouvre directement sur une partie solo (mot du jour), jouable sans compte.
    NavHost(navController = navController, startDestination = Routes.SOLO_DAILY) {

        composable(Routes.HOME) {
            HomeScreen(
                onDaily = { navController.navigate(Routes.SOLO_DAILY) },
                onTraining = { navController.navigate(Routes.SOLO_TRAINING) },
                onMulti = { navController.navigate(Routes.MULTI) },
                onLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onLibrary = { navController.navigate(Routes.LIBRARY) },
                onProfile = { navController.navigate(Routes.PROFILE) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.SOLO_DAILY) {
            LaunchedEffect(Unit) {
                val s = soloVm.state.value
                if (s.secretIndex < 0 || s.mode != SoloMode.DAILY || s.finished) soloVm.start(SoloMode.DAILY)
            }
            SoloGameScreen(
                vm = soloVm,
                onFinished = { navController.navigate(Routes.RESULT) },
                onHome = { navController.navigate(Routes.HOME) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.SOLO_TRAINING) {
            LaunchedEffect(Unit) {
                val s = soloVm.state.value
                if (s.secretIndex < 0 || s.mode != SoloMode.TRAINING || s.finished) soloVm.start(SoloMode.TRAINING)
            }
            SoloGameScreen(
                vm = soloVm,
                onFinished = { navController.navigate(Routes.RESULT) },
                onHome = { navController.navigate(Routes.HOME) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                vm = soloVm,
                onReplay = {
                    soloVm.start(SoloMode.TRAINING)
                    navController.navigate(Routes.SOLO_TRAINING) { popUpTo(Routes.HOME) { inclusive = false } }
                },
                onHome = { navController.navigate(Routes.HOME) { popUpTo(0) } },
            )
        }

        composable(Routes.MULTI) {
            MultiModeScreen(
                vm = multiVm,
                onLobby = { navController.navigate(Routes.LOBBY) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.LOBBY) {
            LobbyScreen(
                vm = multiVm,
                onGameStart = { navController.navigate(Routes.MULTI_GAME) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.MULTI_GAME) {
            MultiGameScreen(
                vm = multiVm,
                onMatchEnd = { navController.navigate(Routes.RANKED_RESULT) },
                onQuit = { navController.navigate(Routes.HOME) { popUpTo(0) } },
            )
        }

        composable(Routes.RANKED_RESULT) {
            RankedResultScreen(
                vm = multiVm,
                onReplay = { navController.navigate(Routes.MULTI) { popUpTo(Routes.HOME) } },
                onHome = { navController.navigate(Routes.HOME) { popUpTo(0) } },
            )
        }

        composable(Routes.LEADERBOARD) { LeaderboardScreen(accountVm) { navController.popBackStack() } }
        composable(Routes.PROFILE) { ProfileScreen(accountVm) { navController.popBackStack() } }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                vm = multiVm,
                onLibrary = { navController.navigate(Routes.LIBRARY) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.LIBRARY) { LibraryScreen { navController.popBackStack() } }
    }
}
