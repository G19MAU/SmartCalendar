package se.umu.calu0217.smartcalendar.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.db.TaskEntity
import se.umu.calu0217.smartcalendar.data.repository.TasksRepository

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {
    val tasks: StateFlow<List<TaskEntity>> =
        repository.tasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun toggleComplete(id: Int) {
        viewModelScope.launch { repository.toggleComplete(id) }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.refresh()
            _isLoading.value = false
            result.exceptionOrNull()?.let { _error.value = it }
        }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = TasksRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return TasksViewModel(repo) as T
                }
            }
    }
}


