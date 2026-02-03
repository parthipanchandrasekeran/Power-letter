package com.powerletter.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.powerletter.domain.model.LetterType
import com.powerletter.ui.form.FormViewModel
import com.powerletter.ui.form.LetterFormScreen
import com.powerletter.ui.home.HomeScreen
import com.powerletter.ui.payment.PaymentScreen
import com.powerletter.ui.payment.PaymentViewModel
import com.powerletter.ui.result.LetterResultScreen
import com.powerletter.ui.result.ResultViewModel
import com.powerletter.ui.settings.SettingsScreen
import com.powerletter.ui.settings.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Shared ViewModels for form -> payment -> result flow
    val formViewModel: FormViewModel = viewModel()
    val paymentViewModel: PaymentViewModel = viewModel()
    val resultViewModel: ResultViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Route.Home.path
    ) {
        composable(Route.Home.path) {
            HomeScreen(
                onLetterTypeSelected = { letterType ->
                    navController.navigate(Route.Form.createPath(letterType.name))
                },
                onSettingsClick = {
                    navController.navigate(Route.Settings.path)
                }
            )
        }

        composable(Route.Settings.path) {
            val settingsViewModel: SettingsViewModel = viewModel()

            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUpgradeClick = {
                    // Navigate to payment screen for upgrade
                    navController.navigate(Route.Payment.path)
                },
                onRestoreClick = { onResult ->
                    paymentViewModel.billingManager.restorePurchases(onResult)
                },
                settingsViewModel = settingsViewModel
            )
        }

        composable(
            route = Route.Form.path,
            arguments = listOf(
                navArgument("letterType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val letterTypeName = backStackEntry.arguments?.getString("letterType") ?: return@composable
            val letterType = remember(letterTypeName) {
                LetterType.valueOf(letterTypeName)
            }

            LetterFormScreen(
                letterType = letterType,
                viewModel = formViewModel,
                onNavigateBack = {
                    formViewModel.reset()
                    navController.popBackStack()
                },
                onGenerateClick = {
                    // Navigate to payment screen
                    navController.navigate(Route.Payment.path)
                }
            )
        }

        composable(Route.Payment.path) {
            PaymentScreen(
                viewModel = paymentViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPaymentComplete = {
                    // Payment successful - now generate the letter
                    val request = formViewModel.toLetterRequest()
                    if (request != null) {
                        resultViewModel.generateLetter(request)
                        // Navigate to result and remove payment from back stack
                        navController.navigate(Route.Result.path) {
                            popUpTo(Route.Payment.path) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Route.Result.path) {
            LetterResultScreen(
                viewModel = resultViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNewLetter = {
                    formViewModel.reset()
                    resultViewModel.reset()
                    paymentViewModel.resetState()
                    navController.popBackStack(Route.Home.path, inclusive = false)
                }
            )
        }
    }
}
