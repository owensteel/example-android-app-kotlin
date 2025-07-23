package com.owensteel.starlingroundup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.owensteel.starlingroundup.ui.MainScreen
import com.owensteel.starlingroundup.ui.theme.StarlingRoundupTheme
import com.owensteel.starlingroundup.viewmodel.MainViewModel

/*

    The entry point for the application
    Since in this project we only want to fulfil one function for
    the user, this directly presents our single UI feature

 */
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarlingRoundupTheme {
                MainScreen(viewModel)
            }
        }
    }
}