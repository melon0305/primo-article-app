package test.primo.primofeedapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import test.primo.primofeedapp.core.database.AppDatabase
import test.primo.primofeedapp.data.ArticleDao
import test.primo.primofeedapp.data.model.ArticleEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ArticleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var articleDao: ArticleDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        articleDao = database.articleDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `getAllArticles first query returns empty list`() = runTest {
        // When
        val articles = articleDao.getAllArticles().first()

        // Then
        assertTrue(articles.isEmpty())
    }

    @Test
    fun `insertArticle singleArticle returns article in list`() = runTest {
        // Given
        val article = ArticleEntity(
            title = "Test Article",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<p>Test content</p>"
        )

        // When
        articleDao.insertArticle(listOf(article))
        val articles = articleDao.getAllArticles().first()

        // Then
        assertEquals(1, articles.size)
        assertEquals("Test Article", articles[0].title)
        assertEquals("2023-12-25T10:30:45Z", articles[0].dateTime)
        assertEquals("<p>Test content</p>", articles[0].content)
    }

    @Test
    fun `insertArticle multipleArticles returns all articles`() = runTest {
        // Given
        val articles = listOf(
            ArticleEntity(
                title = "First Article",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>First content</p>"
            ),
            ArticleEntity(
                title = "Second Article",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Second content</p>"
            ),
            ArticleEntity(
                title = "Third Article",
                dateTime = "2023-12-27T20:15:00Z",
                content = "<p>Third content</p>"
            )
        )

        // When
        articleDao.insertArticle(articles)
        val result = articleDao.getAllArticles().first()

        // Then
        assertEquals(3, result.size)
        assertEquals("First Article", result[0].title)
        assertEquals("Second Article", result[1].title)
        assertEquals("Third Article", result[2].title)
    }

    @Test
    fun `insertArticle with conflict replaces existing article`() = runTest {
        // Given
        val originalArticle = ArticleEntity(
            id = 1L,
            title = "Original Title",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<p>Original content</p>"
        )

        val updatedArticle = ArticleEntity(
            id = 1L,
            title = "Updated Title",
            dateTime = "2023-12-26T15:45:30Z",
            content = "<p>Updated content</p>"
        )

        // When
        articleDao.insertArticle(listOf(originalArticle))
        articleDao.insertArticle(listOf(updatedArticle))
        val articles = articleDao.getAllArticles().first()

        // Then
        assertEquals(1, articles.size)
        assertEquals("Updated Title", articles[0].title)
        assertEquals("2023-12-26T15:45:30Z", articles[0].dateTime)
        assertEquals("<p>Updated content</p>", articles[0].content)
    }

    @Test
    fun `insertArticle empty list does not change database`() = runTest {
        // Given
        val existingArticle = ArticleEntity(
            title = "Existing Article",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<p>Existing content</p>"
        )
        articleDao.insertArticle(listOf(existingArticle))

        // When
        articleDao.insertArticle(emptyList())
        val articles = articleDao.getAllArticles().first()

        // Then
        assertEquals(1, articles.size)
        assertEquals("Existing Article", articles[0].title)
    }

    @Test
    fun `deleteAllArticles removes all articles`() = runTest {
        // Given
        val articles = listOf(
            ArticleEntity(
                title = "Article 1",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Content 1</p>"
            ),
            ArticleEntity(
                title = "Article 2",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Content 2</p>"
            )
        )
        articleDao.insertArticle(articles)

        // When
        articleDao.deleteAllArticles()
        val result = articleDao.getAllArticles().first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deleteAllArticles on empty database does not throw error`() = runTest {
        articleDao.deleteAllArticles()
        val articles = articleDao.getAllArticles().first()
        assertTrue(articles.isEmpty())
    }

    @Test
    fun `refreshArticle replaces all existing articles`() = runTest {
        // Given
        val originalArticles = listOf(
            ArticleEntity(
                title = "Original 1",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Original content 1</p>"
            ),
            ArticleEntity(
                title = "Original 2",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Original content 2</p>"
            )
        )
        articleDao.insertArticle(originalArticles)

        val newArticles = listOf(
            ArticleEntity(
                title = "New 1",
                dateTime = "2023-12-27T20:15:00Z",
                content = "<p>New content 1</p>"
            ),
            ArticleEntity(
                title = "New 2",
                dateTime = "2023-12-28T09:30:15Z",
                content = "<p>New content 2</p>"
            ),
            ArticleEntity(
                title = "New 3",
                dateTime = "2023-12-29T14:45:30Z",
                content = "<p>New content 3</p>"
            )
        )

        // When
        articleDao.refreshArticle(newArticles)
        val result = articleDao.getAllArticles().first()

        // Then
        assertEquals(3, result.size)
        assertEquals("New 1", result[0].title)
        assertEquals("New 2", result[1].title)
        assertEquals("New 3", result[2].title)

        // Verify original articles are gone
        assertFalse(result.any { it.title.startsWith("Original") })
    }

    @Test
    fun `refreshArticle with empty list clears all articles`() = runTest {
        // Given
        val existingArticles = listOf(
            ArticleEntity(
                title = "Existing 1",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Existing content 1</p>"
            ),
            ArticleEntity(
                title = "Existing 2",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Existing content 2</p>"
            )
        )
        articleDao.insertArticle(existingArticles)

        // When
        articleDao.refreshArticle(emptyList())
        val result = articleDao.getAllArticles().first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `refresh article on emptyDatabase inserts new articles`() = runTest {
        // Given
        val newArticles = listOf(
            ArticleEntity(
                title = "Fresh Article 1",
                dateTime = "2023-12-25T10:30:45Z",
                content = "<p>Fresh content 1</p>"
            ),
            ArticleEntity(
                title = "Fresh Article 2",
                dateTime = "2023-12-26T15:45:30Z",
                content = "<p>Fresh content 2</p>"
            )
        )

        // When
        articleDao.refreshArticle(newArticles)
        val result = articleDao.getAllArticles().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Fresh Article 1", result[0].title)
        assertEquals("Fresh Article 2", result[1].title)
    }

    @Test
    fun `getAllArticles flow emits updates onDataChange`() = runTest {
        // Given
        val initialArticle = ArticleEntity(
            title = "Initial Article",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<p>Initial content</p>"
        )

        // When - Insert initial data
        articleDao.insertArticle(listOf(initialArticle))
        val firstEmission = articleDao.getAllArticles().first()

        // Then
        assertEquals(1, firstEmission.size)
        assertEquals("Initial Article", firstEmission[0].title)

        // When - Add more data
        val additionalArticle = ArticleEntity(
            title = "Additional Article",
            dateTime = "2023-12-26T15:45:30Z",
            content = "<p>Additional content</p>"
        )
        articleDao.insertArticle(listOf(additionalArticle))
        val secondEmission = articleDao.getAllArticles().first()

        // Then
        assertEquals(2, secondEmission.size)
    }

    @Test
    fun `insertArticle with special Characters handles correctly`() = runTest {
        // Given
        val articleWithSpecialChars = ArticleEntity(
            title = "Article with émojis 😀 and spëcial chars: <>&\"'",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<p>Content with HTML &lt;tags&gt; and \"quotes\" and 'apostrophes'</p>"
        )

        // When
        articleDao.insertArticle(listOf(articleWithSpecialChars))
        val articles = articleDao.getAllArticles().first()

        // Then
        assertEquals(1, articles.size)
        assertEquals("Article with émojis 😀 and spëcial chars: <>&\"'", articles[0].title)
        assertEquals(
            "<p>Content with HTML &lt;tags&gt; and \"quotes\" and 'apostrophes'</p>",
            articles[0].content
        )
    }

    @Test
    fun `insertArticle with large content handles correctly`() = runTest {
        // Given
        val largeContent = "Large content: " + "Lorem ipsum ".repeat(1000)
        val articleWithLargeContent = ArticleEntity(
            title = "Article with large content",
            dateTime = "2023-12-25T10:30:45Z",
            content = largeContent
        )

        // When
        articleDao.insertArticle(listOf(articleWithLargeContent))
        val articles = articleDao.getAllArticles().first()

        // Then
        assertEquals(1, articles.size)
        assertEquals(largeContent, articles[0].content)
        assertTrue(articles[0].content.length > 10000)
    }
}