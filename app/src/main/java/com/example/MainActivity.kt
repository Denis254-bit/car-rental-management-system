package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.example.data.AppDatabase
import com.example.data.CarRepository
import com.example.ui.CarDashboardScreen
import com.example.ui.CarViewModel
import com.example.ui.ViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core data structures
        val database = AppDatabase.getInstance(applicationContext, lifecycleScope)
        val repository = CarRepository(database.carDao, FirebaseFirestore.getInstance())
        
        // Instant factory construct
        val viewModel: CarViewModel by viewModels {
            ViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    CarDashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
