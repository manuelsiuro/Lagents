package com.msa.lagents

import android.app.Application
import com.msa.lagents.core.di.AppContainer

class LagentsApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
