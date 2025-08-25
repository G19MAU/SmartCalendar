package se.umu.calu0217.smartcalendar.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.db.ActivityEntity
import se.umu.calu0217.smartcalendar.data.repository.ActivitiesRepository

class ActivitiesViewModel(private val repository: ActivitiesRepository) : ViewModel() {
    val activities: StateFlow<List<ActivityEntity>> =
        repository.activities.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch { repository.refresh() }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = ActivitiesRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return ActivitiesViewModel(repo) as T
                }
            }
    }
}


