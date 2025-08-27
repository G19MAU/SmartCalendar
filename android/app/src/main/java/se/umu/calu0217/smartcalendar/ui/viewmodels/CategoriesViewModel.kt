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
import se.umu.calu0217.smartcalendar.data.db.CategoryEntity
import se.umu.calu0217.smartcalendar.data.repository.CategoryRepository
import se.umu.calu0217.smartcalendar.domain.CreateCategoryRequest

class CategoriesViewModel(private val repository: CategoryRepository) : ViewModel() {
    val categories: StateFlow<List<CategoryEntity>> =
        repository.categories.stateIn(
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

    fun create(name: String, color: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.create(CreateCategoryRequest(name, color))
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = CategoryRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return CategoriesViewModel(repo) as T
                }
            }
    }
}

