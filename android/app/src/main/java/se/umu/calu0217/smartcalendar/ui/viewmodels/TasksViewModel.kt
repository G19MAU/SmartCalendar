package se.umu.calu0217.smartcalendar.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    init {
        viewModelScope.launch { repository.refresh() }
    }

    fun toggleComplete(id: Int) {
        viewModelScope.launch { repository.toggleComplete(id) }
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


