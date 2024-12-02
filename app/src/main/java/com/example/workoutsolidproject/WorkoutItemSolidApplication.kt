package com.example.workoutsolidproject

import com.solidannotations.WorkoutItemDatabase
import com.solidannotations.WorkoutItemRepository
import android.app.Application
import com.example.workoutsolidproject.healthdata.HealthConnectManager

//needed
//val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userData")
class WorkoutItemSolidApplication: Application() {
    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }
    init {
        appInstance = this
    }

    companion object {
        lateinit var appInstance: WorkoutItemSolidApplication
        const val FILE_PATH = "WorkoutItemApplication"
        const val BASE_URI = "https://solidworkout.com"
    }

    private val database by lazy { WorkoutItemDatabase.getDatabase(appInstance, BASE_URI, FILE_PATH) }
    val repository by lazy { WorkoutItemRepository(database.WorkoutItemDao()) }
}