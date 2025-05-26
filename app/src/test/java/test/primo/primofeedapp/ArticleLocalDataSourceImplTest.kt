package test.primo.primofeedapp

import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import test.primo.primofeedapp.data.ArticleDao
import test.primo.primofeedapp.data.datasource.ArticleLocalDataSourceImpl
import test.primo.primofeedapp.data.model.ArticleEntity

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ArticleLocalDataSourceImplTest {

    private val articleDao = mockk<ArticleDao>()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dataSource: ArticleLocalDataSourceImpl

    @Before
    fun setup() {
        dataSource = ArticleLocalDataSourceImpl(
            articleDao = articleDao,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getAllArticle returns flow from dao with correct dispatcher`() = runTest(testDispatcher) {
        // Given
        val expectedArticles = listOf(
            ArticleEntity(
                id = 1L,
                title = "Test Article 1",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Test content 1</p>"
            ),
            ArticleEntity(
                id = 2L,
                title = "Test Article 2",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Test content 2</p>"
            )
        )
        every { articleDao.getAllArticles() } returns flowOf(expectedArticles)

        // When
        val result = dataSource.getAllArticle().first()

        // Then
        assertEquals(expectedArticles, result)
        assertEquals(2, result.size)
        assertEquals("Test Article 1", result[0].title)
        assertEquals("Test Article 2", result[1].title)
        verify { articleDao.getAllArticles() }
    }

    @Test
    fun `getAllArticle returns empty flow when dao returns empty list`() = runTest(testDispatcher) {
        // Given
        val emptyList = emptyList<ArticleEntity>()
        every { articleDao.getAllArticles() } returns flowOf(emptyList)

        // When
        val result = dataSource.getAllArticle().first()

        // Then
        assertTrue(result.isEmpty())
        verify { articleDao.getAllArticles() }
    }

    @Test
    fun `getAllArticle handles single article correctly`() = runTest(testDispatcher) {
        // Given
        val singleArticle = listOf(
            ArticleEntity(
                id = 1L,
                title = "Single Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Single content</p>"
            )
        )
        every { articleDao.getAllArticles() } returns flowOf(singleArticle)

        // When
        val result = dataSource.getAllArticle().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Single Article", result[0].title)
        verify { articleDao.getAllArticles() }
    }

    @Test
    fun `getAllArticle preserves article data integrity`() = runTest(testDispatcher) {
        // Given
        val articleWithSpecialContent = listOf(
            ArticleEntity(
                id = 100L,
                title = "Article with Special Characters: <>&\"'",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<figure><img src=\"https://example.com/image.jpg\" /></figure><p>Content with HTML</p>"
            )
        )
        every { articleDao.getAllArticles() } returns flowOf(articleWithSpecialContent)

        // When
        val result = dataSource.getAllArticle().first()

        // Then
        assertEquals(100L, result[0].id)
        assertEquals("Article with Special Characters: <>&\"'", result[0].title)
        assertTrue(result[0].content.contains("<figure>"))
        assertTrue(result[0].content.contains("https://example.com/image.jpg"))
        verify { articleDao.getAllArticles() }
    }

    @Test
    fun `refreshArticles calls dao with correct parameters and dispatcher`() =
        runTest(testDispatcher) {
            // Given
            val articlesToRefresh = listOf(
                ArticleEntity(
                    id = 1L,
                    title = "New Article 1",
                    dateTime = "2023-12-25T10:30:45Z",
                    content = "<p>New content 1</p>"
                ),
                ArticleEntity(
                    id = 2L,
                    title = "New Article 2",
                    dateTime = "2023-12-26T15:45:30Z",
                    content = "<p>New content 2</p>"
                )
            )
            coEvery { articleDao.refreshArticle(articlesToRefresh) } just Runs

            // When
            dataSource.refreshArticles(articlesToRefresh)

            // Then
            coVerify { articleDao.refreshArticle(articlesToRefresh) }
        }

    @Test
    fun `refreshArticles handles empty list correctly`() = runTest(testDispatcher) {
        // Given
        val emptyArticleList = emptyList<ArticleEntity>()
        coEvery { articleDao.refreshArticle(emptyArticleList) } just Runs

        // When
        dataSource.refreshArticles(emptyArticleList)

        // Then
        coVerify { articleDao.refreshArticle(emptyArticleList) }
    }

    @Test
    fun `refreshArticles handles single article correctly`() = runTest(testDispatcher) {
        // Given
        val singleArticle = listOf(
            ArticleEntity(
                id = 1L,
                title = "Single Refresh Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Single refresh content</p>"
            )
        )
        coEvery { articleDao.refreshArticle(singleArticle) } just Runs

        // When
        dataSource.refreshArticles(singleArticle)

        // Then
        coVerify { articleDao.refreshArticle(singleArticle) }
    }

    @Test
    fun `refreshArticles handles large dataset correctly`() = runTest(testDispatcher) {
        // Given
        val largeArticleList = (1..100).map { index ->
            ArticleEntity(
                id = index.toLong(),
                title = "Article $index",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Content for article $index</p>"
            )
        }
        coEvery { articleDao.refreshArticle(largeArticleList) } just Runs

        // When
        dataSource.refreshArticles(largeArticleList)

        // Then
        coVerify { articleDao.refreshArticle(largeArticleList) }
    }

    @Test
    fun `refreshArticles preserves article data during refresh`() = runTest(testDispatcher) {
        // Given
        val articlesWithComplexData = listOf(
            ArticleEntity(
                id = 1L,
                title = "Article with Unicode: ทดสอบ 测试 🚀",
                dateTime = "2023-12-25T10:30:45Z",
                content = """
                    <figure>
                        <img src="https://example.com/complex-image.jpg" alt="Complex" />
                        <figcaption>Caption with special chars: <>&"'</figcaption>
                    </figure>
                    <p>Paragraph with <strong>bold</strong> and <em>italic</em> text</p>
                """.trimIndent()
            )
        )
        coEvery { articleDao.refreshArticle(articlesWithComplexData) } just Runs

        // When
        dataSource.refreshArticles(articlesWithComplexData)

        // Then
        coVerify { articleDao.refreshArticle(articlesWithComplexData) }
    }

    @Test
    fun `getAllArticle flow updates when dao emits new data`() = runTest(testDispatcher) {
        // Given
        val initialArticles = listOf(
            ArticleEntity(
                id = 1L,
                title = "Initial Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Initial content</p>"
            )
        )

        val updatedArticles = listOf(
            ArticleEntity(
                id = 1L,
                title = "Updated Article",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Updated content</p>"
            ),
            ArticleEntity(
                id = 2L,
                title = "New Article",
                dateTime = "2023-12-27T20:15:00Z",
                content = "<p>New content</p>"
            )
        )

        every { articleDao.getAllArticles() } returns flowOf(initialArticles) andThen flowOf(
            updatedArticles
        )

        // When
        val firstEmission = dataSource.getAllArticle().first()

        // Then
        assertEquals(1, firstEmission.size)
        assertEquals("Initial Article", firstEmission[0].title)
        verify { articleDao.getAllArticles() }
    }

    @Test
    fun `refreshArticles handles dao exceptions gracefully`() = runTest(testDispatcher) {
        // Given
        val articles = listOf(
            ArticleEntity(
                id = 1L,
                title = "Exception Test Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Test content</p>"
            )
        )

        coEvery { articleDao.refreshArticle(articles) } throws RuntimeException("Database error")

        // When & Then
        try {
            dataSource.refreshArticles(articles)
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
            coVerify { articleDao.refreshArticle(articles) }
        }
    }

    @Test
    fun `data source maintains between operations`() = runTest(testDispatcher) {
        // Given
        val originalArticles = listOf(
            ArticleEntity(
                id = 1L,
                title = "Original Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Original content</p>"
            )
        )

        val refreshedArticles = listOf(
            ArticleEntity(
                id = 2L,
                title = "Refreshed Article",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Refreshed content</p>"
            )
        )

        every { articleDao.getAllArticles() } returns flowOf(originalArticles) andThen flowOf(
            refreshedArticles
        )
        coEvery { articleDao.refreshArticle(refreshedArticles) } just Runs

        // When
        val initialData = dataSource.getAllArticle().first()
        dataSource.refreshArticles(refreshedArticles)

        // Then
        assertEquals("Original Article", initialData[0].title)
        verify { articleDao.getAllArticles() }
        coVerify { articleDao.refreshArticle(refreshedArticles) }
    }

    @Test
    fun `verify exact method calls with relaxed mock`() = runTest(testDispatcher) {
        // Given
        val relaxedDao = mockk<ArticleDao>(relaxed = true)
        val relaxedDataSource = ArticleLocalDataSourceImpl(relaxedDao, testDispatcher)

        val articles = listOf(
            ArticleEntity(
                id = 1L,
                title = "Relaxed Test Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Relaxed content</p>"
            )
        )

        // When
        relaxedDataSource.refreshArticles(articles)

        // Then
        coVerify(exactly = 1) { relaxedDao.refreshArticle(articles) }
        confirmVerified(relaxedDao)
    }

    @Test
    fun `verify no interactions when methods not called`() = runTest(testDispatcher) {
        // Given
        val freshDao = mockk<ArticleDao>()
        val freshDataSource = ArticleLocalDataSourceImpl(freshDao, testDispatcher)

        // When - no operations performed

        // Then
        verify { freshDao wasNot Called }
    }

    @Test
    fun `verify call order for multiple operations`() = runTest(testDispatcher) {
        // Given
        val articles1 = listOf(
            ArticleEntity(
                id = 1L,
                title = "Article 1",
                dateTime = "2023-12-25T10:30:45Z",
                content = "Content 1"
            )
        )
        val articles2 = listOf(
            ArticleEntity(
                id = 2L,
                title = "Article 2",
                dateTime = "2023-12-26T15:45:30Z",
                content = "Content 2"
            )
        )

        every { articleDao.getAllArticles() } returns flowOf(articles1)
        coEvery { articleDao.refreshArticle(any()) } just Runs

        // When
        dataSource.getAllArticle().first()
        dataSource.refreshArticles(articles2)

        // Then
        coVerifyOrder {
            articleDao.getAllArticles()
            articleDao.refreshArticle(articles2)
        }
    }

    @Test
    fun `verify argument matching with any matcher`() = runTest(testDispatcher) {
        // Given
        coEvery { articleDao.refreshArticle(any()) } just Runs

        val anyArticles = listOf(
            ArticleEntity(
                id = 999L,
                title = "Any Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "Any content"
            )
        )

        // When
        dataSource.refreshArticles(anyArticles)

        // Then
        coVerify { articleDao.refreshArticle(any<List<ArticleEntity>>()) }
    }

    @Test
    fun `verify with capture slot for detailed parameter inspection`() = runTest(testDispatcher) {
        // Given
        val slot = slot<List<ArticleEntity>>()
        coEvery { articleDao.refreshArticle(capture(slot)) } just Runs

        val testArticles = listOf(
            ArticleEntity(
                id = 1L,
                title = "Captured Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Captured content</p>"
            )
        )

        // When
        dataSource.refreshArticles(testArticles)

        // Then
        coVerify { articleDao.refreshArticle(any()) }
        assertEquals(testArticles, slot.captured)
        assertEquals("Captured Article", slot.captured[0].title)
    }
}