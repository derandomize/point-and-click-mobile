package com.podzemnayapochta.presentation.navigation

/** Маршруты навигации между экранами (см. docs/architecture.md, app-слой). */
object Routes {
    const val MENU = "menu"
    const val MAP = "map"

    const val LOCATION_ARG = "locationId"
    const val LOCATION = "location/{$LOCATION_ARG}"

    const val ENDING_ARG = "ending"
    const val ENDING = "ending/{$ENDING_ARG}"

    fun location(locationId: String): String = "location/$locationId"

    fun ending(endingName: String): String = "ending/$endingName"
}
