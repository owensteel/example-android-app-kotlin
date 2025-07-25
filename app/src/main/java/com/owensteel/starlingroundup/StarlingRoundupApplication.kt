package com.owensteel.starlingroundup

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/*

    Runs before the first activity and persists
    as long as the app is running in memory
    Hilt uses this as the entry point for setting
    up dependency graphs

 */

@HiltAndroidApp
class StarlingRoundupApplication: Application() {
}