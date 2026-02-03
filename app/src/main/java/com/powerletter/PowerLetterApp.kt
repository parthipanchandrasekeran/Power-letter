package com.powerletter

import androidx.compose.runtime.Composable
import com.powerletter.navigation.NavGraph
import com.powerletter.ui.theme.PowerLetterTheme

@Composable
fun PowerLetterApp() {
    PowerLetterTheme {
        NavGraph()
    }
}
