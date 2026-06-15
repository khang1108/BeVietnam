package com.bevietnam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.bevietnam.ui.navigation.AppNavHost
import com.bevietnam.ui.theme.BeVietnamTheme
import dagger.hilt.android.AndroidEntryPoint

import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeVietnamTheme {
                val backendStatus by mainViewModel.backendStatus.collectAsState()

                LaunchedEffect(backendStatus) {
                    backendStatus?.let { result ->
                        result.onSuccess { status ->
                            Toast.makeText(this@MainActivity, "Backend Connected (v${status.version})", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(this@MainActivity, "Backend is offline / Cannot connect", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
