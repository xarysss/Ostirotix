package com.ostirotix.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.data.auth.AuthMode
import com.ostirotix.app.ui.screens.AuthScreen
import com.ostirotix.app.ui.screens.GrimoireTutorialScreen
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
import com.ostirotix.app.ui.screens.ShopTab
import com.ostirotix.app.ui.screens.SoloGameScreen
import com.ostirotix.app.vm.AccountViewModel
import com.ostirotix.app.vm.AuthViewModel
import com.ostirotix.app.vm.MultiViewModel
import com.ostirotix.app.vm.SoloMode
import com.ostirotix.app.vm.SoloViewModel

object Routes {
    const val HOME = "home"
    const val GRIMOIRE_TUTORIAL = "grimoire/tutorial"
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
    const val AUTH = "auth"
    const val LIBRARY = "library"
    const val LIBRARY_TAB = "library/{tab}"
}

@Composable
fun OstirotixNavGraph(navController: NavHostController = rememberNavController()) {
    // ViewModels partagés entre écrans (créés au niveau du graphe)
    val soloVm: SoloViewModel = viewModel()
    val multiVm: MultiViewModel = viewModel()
    val accountVm: AccountViewModel = viewModel()
    val authVm: AuthViewModel = viewModel()

    fun requireAuth(target: String, message: String, mode: AuthMode = AuthMode.LOGIN) {
        if (ServiceLocator.auth.isAuthenticated()) {
            navController.navigate(target)
        } else {
            ServiceLocator.auth.requireAuth(target, message, mode)
            navController.navigate(Routes.AUTH)
        }
    }

    // L'app s'ouvre directement sur une partie solo (mot du jour), jouable sans compte.
    NavHost(navController = navController, startDestination = Routes.SOLO_DAILY) {

        composable(Routes.HOME) {
            HomeScreen(
                onDaily = { navController.navigate(Routes.SOLO_DAILY) },
                onGrimoire = { navController.navigate(Routes.GRIMOIRE_TUTORIAL) },
                onTraining = { navController.navigate(Routes.SOLO_TRAINING) },
                onMulti = {
                    requireAuth(
                        Routes.MULTI,
                        "Connecte-toi pour accéder au duel lexical et protéger ton classement.",
                    )
                },
                onLeaderboard = {
                    requireAuth(
                        Routes.LEADERBOARD,
                        "Connecte-toi pour accéder au classement et retrouver ton registre.",
                    )
                },
                onLibrary = { navController.navigate(Routes.LIBRARY) },
                onTreasury = { navController.navigate("library/${ShopTab.TREASURY.route}") },
                onProfile = {
                    requireAuth(
                        Routes.PROFILE,
                        "Connecte-toi pour accéder à ton profil en ligne.",
                    )
                },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.AUTH) {
            val initialMode = ServiceLocator.auth.pendingMode
            val message = ServiceLocator.auth.pendingMessage
            AuthScreen(
                vm = authVm,
                initialMode = initialMode,
                message = message,
                onBack = { navController.popBackStack() },
                onAuthenticated = {
                    multiVm.syncAccount()
                    val target = ServiceLocator.auth.consumePendingTarget()
                    if (target != null) {
                        navController.navigate(target) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
            )
        }

        composable(Routes.GRIMOIRE_TUTORIAL) {
            GrimoireTutorialScreen(
                onClose = { navController.popBackStack() },
                onStartDaily = {
                    navController.navigate(Routes.SOLO_DAILY) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
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
                onRequireAuth = {
                    requireAuth(
                        Routes.MULTI,
                        "Connecte-toi pour accéder au duel lexical.",
                    )
                },
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
                onAuth = { mode ->
                    ServiceLocator.auth.requireAuth(
                        Routes.SETTINGS,
                        "Connecte-toi pour accéder aux fonctionnalités liées au compte.",
                        mode,
                    )
                    navController.navigate(Routes.AUTH)
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.LIBRARY) {
            LibraryScreen(
                initialTab = ShopTab.LIBRARY,
                onRequireAuth = {
                    ServiceLocator.auth.requireAuth(
                        Routes.LIBRARY,
                        "Connecte-toi pour sécuriser tes achats et retrouver tes ressources sur ton compte.",
                    )
                    navController.navigate(Routes.AUTH)
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.LIBRARY_TAB) { entry ->
            val tab = ShopTab.fromRoute(entry.arguments?.getString("tab"))
            LibraryScreen(
                initialTab = tab,
                onRequireAuth = {
                    ServiceLocator.auth.requireAuth(
                        "library/${tab.route}",
                        "Connecte-toi pour sécuriser tes achats et retrouver tes ressources sur ton compte.",
                    )
                    navController.navigate(Routes.AUTH)
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
