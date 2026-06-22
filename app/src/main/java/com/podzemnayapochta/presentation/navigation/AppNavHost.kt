package com.podzemnayapochta.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.podzemnayapochta.domain.model.Ending
import com.podzemnayapochta.presentation.ending.EndingScreen
import com.podzemnayapochta.presentation.game.GameViewModel
import com.podzemnayapochta.presentation.location.LocationRoute
import com.podzemnayapochta.presentation.map.MapRoute
import com.podzemnayapochta.presentation.menu.MenuScreen
import com.podzemnayapochta.presentation.menu.MenuViewModel

private const val GAME_GRAPH = "game_graph"

/**
 * Граф навигации игры: меню → [игровой граф: карта ↔ локация].
 * Внутри игрового графа [GameViewModel] разделяется между картой и локацией
 * (scoped к backstack-entry графа), чтобы состояние игры было единым.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            val menuViewModel: MenuViewModel = hiltViewModel()
            val hasSave by menuViewModel.hasSave.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { menuViewModel.refreshHasSave() }
            MenuScreen(
                hasSave = hasSave,
                onContinue = { navController.navigate(GAME_GRAPH) },
                onNewGame = { menuViewModel.startNewGame { navController.navigate(GAME_GRAPH) } },
            )
        }
        gameGraph(navController)

        composable(
            route = Routes.ENDING,
            arguments = listOf(navArgument(Routes.ENDING_ARG) { type = NavType.StringType }),
        ) { entry ->
            val endingName = entry.arguments?.getString(Routes.ENDING_ARG)
            val ending =
                endingName
                    ?.let { runCatching { Ending.valueOf(it) }.getOrNull() }
                    ?: Ending.KEEP_SECRET
            EndingScreen(
                ending = ending,
                onBackToMenu = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                },
            )
        }
    }
}

private fun NavGraphBuilder.gameGraph(navController: NavHostController) {
    navigation(startDestination = Routes.MAP, route = GAME_GRAPH) {
        composable(Routes.MAP) { entry ->
            val viewModel = entry.sharedGameViewModel(navController)
            MapRoute(
                viewModel = viewModel,
                onLocationSelected = { locationId ->
                    navController.navigate(Routes.location(locationId))
                },
            )
        }

        composable(
            route = Routes.LOCATION,
            arguments = listOf(navArgument(Routes.LOCATION_ARG) { type = NavType.StringType }),
        ) { entry ->
            val viewModel = entry.sharedGameViewModel(navController)
            val locationId = entry.arguments?.getString(Routes.LOCATION_ARG).orEmpty()
            LocationRoute(
                locationId = locationId,
                viewModel = viewModel,
                onNavigateToLocation = { targetId ->
                    navController.navigate(Routes.location(targetId)) {
                        popUpTo(Routes.MAP)
                    }
                },
                onFinish = { ending ->
                    navController.navigate(Routes.ending(ending.name))
                },
            )
        }
    }
}

/** Возвращает [GameViewModel], общий для всех экранов игрового графа. */
@Composable
private fun androidx.navigation.NavBackStackEntry.sharedGameViewModel(navController: NavHostController): GameViewModel {
    val parentEntry =
        androidx.compose.runtime.remember(this) {
            navController.getBackStackEntry(GAME_GRAPH)
        }
    return hiltViewModel(parentEntry)
}
