package com.bevietnam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.bevietnam.ui.navigation.AppNavHost
import com.bevietnam.ui.theme.BeVietnamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeVietnamTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
