package com.abh80.smartedge.flashalert

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.abh80.smartedge.flashalert.db.AppDatabase
import com.abh80.smartedge.flashalert.tools.ThemeSelector



class FlashAlert: Application() {
    companion object {
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        ThemeSelector.setTheme(this)
        AppDatabase.getAppDataBase(this)
        DynamicColors.applyToActivitiesIfAvailable(this)

    }
}