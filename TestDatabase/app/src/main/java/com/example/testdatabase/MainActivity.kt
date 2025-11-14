package com.example.testdatabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.testdatabase.data.repository.ImageUploadRepositoryImpl
import com.example.testdatabase.data.repository.UserRepositoryImpl
import com.example.testdatabase.presentation.navigation.AppNavigation
import com.example.testdatabase.presentation.viewmodel.UserViewModel
import com.example.testdatabase.ui.theme.TestDatabaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userRepository = UserRepositoryImpl()
        val imageRepository = ImageUploadRepositoryImpl()
        val viewModel = UserViewModel(userRepository, imageRepository)

        setContent {
            TestDatabaseTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}