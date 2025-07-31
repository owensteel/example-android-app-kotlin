package com.owensteel.starlingroundup

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.owensteel.starlingroundup.ui.roundup.RoundUpAndSaveScreen
import com.owensteel.starlingroundup.ui.theme.StarlingRoundupTheme
import com.owensteel.starlingroundup.util.DeviceSecurityCheck
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/*

    The entry point for the application
    Since in this project we only want to fulfil one feature for
    the user, this directly presents our single UI feature

 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deviceSecurityCheck: DeviceSecurityCheck

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Perform device security check immediately
        // Even in debug mode, so there is no single point
        // of failure here
        if (deviceSecurityCheck.isCompromised(this)) {
            Toast.makeText(this, "This device is not secure. Exiting.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Single-page application setup
        setContent {
            val navController = rememberNavController()

            StarlingRoundupTheme {
                NavHost(navController, startDestination = "RoundUpAndSave") {
                    composable("RoundUpAndSave") { backStackEntry ->
                        val roundUpAndSaveViewModel: RoundUpAndSaveViewModel =
                            hiltViewModel(backStackEntry)
                        RoundUpAndSaveScreen(
                            viewModel = roundUpAndSaveViewModel
                        )
                    }
                }
            }
        }

    }

}