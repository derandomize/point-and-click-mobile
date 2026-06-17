package com.podzemnayapochta.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.podzemnayapochta.presentation.map.MapScreen
import com.podzemnayapochta.presentation.menu.MenuScreen

/**
 * Граф навигации игры: меню → карта (см. docs/architecture.md, app-слой).
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MenuScreen(onStartGame = { navController.navigate(Routes.MAP) })
        }
        composable(Routes.MAP) {
            MapScreen()
        }
    }
}
