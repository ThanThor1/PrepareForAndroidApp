package com.example.testmapbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testmapbox.data.repository.MapRepository
import com.example.testmapbox.data.service.GeocodingService
import com.example.testmapbox.di.NetworkModule
import com.example.testmapbox.ui.screen.MapScreen
import com.example.testmapbox.ui.viewmodel.MapViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup dependencies
        val accessToken = resources.getString(R.string.mapbox_access_token)
        val httpClient = NetworkModule.provideHttpClient()
        val geocodingService = GeocodingService(httpClient, accessToken)
        val repository = MapRepository(geocodingService)
        val viewModel = MapViewModel(repository)

        setContent {
            MapboxAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MapboxAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}