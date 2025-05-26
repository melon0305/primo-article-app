package test.primo.primofeedapp

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import test.primo.primofeedapp.data.datasource.ArticleLocalDataSource
import test.primo.primofeedapp.data.datasource.ArticleRemoteDataSource
import test.primo.primofeedapp.data.model.ArticleEntity
import test.primo.primofeedapp.data.model.ArticleResponse
import test.primo.primofeedapp.data.repository.ArticleRepositoryImpl
import test.primo.primofeedapp.domain.model.Article

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ArticleRepositoryImplTest {

    private val localDataSource = mockk<ArticleLocalDataSource>()
    private val remoteDataSource = mockk<ArticleRemoteDataSource>()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ArticleRepositoryImpl

    @Before
    fun setup() {
        repository = ArticleRepositoryImpl(
            local = localDataSource,
            remote = remoteDataSource,
            dispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getAllArticle emits local data immediately`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        val localEntities = listOf(
            ArticleEntity(
                id = 1L,
                title = "Local Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Local content</p>"
            )
        )

        every { localDataSource.getAllArticle() } returns flowOf(localEntities)
        coEvery { remoteDataSource.getAllArticle(username) } returns createMockArticleResponse()
        coEvery { localDataSource.refreshArticles(any()) } just Runs

        // When
        val result = mutableListOf<List<Article>>()
        val job = launch {
            repository.getAllArticle(username).take(1).toList().let {
                result.addAll(it)
            }
        }

        // Advance to complete the first emission
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        // Then
        assertEquals(1, result.size)
        assertEquals(1, result[0].size)
        assertEquals("Local Article", result[0][0].title)

        verify { localDataSource.getAllArticle() }
    }

    @Test
    fun `getAllArticle fetches remote data only once per flow collection`() =
        runTest(testDispatcher) {
            // Given
            val username = "onceuser"
            val localEntities = listOf(
                ArticleEntity(
                    id = 1L,
                    title = "Test Article",
                    dateTime = "2023-12-25T10:30:45Z",
                    content = "<p>Test content</p>"
                )
            )

            // Simulate local data updating multiple times
            every { localDataSource.getAllArticle() } returns flow {
                emit(localEntities)
                kotlinx.coroutines.delay(100)
                emit(
                    localEntities + listOf(
                        ArticleEntity(
                            id = 2L,
                            title = "New Article",
                            dateTime = "2023-12-27T20:15:00Z",
                            content = "<p>New content</p>"
                        )
                    )
                )
            }
            coEvery { remoteDataSource.getAllArticle(username) } returns createMockArticleResponse()
            coEvery { localDataSource.refreshArticles(any()) } just Runs

            // When
            val job = launch {
                repository.getAllArticle(username).take(2).collect { }
            }

            testDispatcher.scheduler.advanceUntilIdle()
            advanceTimeBy(1000)
            testDispatcher.scheduler.advanceUntilIdle()
            job.cancel()

            // Then - Remote should only be called once despite multiple local emissions
            coVerify(exactly = 1) { remoteDataSource.getAllArticle(username) }
            coVerify(exactly = 1) { localDataSource.refreshArticles(any()) }
        }

    @Test
    fun `getAllArticle preserves data mapping between layers`() = runTest(testDispatcher) {
        // Given
        val username = "mappinguser"
        val localEntity = ArticleEntity(
            id = 1L,
            title = "Mapping Test Article",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<figure><img src=\"https://example.com/image.jpg\" /></figure><p>Test content</p>"
        )

        every { localDataSource.getAllArticle() } returns flowOf(listOf(localEntity))
        coEvery { remoteDataSource.getAllArticle(username) } returns createMockArticleResponse()
        coEvery { localDataSource.refreshArticles(any()) } just Runs

        // When
        val result = mutableListOf<List<Article>>()
        val job = launch {
            repository.getAllArticle(username).take(1).collect { articles ->
                result.add(articles)
            }
        }

        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        // Then
        assertEquals(1, result.size)
        val mappedArticle = result[0][0]
        assertEquals("Mapping Test Article", mappedArticle.title)
        assertEquals("2023-12-25T10:30:45Z", mappedArticle.dateTime)
        assertTrue(mappedArticle.content.contains("<figure>"))
        assertTrue(mappedArticle.content.contains("https://example.com/image.jpg"))
    }


    @Test
    fun `verify no remote calls when flow is cancelled before delay`() = runTest(testDispatcher) {
        // Given
        val username = "cancelleduser"
        val localEntities = listOf(
            ArticleEntity(
                id = 1L,
                title = "Cancelled Test",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Content</p>"
            )
        )

        every { localDataSource.getAllArticle() } returns flowOf(localEntities)
        coEvery { remoteDataSource.getAllArticle(username) } returns createMockArticleResponse()

        // When
        val job = launch {
            repository.getAllArticle(username).take(1).collect { }
        }

        testDispatcher.scheduler.advanceUntilIdle()
        // Cancel before the 500ms delay
        advanceTimeBy(300)
        job.cancel()

        // Advance past delay to ensure remote is not called
        advanceTimeBy(300)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { remoteDataSource.getAllArticle(username) }
    }


    // Helper methods to create mock responses
    private fun createMockArticleResponse(): ArticleResponse {
        val item1 = ArticleResponse.ItemResponse().apply {
            title = "Remote Article 1"
            dateTime = "2023-12-26T15:45:30Z"
            content = "<p>Remote content 1</p>"
        }

        val item2 = ArticleResponse.ItemResponse().apply {
            title = "Remote Article 2"
            dateTime = "2023-12-27T20:15:00Z"
            content = "<p>Remote content 2</p>"
        }

        val channel = ArticleResponse.ChannelResponse().apply {
            articles = listOf(item1, item2)
        }

        return ArticleResponse().apply {
            this.channel = channel
        }
    }

    private fun createEmptyArticleResponse(): ArticleResponse {
        val channel = ArticleResponse.ChannelResponse().apply {
            articles = emptyList()
        }

        return ArticleResponse().apply {
            this.channel = channel
        }
    }
}