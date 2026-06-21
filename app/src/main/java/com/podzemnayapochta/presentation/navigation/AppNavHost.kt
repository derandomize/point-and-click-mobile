package com.podzemnayapochta.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.podzemnayapochta.presentation.game.GameViewModel
import com.podzemnayapochta.presentation.location.LocationRoute
import com.podzemnayapochta.presentation.map.MapRoute
import com.podzemnayapochta.presentation.menu.MenuScreen

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
            MenuScreen(onStartGame = { navController.navigate(GAME_GRAPH) })
        }
        gameGraph(navController)
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
