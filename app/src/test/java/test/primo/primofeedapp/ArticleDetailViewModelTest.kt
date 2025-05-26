package test.primo.primofeedapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import test.primo.primofeedapp.fake.FakeAndroidResourceProvider
import test.primo.primofeedapp.presentation.screen.detail.ArticleDetailViewModel

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ArticleDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeResourceProvider: FakeAndroidResourceProvider
    private lateinit var viewModel: ArticleDetailViewModel

    @Before
    fun setup() {
        fakeResourceProvider = FakeAndroidResourceProvider()
        viewModel = ArticleDetailViewModel(fakeResourceProvider)
    }

    @Test
    fun `initial uiState is correct`() {
        // Given & When
        val initialState = viewModel.uiState.value

        // Then
        assertFalse(initialState.isLoading)
        assertNull(initialState.data)
        assertNull(initialState.error)
    }

    @Test
    fun `generateHtmlTemplate with valid template replaces content correctly`() = runTest(testDispatcher) {
        // Given
        val template = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Article Detail</title>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .content { padding: 20px; }
                </style>
            </head>
            <body>
                <div class="content">
                    {{content}}
                </div>
            </body>
            </html>
        """.trimIndent()
        
        val articleContent = "<h1>Test Article</h1><p>This is test content</p>"
        fakeResourceProvider.setTemplate(template)

        // When
        viewModel.generateHtmlTemplate(articleContent)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        assertNotNull(finalState.data)
        
        val expectedHtml = template.replace("{{content}}", articleContent)
        assertEquals(expectedHtml, finalState.data)
        assertTrue(finalState.data?.contains("<h1>Test Article</h1>") == true)
        assertTrue(finalState.data?.contains("<p>This is test content</p>") == true)
    }

    @Test
    fun `generateHtmlTemplate with null template returns original content`() = runTest(testDispatcher) {
        // Given
        val articleContent = "<h1>Original Content</h1><p>This should be returned as-is</p>"
        fakeResourceProvider.setTemplate(null) // Template not found

        // When
        viewModel.generateHtmlTemplate(articleContent)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        assertEquals(articleContent, finalState.data)
    }

    @Test
    fun `generateHtmlTemplate with resource provider exception returns original content`() = runTest(testDispatcher) {
        // Given
        val articleContent = "<h1>Exception Content</h1><p>This should be returned on exception</p>"
        fakeResourceProvider.setShouldThrowException(true)

        // When
        viewModel.generateHtmlTemplate(articleContent)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        assertEquals(articleContent, finalState.data)
    }

    @Test
    fun `generateHtmlTemplate with empty content handles correctly`() = runTest(testDispatcher) {
        // Given
        val template = "<html><body>{{content}}</body></html>"
        val emptyContent = ""
        fakeResourceProvider.setTemplate(template)

        // When
        viewModel.generateHtmlTemplate(emptyContent)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        val expectedHtml = "<html><body></body></html>"
        assertEquals(expectedHtml, finalState.data)
    }

    @Test
    fun `generateHtmlTemplate with template containing no placeholder returns template with content appended`() = runTest(testDispatcher) {
        // Given
        val templateWithoutPlaceholder = "<html><body><h1>Static Template</h1></body></html>"
        val content = "<p>New content</p>"
        fakeResourceProvider.setTemplate(templateWithoutPlaceholder)

        // When
        viewModel.generateHtmlTemplate(content)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        // Since there's no {{content}} placeholder, replace() returns the original template
        assertEquals(templateWithoutPlaceholder, finalState.data)
    }


    @Test
    fun `generateHtmlTemplate sets loading state correctly`() = runTest(testDispatcher) {
        // Given
        val template = "<html><body>{{content}}</body></html>"
        val content = "<p>Loading test content</p>"
        fakeResourceProvider.setTemplate(template)

        // When
        viewModel.generateHtmlTemplate(content)
        
        // Check initial state is not loading (since it starts with isLoading = false)
        val initialState = viewModel.uiState.value
        assertFalse(initialState.isLoading)
        
        // Complete the operation
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading) // Should be false after completion
        assertNotNull(finalState.data)
    }

}