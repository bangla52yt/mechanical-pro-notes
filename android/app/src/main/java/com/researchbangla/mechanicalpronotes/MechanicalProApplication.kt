package com.researchbangla.mechanicalpronotes

import android.app.Application
import com.researchbangla.mechanicalpronotes.di.AppContainer
import com.researchbangla.mechanicalpronotes.di.DefaultAppContainer

class MechanicalProApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
