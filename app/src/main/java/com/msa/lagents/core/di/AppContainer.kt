package com.msa.lagents.core.di

import android.content.Context
import androidx.room.Room
import com.msa.lagents.data.local.LagentsDatabase
import com.msa.lagents.data.settings.AppSettingsRepository
import com.msa.lagents.ui.settings.AppSettingsViewModel
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val database: LagentsDatabase = Room.databaseBuilder(
        context = applicationContext,
        klass = LagentsDatabase::class.java,
        name = "lagents.db",
    ).build()

    val appSettingsRepository: AppSettingsRepository = AppSettingsRepository(
        context = applicationContext,
    )

    val appSettingsViewModelFactory: AppSettingsViewModel.Factory =
        AppSettingsViewModel.Factory(appSettingsRepository)
}
