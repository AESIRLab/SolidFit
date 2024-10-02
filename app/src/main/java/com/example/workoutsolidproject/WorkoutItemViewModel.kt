package com.example.workoutsolidproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
//import com.example.workoutroomproject.WorkoutItemRepository
import com.example.workoutsolidproject.model.WorkoutItem
import com.solidannotations.WorkoutItemRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

class WorkoutItemViewModel(private val repository: WorkoutItemRepository): ViewModel() {
    val allItems = repository.allWorkoutItems.asLiveData()

    lateinit var curWorkoutItem: LiveData<WorkoutItem>

    private var updatedCounter = MutableLiveData(0)

    fun setUri(uri: String) {
        val workoutLiveData = repository.getWorkoutItemLiveData(uri)
        curWorkoutItem = workoutLiveData.asLiveData()
    }

    fun resetUpdatedCounter() {
        runBlocking {
            updatedCounter.value = 0
        }
    }

    suspend fun delete(uri: String) {
        coroutineScope {
            repository.deleteByUri(uri)
            updatedCounter.postValue(updatedCounter.value?.plus(1) ?: 1)
        }
    }
}