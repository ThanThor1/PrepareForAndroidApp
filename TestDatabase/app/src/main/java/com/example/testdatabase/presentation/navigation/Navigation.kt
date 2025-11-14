package com.example.testdatabase.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.testdatabase.presentation.screens.UserDetailScreen
import com.example.testdatabase.presentation.screens.UserFormScreen
import com.example.testdatabase.presentation.screens.UserListScreen
import com.example.testdatabase.presentation.viewmodel.UserViewModel

sealed class Screen(val route: String) {
    object UserList : Screen("user_list")
    object UserForm : Screen("user_form?userId={userId}") {
        fun createRoute(userId: String? = null) =
            if (userId != null) "user_form?userId=$userId" else "user_form"
    }
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: UserViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.UserList.route
    ) {
        composable(Screen.UserList.route) {
            UserListScreen(
                viewModel = viewModel,
                onNavigateToDetail = { userId ->
                    navController.navigate(Screen.UserDetail.createRoute(userId))
                },
                onNavigateToForm = { userId ->
                    navController.navigate(Screen.UserForm.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.UserForm.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            UserFormScreen(
                viewModel = viewModel,
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(
                viewModel = viewModel,
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.UserForm.createRoute(id))
                }
            )
        }
    }
}