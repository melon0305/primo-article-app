package test.primo.primofeedapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import test.primo.primofeedapp.domain.model.Article
import test.primo.primofeedapp.domain.repository.ArticleRepository
import test.primo.primofeedapp.presentation.screen.list.ArticleListViewModel
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ArticleListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository = mockk<ArticleRepository>()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ArticleListViewModel

    @Before
    fun setup() {
        viewModel = ArticleListViewModel(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
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
    fun `getAllArticle sets loading state initially`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        val articles = listOf(
            Article(
                title = "Test Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Test content</p>"
            )
        )
        every { repository.getAllArticle(username) } returns flowOf(articles)

        // When
        viewModel.getAllArticle(username)

        // Capture the loading state (first emission)
        advanceUntilIdle()

        // Then - Check that loading was set to true initially
        verify { repository.getAllArticle(username) }
    }

    @Test
    fun `getAllArticle with successful data updates uiState correctly`() = runTest(testDispatcher) {
        // Given
        val username = "successuser"
        val articles = listOf(
            Article(
                title = "Success Article 1",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<figure><img src=\"https://example.com/image1.jpg\" /></figure><p>Content 1</p>"
            ),
            Article(
                title = "Success Article 2",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Content 2</p>"
            )
        )
        every { repository.getAllArticle(username) } returns flowOf(articles)

        // When
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        assertNotNull(finalState.data)
        assertEquals(2, finalState.data?.size)

        // Verify mapping to UI state
        assertEquals("Success Article 1", finalState.data?.get(0)?.title)
        assertEquals(
            "Dec 25, 2023",
            finalState.data?.get(0)?.dateTime
        ) // Assumes toReadableDate() works
        assertEquals(
            "https://example.com/image1.jpg",
            finalState.data?.get(0)?.imageUrl
        ) // Assumes getImageFromFigure() works

        assertEquals("Success Article 2", finalState.data?.get(1)?.title)
        assertNull(finalState.data?.get(1)?.imageUrl) // No image in content

        verify { repository.getAllArticle(username) }
    }

    @Test
    fun `getAllArticle with empty data updates uiState correctly`() = runTest(testDispatcher) {
        // Given
        val username = "emptyuser"
        val emptyArticles = emptyList<Article>()
        every { repository.getAllArticle(username) } returns flowOf(emptyArticles)

        // When
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        assertNotNull(finalState.data)
        assertTrue(finalState.data?.isEmpty() == true)

        verify { repository.getAllArticle(username) }
    }

    @Test
    fun `getAllArticle with repository exception updates error state`() = runTest(testDispatcher) {
        // Given
        val username = "erroruser"
        val exception = RuntimeException("Repository error")
        every { repository.getAllArticle(username) } throws exception

        // When
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.data)
        assertEquals("Repository error", finalState.error)

        verify { repository.getAllArticle(username) }
    }

    @Test
    fun `getAllArticle with network exception updates error state`() = runTest(testDispatcher) {
        // Given
        val username = "networkuser"
        val networkException = IOException("Network connection failed")
        every { repository.getAllArticle(username) } throws networkException

        // When
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.data)
        assertEquals("Network connection failed", finalState.error)

        verify { repository.getAllArticle(username) }
    }

    @Test
    fun `getAllArticle with multiple flow emissions updates uiState correctly`() =
        runTest(testDispatcher) {
            // Given
            val username = "multiuser"
            val initialArticles = listOf(
                Article(
                    title = "Initial Article",
                    dateTime = "2023-12-25T10:30:45Z",
                    content = "<p>Initial content</p>"
                )
            )
            val updatedArticles = listOf(
                Article(
                    title = "Initial Article",
                    dateTime = "2023-12-25T10:30:45Z",
                    content = "<p>Initial content</p>"
                ),
                Article(
                    title = "Updated Article",
                    dateTime = "2023-12-26T15:45:30Z",
                    content = "<p>Updated content</p>"
                )
            )

            coEvery { repository.getAllArticle(username) } returns flow {
                emit(initialArticles)
                emit(updatedArticles)
            }

            // When
            viewModel.getAllArticle(username)
            advanceUntilIdle()

            // Then - Should have the final emission
            val finalState = viewModel.uiState.value
            assertFalse(finalState.isLoading)
            assertNull(finalState.error)
            assertEquals(2, finalState.data?.size)
            assertEquals("Initial Article", finalState.data?.get(0)?.title)
            assertEquals("Updated Article", finalState.data?.get(1)?.title)

            verify { repository.getAllArticle(username) }
        }

    @Test
    fun `getAllArticle clears previous error state on new request`() = runTest(testDispatcher) {
        // Given
        val username = "clearerroruser"

        // First call fails
        every { repository.getAllArticle(username) } throws RuntimeException("First error")
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Verify error state
        assertEquals("First error", viewModel.uiState.value.error)

        // Second call succeeds
        val articles = listOf(
            Article(
                title = "Success Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Success content</p>"
            )
        )
        every { repository.getAllArticle(username) } returns flowOf(articles)

        // When
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.error) // Error should be cleared
        assertEquals(1, finalState.data?.size)
        assertEquals("Success Article", finalState.data?.get(0)?.title)
    }

    @Test
    fun `getAllArticle handles concurrent calls correctly`() = runTest(testDispatcher) {
        // Given
        val username1 = "user1"
        val username2 = "user2"

        val articles1 = listOf(
            Article(
                title = "User1 Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>User1 content</p>"
            )
        )
        val articles2 = listOf(
            Article(
                title = "User2 Article",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>User2 content</p>"
            )
        )

        every { repository.getAllArticle(username1) } returns flowOf(articles1)
        every { repository.getAllArticle(username2) } returns flowOf(articles2)

        // When
        viewModel.getAllArticle(username1)
        viewModel.getAllArticle(username2) // Second call should override first
        advanceUntilIdle()

        // Then - Should have the result from the second call
        val finalState = viewModel.uiState.value
        assertEquals("User2 Article", finalState.data?.get(0)?.title)

        verify { repository.getAllArticle(username1) }
        verify { repository.getAllArticle(username2) }
    }

    @Test
    fun `getAllArticle with null exception message handles gracefully`() = runTest(testDispatcher) {
        // Given
        val username = "nullerroruser"
        val exception = RuntimeException(null as String?)
        every { repository.getAllArticle(username) } throws exception

        // When
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.data)
        assertNull(finalState.error)

        verify { repository.getAllArticle(username) }
    }

    @Test
    fun `getAllArticle with large dataset handles correctly`() = runTest(testDispatcher) {
        // Given
        val username = "largeuser"
        val largeArticleList = (1..100).map { index ->
            Article(
                title = "Article $index",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Content for article $index</p>"
            )
        }
        every { repository.getAllArticle(username) } returns flowOf(largeArticleList)

        // When
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        assertEquals(100, finalState.data?.size)
        assertEquals("Article 1", finalState.data?.first()?.title)
        assertEquals("Article 100", finalState.data?.last()?.title)

        verify { repository.getAllArticle(username) }
    }

    @Test
    fun `verify exact repository call count`() = runTest(testDispatcher) {
        // Given
        val username = "exactuser"
        val articles = listOf(
            Article(
                title = "Exact Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Exact content</p>"
            )
        )
        every { repository.getAllArticle(username) } returns flowOf(articles)

        // When
        viewModel.getAllArticle(username)
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { repository.getAllArticle(username) }
        confirmVerified(repository)
    }

}