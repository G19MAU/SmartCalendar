package se.umu.calu0217.smartcalendar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.db.ActivityEntity
import se.umu.calu0217.smartcalendar.data.repository.ActivitiesRepository
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val repository: ActivitiesRepository
) : ViewModel() {
    val activities: StateFlow<List<ActivityEntity>> =
        repository.activities.stateIn(
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

    suspend fun getById(id: Int): ActivityEntity? = repository.getById(id)

    suspend fun create(request: CreateActivityRequest) = repository.create(request)

    suspend fun edit(id: Int, request: CreateActivityRequest) = repository.edit(id, request)

    suspend fun delete(id: Int) = repository.delete(id)
}


