package test.primo.primofeedapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import test.primo.primofeedapp.core.mapper.ArticleMapper.toDomain
import test.primo.primofeedapp.core.mapper.ArticleMapper.toEntity
import test.primo.primofeedapp.core.mapper.ArticleMapper.toUiState
import test.primo.primofeedapp.data.model.ArticleEntity
import test.primo.primofeedapp.data.model.ArticleResponse
import test.primo.primofeedapp.domain.model.Article

class ArticleMapperTest {

    @Test
    fun `ArticleEntity toDomain maps all fields correctly`() {
        // Given
        val entity = ArticleEntity(
            id = 1L,
            title = "Test Article Title",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<p>Test content</p>"
        )

        // When
        val result = entity.toDomain()

        // Then
        assertEquals("Test Article Title", result.title)
        assertEquals("2023-12-25T10:30:45Z", result.dateTime)
        assertEquals("<p>Test content</p>", result.content)
    }

    @Test
    fun `ArticleEntity toDomain with empty fields maps correctly`() {
        // Given
        val entity = ArticleEntity(
            id = 0L,
            title = "",
            dateTime = "",
            content = ""
        )

        // When
        val result = entity.toDomain()

        // Then
        assertEquals("", result.title)
        assertEquals("", result.dateTime)
        assertEquals("", result.content)
    }

    @Test
    fun `ArticleResponse toDomain with valid data maps correctly`() {
        // Given
        val itemResponse1 = ArticleResponse.ItemResponse().apply {
            title = "First Article"
            dateTime = "2023-12-25T10:30:45Z"
            content = "<p>First article content</p>"
        }

        val itemResponse2 = ArticleResponse.ItemResponse().apply {
            title = "Second Article"
            dateTime = "2023-12-26T15:45:30Z"
            content = "<p>Second article content</p>"
        }

        val channelResponse = ArticleResponse.ChannelResponse().apply {
            articles = listOf(itemResponse1, itemResponse2)
        }

        val articleResponse = ArticleResponse().apply {
            channel = channelResponse
        }

        // When
        val result = articleResponse.toDomain()

        // Then
        assertEquals(2, result.size)

        assertEquals("First Article", result[0].title)
        assertEquals("2023-12-25T10:30:45Z", result[0].dateTime)
        assertEquals("<p>First article content</p>", result[0].content)

        assertEquals("Second Article", result[1].title)
        assertEquals("2023-12-26T15:45:30Z", result[1].dateTime)
        assertEquals("<p>Second article content</p>", result[1].content)
    }

    @Test
    fun `ArticleResponse toDomain with null fields uses empty strings`() {
        // Given
        val itemResponse = ArticleResponse.ItemResponse().apply {
            title = null
            dateTime = null
            content = null
        }

        val channelResponse = ArticleResponse.ChannelResponse().apply {
            articles = listOf(itemResponse)
        }

        val articleResponse = ArticleResponse().apply {
            channel = channelResponse
        }

        // When
        val result = articleResponse.toDomain()

        // Then
        assertEquals(1, result.size)
        assertEquals("", result[0].title)
        assertEquals("", result[0].dateTime)
        assertEquals("", result[0].content)
    }

