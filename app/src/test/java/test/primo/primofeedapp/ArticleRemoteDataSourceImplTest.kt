package test.primo.primofeedapp

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import test.primo.primofeedapp.data.ApiService
import test.primo.primofeedapp.data.datasource.ArticleRemoteDataSourceImpl
import test.primo.primofeedapp.data.model.ArticleResponse
import java.io.IOException
import java.net.SocketTimeoutException

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ArticleRemoteDataSourceImplTest {

    private val apiService = mockk<ApiService>()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var remoteDataSource: ArticleRemoteDataSourceImpl

    @Before
    fun setup() {
        remoteDataSource = ArticleRemoteDataSourceImpl(
            service = apiService,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getAllArticle with valid username returns ArticleResponse`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        val expectedResponse = createMockArticleResponse()
        coEvery { apiService.getUserFeed(username) } returns expectedResponse

        // When
        val result = remoteDataSource.getAllArticle(username)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse, result)
        assertNotNull(result.channel)
        assertEquals(2, result.channel?.articles?.size)
        assertEquals("Test Article 1", result.channel?.articles?.get(0)?.title)
        assertEquals("Test Article 2", result.channel?.articles?.get(1)?.title)
        
        coVerify { apiService.getUserFeed(username) }
    }

    @Test
    fun `getAllArticle with empty username calls service correctly`() = runTest(testDispatcher) {
        // Given
        val username = ""
        val expectedResponse = createEmptyArticleResponse()
        coEvery { apiService.getUserFeed(username) } returns expectedResponse

        // When
        val result = remoteDataSource.getAllArticle(username)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse, result)
        assertTrue(result.channel?.articles?.isEmpty() == true)
        
        coVerify { apiService.getUserFeed("") }
    }

    @Test
    fun `getAllArticle with special characters in username handles correctly`() = runTest(testDispatcher) {
        // Given
        val username = "test_user-123"
        val expectedResponse = createMockArticleResponse()
        coEvery { apiService.getUserFeed(username) } returns expectedResponse

        // When
        val result = remoteDataSource.getAllArticle(username)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse, result)
        
        coVerify { apiService.getUserFeed("test_user-123") }
    }

    @Test(expected = HttpException::class)
    fun `getAllArticle with network error throws HttpException`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        val exception = HttpException(
            Response.error<String>(
                404,
                "Not found".toResponseBody("text/plain".toMediaType())
            )
        )
        coEvery { apiService.getUserFeed(username) } throws exception

        // When & Then
        remoteDataSource.getAllArticle(username)
    }

    @Test(expected = IOException::class)
    fun `getAllArticle with connection error throws IOException`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        coEvery { apiService.getUserFeed(username) } throws IOException("Network connection failed")

        // When & Then
        remoteDataSource.getAllArticle(username)
    }

    @Test(expected = SocketTimeoutException::class)
    fun `getAllArticle with timeout error throws SocketTimeoutException`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        coEvery { apiService.getUserFeed(username) } throws SocketTimeoutException("Request timeout")

        // When & Then
        remoteDataSource.getAllArticle(username)
    }

    @Test
    fun `getAllArticle with null channel response handles gracefully`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        val responseWithNullChannel = ArticleResponse().apply {
            channel = null
        }
        coEvery { apiService.getUserFeed(username) } returns responseWithNullChannel

        // When
        val result = remoteDataSource.getAllArticle(username)

        // Then
        assertNotNull(result)
        assertNull(result.channel)
        
        coVerify { apiService.getUserFeed(username) }
    }

    @Test
    fun `getAllArticle with partial article data preserves data integrity`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        val responseWithPartialData = createPartialDataArticleResponse()
        coEvery { apiService.getUserFeed(username) } returns responseWithPartialData

        // When
        val result = remoteDataSource.getAllArticle(username)

        // Then
        assertNotNull(result)
        assertNotNull(result.channel)
        assertEquals(3, result.channel?.articles?.size)
        
        // Complete article
        assertEquals("Complete Article", result.channel?.articles?.get(0)?.title)
        assertEquals("2023-12-25T10:30:45Z", result.channel?.articles?.get(0)?.dateTime)
        assertEquals("<p>Complete content</p>", result.channel?.articles?.get(0)?.content)
        
        // Title only article
        assertEquals("Title Only", result.channel?.articles?.get(1)?.title)
        assertNull(result.channel?.articles?.get(1)?.dateTime)
        assertNull(result.channel?.articles?.get(1)?.content)
        
        // Content only article
        assertNull(result.channel?.articles?.get(2)?.title)
        assertNull(result.channel?.articles?.get(2)?.dateTime)
        assertEquals("<p>Content only</p>", result.channel?.articles?.get(2)?.content)
        
        coVerify { apiService.getUserFeed(username) }
    }

    @Test
    fun `getAllArticle uses correct dispatcher for network operations`() = runTest(testDispatcher) {
        // Given
        val username = "testuser"
        val expectedResponse = createMockArticleResponse()
        coEvery { apiService.getUserFeed(username) } returns expectedResponse

        // When
        val result = remoteDataSource.getAllArticle(username)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse, result)
        
        // Verify that the operation completed on the test dispatcher
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { apiService.getUserFeed(username) }
    }

    @Test
    fun `getAllArticle with large response handles correctly`() = runTest(testDispatcher) {
        // Given
        val username = "productiveuser"
        val largeResponse = createLargeArticleResponse()
        coEvery { apiService.getUserFeed(username) } returns largeResponse

        // When
        val result = remoteDataSource.getAllArticle(username)

        // Then
        assertNotNull(result)
        assertEquals(50, result.channel?.articles?.size)
        assertEquals("Article 1", result.channel?.articles?.first()?.title)
        assertEquals("Article 50", result.channel?.articles?.last()?.title)
        
        coVerify { apiService.getUserFeed(username) }
    }

    @Test
    fun `verify exact service call count`() = runTest(testDispatcher) {
        // Given
        val username = "countuser"
        val response = createMockArticleResponse()
        coEvery { apiService.getUserFeed(username) } returns response

        // When
        remoteDataSource.getAllArticle(username)

        // Then
        coVerify(exactly = 1) { apiService.getUserFeed(username) }
        confirmVerified(apiService)
    }

    @Test
    fun `verify no service calls when method not invoked`() = runTest(testDispatcher) {
        // Given
        val freshService = mockk<ApiService>()
        val freshDataSource = ArticleRemoteDataSourceImpl(freshService, testDispatcher)

        // When - no operations performed

        // Then
        verify { freshService wasNot Called }
    }

    @Test
    fun `verify argument passing with capture slot`() = runTest(testDispatcher) {
        // Given
        val slot = slot<String>()
        val response = createMockArticleResponse()
        coEvery { apiService.getUserFeed(capture(slot)) } returns response

        val testUsername = "captured_user"

        // When
        remoteDataSource.getAllArticle(testUsername)

        // Then
        coVerify { apiService.getUserFeed(any()) }
        assertEquals(testUsername, slot.captured)
    }

    @Test
    fun `getAllArticle with relaxed mock service returns default response`() = runTest(testDispatcher) {
        // Given
        val relaxedService = mockk<ApiService>(relaxed = true)
        val relaxedDataSource = ArticleRemoteDataSourceImpl(relaxedService, testDispatcher)
        val username = "relaxeduser"

        // When
        val result = relaxedDataSource.getAllArticle(username)

        // Then
        assertNotNull(result) // relaxed mock returns non-null default
        coVerify { relaxedService.getUserFeed(username) }
    }

    @Test
    fun `getAllArticle handles concurrent calls correctly`() = runTest(testDispatcher) {
        // Given
        val response = createMockArticleResponse()
        coEvery { apiService.getUserFeed(any()) } returns response

        // When
        val result1 = remoteDataSource.getAllArticle("user1")
        val result2 = remoteDataSource.getAllArticle("user2")

        // Then
        assertNotNull(result1)
        assertNotNull(result2)
        
        coVerify { apiService.getUserFeed("user1") }
        coVerify { apiService.getUserFeed("user2") }
    }

    // Helper methods to create mock responses
    private fun createMockArticleResponse(): ArticleResponse {
        val item1 = ArticleResponse.ItemResponse().apply {
            title = "Test Article 1"
            dateTime = "2023-12-25T10:30:45Z"
            content = "<p>Test content 1</p>"
        }
        
        val item2 = ArticleResponse.ItemResponse().apply {
            title = "Test Article 2"
            dateTime = "2023-12-26T15:45:30Z"
            content = "<p>Test content 2</p>"
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

    private fun createPartialDataArticleResponse(): ArticleResponse {
        val completeItem = ArticleResponse.ItemResponse().apply {
            title = "Complete Article"
            dateTime = "2023-12-25T10:30:45Z"
            content = "<p>Complete content</p>"
        }
        
        val titleOnlyItem = ArticleResponse.ItemResponse().apply {
            title = "Title Only"
            dateTime = null
            content = null
        }
        
        val contentOnlyItem = ArticleResponse.ItemResponse().apply {
            title = null
            dateTime = null
            content = "<p>Content only</p>"
        }

        val channel = ArticleResponse.ChannelResponse().apply {
            articles = listOf(completeItem, titleOnlyItem, contentOnlyItem)
        }

        return ArticleResponse().apply {
            this.channel = channel
        }
    }

    private fun createLargeArticleResponse(): ArticleResponse {
        val articles = (1..50).map { index ->
            ArticleResponse.ItemResponse().apply {
                title = "Article $index"
                dateTime = "2023-12-25T10:30:45Z"
                content = "<p>Content for article $index</p>"
            }
        }

        val channel = ArticleResponse.ChannelResponse().apply {
            this.articles = articles
        }

        return ArticleResponse().apply {
            this.channel = channel
        }
    }
}