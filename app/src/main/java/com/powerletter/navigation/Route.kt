package com.powerletter.navigation

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Form : Route("form/{letterType}") {
        fun createPath(letterType: String) = "form/$letterType"
    }
    data object Payment : Route("payment")
    data object Result : Route("result")
    data object Settings : Route("settings")
}