    @Test
    fun `ArticleResponse toDomain with null channel returns empty list`() {
        // Given
        val articleResponse = ArticleResponse().apply {
            channel = null
        }

        // When
        val result = articleResponse.toDomain()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `ArticleResponse toDomain with null articles returns empty list`() {
        // Given
        val channelResponse = ArticleResponse.ChannelResponse().apply {
            articles = emptyList()
        }

        val articleResponse = ArticleResponse().apply {
            channel = channelResponse
        }

        // When
        val result = articleResponse.toDomain()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `ArticleResponse toDomain with mixed null and valid fields`() {
        // Given
        val itemResponse1 = ArticleResponse.ItemResponse().apply {
            title = "Valid Title"
            dateTime = null
            content = "<p>Valid content</p>"
        }

        val itemResponse2 = ArticleResponse.ItemResponse().apply {
            title = null
            dateTime = "2023-12-25T10:30:45Z"
            content = null
        }

        val channelResponse = ArticleResponse.ChannelResponse().apply {
            articles = listOf(itemResponse1, itemResponse2)
        }

        val articleResponse = ArticleResponse().apply {
            channel = channelResponse
        }

        // When
        val result = articleResponse.toDomain()

        // Then
        assertEquals(2, result.size)

        assertEquals("Valid Title", result[0].title)
        assertEquals("", result[0].dateTime)
        assertEquals("<p>Valid content</p>", result[0].content)

        assertEquals("", result[1].title)
        assertEquals("2023-12-25T10:30:45Z", result[1].dateTime)
        assertEquals("", result[1].content)
    }

    @Test
    fun `Article toEntity maps all fields correctly`() {
        // Given
        val article = Article(
            title = "Domain Article Title",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<p>Domain content</p>"
        )

        // When
        val result = article.toEntity()

        // Then
        assertEquals(0L, result.id) // Default auto-generated value
        assertEquals("Domain Article Title", result.title)
        assertEquals("2023-12-25T10:30:45Z", result.dateTime)
        assertEquals("<p>Domain content</p>", result.content)
    }

    @Test
    fun `Article toEntity with empty fields maps correctly`() {
        // Given
        val article = Article(
            title = "",
            dateTime = "",
            content = ""
        )

        // When
        val result = article.toEntity()

        // Then
        assertEquals(0L, result.id)
        assertEquals("", result.title)
        assertEquals("", result.dateTime)
        assertEquals("", result.content)
    }

    @Test
    fun `Article toUiState with valid data maps correctly`() {
        // Given
        val article = Article(
            title = "UI Article Title",
            dateTime = "2023-12-25T10:30:45Z",
            content = """<figure><img src="https://example.com/image.jpg" alt="Test"></figure><p>Article content</p>"""
        )

        // When
        val result = article.toUiState()

        // Then
        assertEquals("https://example.com/image.jpg", result.imageUrl)
        assertEquals("UI Article Title", result.title)
        assertEquals("Dec 25, 2023", result.dateTime) // Assumes toReadableDate() works
        assertEquals(
            """<figure><img src="https://example.com/image.jpg" alt="Test"></figure><p>Article content</p>""",
            result.content
        )
    }

    @Test
    fun `Article toUiState with no image returns null imageUrl`() {
        // Given
        val article = Article(
            title = "No Image Article",
            dateTime = "2023-12-25T10:30:45Z",
            content = "<p>Content without image</p>"
        )

        // When
        val result = article.toUiState()

        // Then
        assertNull(result.imageUrl)
        assertEquals("No Image Article", result.title)
        assertEquals("Dec 25, 2023", result.dateTime)
        assertEquals("<p>Content without image</p>", result.content)
    }

    @Test
    fun `Article toUiState with invalid dateTime returns original dateTime`() {
        // Given
        val article = Article(
            title = "Invalid Date Article",
            dateTime = "invalid-date",
            content = "<p>Content</p>"
        )

        // When
        val result = article.toUiState()

        // Then
        assertNull(result.imageUrl)
        assertEquals("Invalid Date Article", result.title)
        assertEquals("invalid-date", result.dateTime) // Should return original when parsing fails
        assertEquals("<p>Content</p>", result.content)
    }

    @Test
    fun `Article toUiState with empty fields maps correctly`() {
        // Given
        val article = Article(
            title = "",
            dateTime = "",
            content = ""
        )

        // When
        val result = article.toUiState()

        // Then
        assertNull(result.imageUrl)
        assertEquals("", result.title)
        assertEquals("", result.dateTime)
        assertEquals("", result.content)
    }

    @Test
    fun `Article toUiState with multiple images returns first image`() {
        // Given
        val multiImageContent = """
            <figure><img src="https://example.com/first.jpg" alt="First"></figure>
            <p>Some content</p>
            <figure><img src="https://example.com/second.jpg" alt="Second"></figure>
        """.trimIndent()

        val article = Article(
            title = "Multi Image Article",
            dateTime = "2023-12-25T10:30:45Z",
            content = multiImageContent
        )

        // When
        val result = article.toUiState()

        // Then
        assertEquals("https://example.com/first.jpg", result.imageUrl)
        assertEquals("Multi Image Article", result.title)
        assertEquals("Dec 25, 2023", result.dateTime)
        assertEquals(multiImageContent, result.content)
    }

    @Test
    fun `ArticleResponse toDomain with large dataset maps efficiently`() {
        // Given
        val articles = (1..100).map { index ->
            ArticleResponse.ItemResponse().apply {
                title = "Article $index"
                dateTime = "2023-12-25T10:30:45Z"
                content = "<p>Content for article $index</p>"
            }
        }

        val channelResponse = ArticleResponse.ChannelResponse().apply {
            this.articles = articles
        }

        val articleResponse = ArticleResponse().apply {
            channel = channelResponse
        }

        // When
        val result = articleResponse.toDomain()

        // Then
        assertEquals(100, result.size)
        assertEquals("Article 1", result.first().title)
        assertEquals("Article 100", result.last().title)
        assertTrue(result.all { it.title.isNotEmpty() })
        assertTrue(result.all { it.dateTime.isNotEmpty() })
        assertTrue(result.all { it.content.isNotEmpty() })
    }
}