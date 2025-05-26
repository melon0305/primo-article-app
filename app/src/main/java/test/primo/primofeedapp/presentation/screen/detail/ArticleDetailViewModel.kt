package test.primo.primofeedapp.presentation.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import test.primo.primofeedapp.core.base.UiState
import test.primo.primofeedapp.domain.repository.ArticleRepository
import test.primo.primofeedapp.core.mapper.ArticleMapper.toUiState
import test.primo.primofeedapp.core.provider.AndroidResourceProvider
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val androidResourceProvider: AndroidResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<String>>(UiState())
    var uiState = _uiState.asStateFlow()

    fun generateHtmlTemplate(content: String) {
        viewModelScope.launch {
            try {
                androidResourceProvider.getStringFromAssert("article_detail_template.html")?.let { template ->
                    val htmlContent = template.replace("{{content}}", content)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        data = htmlContent
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        data = content
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    data = content
                )
            }
        }
    }

}