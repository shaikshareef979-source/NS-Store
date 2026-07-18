package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ECommerceRepository
import com.example.ui.ECommerceViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class ECommerceViewModelFactory(private val repository: ECommerceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ECommerceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ECommerceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize offline Room Database
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ECommerceRepository(database.eCommerceDao())
        val factory = ECommerceViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                // Instantiate the unified State Machine ViewModel with our custom Factory
                val viewModel: ECommerceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
