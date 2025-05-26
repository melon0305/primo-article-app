package test.primo.primofeedapp.presentation.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import test.primo.primofeedapp.core.base.UiState
import test.primo.primofeedapp.domain.repository.ArticleRepository
import test.primo.primofeedapp.core.mapper.ArticleMapper.toUiState
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ArticleUiState>>>(UiState())
    var uiState = _uiState.asStateFlow()

    fun getAllArticle(username: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                repository.getAllArticle(username).collect { articles ->
                    val data = UiState(
                        isLoading = false,
                        data = articles.map { it.toUiState() },
                        error = null
                    )
                    _uiState.value = data
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }

        }
    }

}