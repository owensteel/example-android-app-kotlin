package com.owensteel.starlingroundup

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.token.TokenManager
import com.owensteel.starlingroundup.ui.MainScreen
import com.owensteel.starlingroundup.ui.theme.StarlingRoundupTheme
import com.owensteel.starlingroundup.util.DeviceSecurityCheck
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

        // Perform device security check immediately
        // Even in debug mode, so there is no single point
        // of failure here
        if(DeviceSecurityCheck.isCompromised(this)) {
            Toast.makeText(this, "This device is not secure. Exiting.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // API setup
        val authApi = StarlingService.createAuthApi()
        val tokenManager = TokenManager(applicationContext, authApi)

        // App setup
        enableEdgeToEdge()
        setContent {
            StarlingRoundupTheme {
                MainScreen(viewModel)
            }
        }
    }
}