package test.primo.primofeedapp.core.base

data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasData: Boolean get() = data != null
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = data == null
}