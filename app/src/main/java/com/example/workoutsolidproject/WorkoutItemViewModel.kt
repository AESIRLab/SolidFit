package com.example.workoutsolidproject

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.workoutsolidproject.model.WorkoutItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.skCompiler.generatedModel.WorkoutItemRemoteDataSource
import org.skCompiler.generatedModel.WorkoutItemRepository

class WorkoutItemViewModel(
    private val repository: WorkoutItemRepository,
    private val WorkoutItemRemoteDataSource: WorkoutItemRemoteDataSource
): ViewModel() {

    private var _allItems: MutableStateFlow<List<WorkoutItem>> = MutableStateFlow(listOf())
    val allItems: StateFlow<List<WorkoutItem>> get() = _allItems

    private val _workoutItem = MutableStateFlow<WorkoutItem?>(null)
    val workoutItem: StateFlow<WorkoutItem?> = _workoutItem

    init {
        this.viewModelScope.launch {
            val newList = mutableListOf<WorkoutItem>()
            try{
                if (WorkoutItemRemoteDataSource.remoteAccessible()) {
                    newList += WorkoutItemRemoteDataSource.fetchRemoteItemList()
                }
                repository.allWorkoutItemsAsFlow.collect { list ->
                    newList += list
                }
                _allItems.value = newList.distinct()
            } catch (e: NullPointerException) {
                    Log.e("WorkoutViewModel", "Error loading RDF model: ${e.message}")
                    _allItems.value = emptyList()
            } catch (e: Exception) {
                    Log.e("WorkoutViewModel", "Unexpected error: ${e.message}")
                    _allItems.value = emptyList()
            }
        }
    }


    fun remoteIsAvailable(): Boolean {
        return WorkoutItemRemoteDataSource.remoteAccessible()
    }


    fun setRemoteRepositoryData(
        accessToken: String,
        signingJwk: String,
        webId: String,
        expirationTime: Long,
    ) {
        WorkoutItemRemoteDataSource.signingJwk = signingJwk
        WorkoutItemRemoteDataSource.webId = webId
        WorkoutItemRemoteDataSource.expirationTime = expirationTime
        WorkoutItemRemoteDataSource.accessToken = accessToken
    }


    fun updateWebId(webId: String) {
        viewModelScope.launch {
            try {
                repository.insertWebId(webId)
            } catch (e: Exception) {
                Log.e("WorkoutVM", "RDF parsing error, resetting local model", e)
                repository.resetModel()
            }

            repository.allWorkoutItemsAsFlow
                .catch { e ->
                    Log.e("WorkoutVM", "Error reading workout items", e)
                    emit(emptyList())
                }
                .collect { list ->
                    _allItems.value = list
                }

            WorkoutItemRemoteDataSource.updateRemoteItemList(_allItems.value)
        }
    }


    suspend fun insert(item: WorkoutItem) {
        val tempList = mutableListOf<WorkoutItem>()
        viewModelScope.launch {
            repository.insert(item)
            // not sure if this is the right way to do it...
            repository.allWorkoutItemsAsFlow.collect { list ->
                tempList += list
            }
        }
        viewModelScope.launch {
            _allItems.value = tempList
            WorkoutItemRemoteDataSource.updateRemoteItemList(tempList)
        }
    }


    suspend fun insertMany(list: List<WorkoutItem>) {
        viewModelScope.launch {
            repository.insertMany(list)
            repository.allWorkoutItemsAsFlow.collect { list ->
                _allItems.value = list
            }
        }
    }


    suspend fun delete(item: WorkoutItem) {
        viewModelScope.launch {
            repository.deleteByUri(item.id)
            repository.allWorkoutItemsAsFlow.collect { list ->
                _allItems.value = list
            }
            WorkoutItemRemoteDataSource.updateRemoteItemList(_allItems.value)
//            WorkoutItemRemoteDataSource.updateRemoteItemList()
        }
    }


    suspend fun updateRemote() {
        viewModelScope.launch {
            repository.allWorkoutItemsAsFlow.collect { list ->
                _allItems.value = list
            }.also {
                WorkoutItemRemoteDataSource.updateRemoteItemList(_allItems.value)
            }
        }
    }


    suspend fun update(item: WorkoutItem) {
        viewModelScope.launch {
            repository.update(item)
            repository.allWorkoutItemsAsFlow.collect { list ->
                _allItems.value = list
            }
            WorkoutItemRemoteDataSource.updateRemoteItemList(_allItems.value)
        }
    }

    fun loadWorkoutByUri(uri: String) {
        viewModelScope.launch {
            // Call the suspend function safely inside the ViewModel's coroutine scope
            repository.getWorkoutItemLiveData(uri).collect { item ->
                // Update the StateFlow with the new data
                _workoutItem.value = item
            }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WorkoutItemSolidApplication)
                val itemRepository = application.repository
                val itemRemoteDataSource = WorkoutItemRemoteDataSource(externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Default))
                WorkoutItemViewModel(itemRepository, itemRemoteDataSource)
            }
        }
    }
}