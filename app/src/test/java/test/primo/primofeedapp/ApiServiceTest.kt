package test.primo.primofeedapp

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import test.primo.primofeedapp.data.ApiService
import java.net.HttpURLConnection

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getUserFeed with valid username returns parsed RSS response`() = runTest {
        // Given
        val username = "testuser"

        val mockRssResponse = """
            <rss xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
                <channel>
                    <title>Test User Stories</title>
                    <description>Test Description</description>
                    <item>
                        <title><![CDATA[First Test Article]]></title>
                        <pubDate>Wed, 12 Jun 2019 08:29:51 GMT</pubDate>
                        <atom:updated>2019-06-12T09:34:32.550Z</atom:updated>
                        <content:encoded><![CDATA[ <figure><img src="https://example.com/image1.jpg" /></figure><p>First article content</p> ]]></content:encoded>
                    </item>
                    <item>
                        <title><![CDATA[Second Test Article]]></title>
                        <pubDate>Thu, 23 May 2019 05:24:15 GMT</pubDate>
                        <atom:updated>2019-05-23T05:24:15.354Z</atom:updated>
                        <content:encoded><![CDATA[ <p>Second article content</p> ]]></content:encoded>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockRssResponse)
                .setHeader("Content-Type", "application/rss+xml")
        )

        // When
        val result = apiService.getUserFeed(username)

        // Then
        assertNotNull(result)
        assertNotNull(result.channel)
        assertEquals(2, result.channel?.articles?.size)

        val firstArticle = result.channel?.articles?.get(0)
        assertEquals("First Test Article", firstArticle?.title)
        assertEquals("2019-06-12T09:34:32.550Z", firstArticle?.dateTime)
        assertTrue(firstArticle?.content?.contains("First article content") == true)

        val secondArticle = result.channel?.articles?.get(1)
        assertEquals("Second Test Article", secondArticle?.title)
        assertEquals("2019-05-23T05:24:15.354Z", secondArticle?.dateTime)
        assertTrue(secondArticle?.content?.contains("Second article content") == true)

        // Verify HTTP request
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/feed/@testuser", request.path)
    }

    @Test
    fun `getUserFeed with empty channel returns empty articles list`() = runTest {
        // Given
        val username = "emptyuser"
        val emptyRssResponse = """
            <rss version="2.0">
                <channel>
                    <title>Empty Feed</title>
                    <description>No articles here</description>
                </channel>
            </rss>
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(emptyRssResponse)
                .setHeader("Content-Type", "application/rss+xml")
        )

        // When
        val result = apiService.getUserFeed(username)

        // Then
        assertNotNull(result)
        assertNotNull(result.channel)
        assertTrue(result.channel?.articles?.isEmpty() == true)
    }


    @Test
    fun `getUserFeed with special characters in username encodes path correctly`() = runTest {
        // Given
        val username = "test_user-123"
        val simpleRssResponse = """
            <rss xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
                <channel>
                    <item>
                        <title><![CDATA[ Test Article ]]></title>
                        <content:encoded><![CDATA[ <p>Test content</p> ]]></content:encoded>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(simpleRssResponse)
                .setHeader("Content-Type", "application/rss+xml")
        )

        // When
        apiService.getUserFeed(username)

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("/feed/@test_user-123", request.path)
    }

    @Test(expected = HttpException::class)
    fun `getUserFeed with 404 error throws HttpException`() = runTest {
        // Given
        val username = "nonexistentuser"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody("User not found")
        )

        // When & Then
        apiService.getUserFeed(username)
    }

    @Test(expected = HttpException::class)
    fun `getUserFeed with 500 server error throws HttpException`() = runTest {
        // Given
        val username = "testuser"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody("Internal server error")
        )

        // When & Then
        apiService.getUserFeed(username)
    }

    @Test(expected = HttpException::class)
    fun `getUserFeed with 401 unauthorized throws HttpException`() = runTest {
        // Given
        val username = "unauthorizeduser"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody("Unauthorized")
        )

        // When & Then
        apiService.getUserFeed(username)
    }

    @Test
    fun `getUserFeed with malformed XML handles parsing error`() = runTest {
        // Given
        val username = "testuser"
        val malformedXml = """
            <rss version="2.0">
                <channel>
                    <item>
                        <title><![CDATA[ Test Article ]]></title>
                        <content:encoded><![CDATA[ <p>Unclosed tag content
                    </item>
                </channel>
            <!-- Missing closing rss tag -->
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(malformedXml)
                .setHeader("Content-Type", "application/rss+xml")
        )

        // When & Then
        try {
            apiService.getUserFeed(username)
            fail("Expected XML parsing exception")
        } catch (e: Exception) {
            // Should throw some parsing exception
            assertTrue(e.message?.contains("XML") == true || e is RuntimeException)
        }
    }

    @Test
    fun `getUserFeed with large response handles multiple articles`() = runTest {
        // Given
        val username = "productiveuser"
        val articles = (1..10).joinToString("\n") { index ->
            """
                <item>
                    <title><![CDATA[Article $index]]></title>
                    <pubDate>Wed, 12 Jun 2019 08:29:51 GMT</pubDate>
                    <atom:updated>2019-06-12T09:34:32.550Z</atom:updated>
                    <content:encoded><![CDATA[ <p>Content for article $index with some detailed text</p> ]]></content:encoded>
                </item>
            """.trimIndent()
        }

        val largeRssResponse = """
            <rss xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
                <channel>
                    <title>Productive User Feed</title>
                    $articles
                </channel>
            </rss>
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(largeRssResponse)
                .setHeader("Content-Type", "application/rss+xml")
        )

        // When
        val result = apiService.getUserFeed(username)

        // Then
        assertNotNull(result)
        assertEquals(10, result.channel?.articles?.size)
        assertEquals("Article 1", result.channel?.articles?.first()?.title)
        assertEquals("Article 10", result.channel?.articles?.last()?.title)
        assertTrue(result.channel?.articles?.all { it.content?.isNotEmpty() == true } == true)
    }

    @Test
    fun `getUserFeed request includes proper headers`() = runTest {
        // Given
        val username = "testuser"
        val simpleRssResponse = """
            <rss version="2.0">
                <channel>
                    <item>
                        <title><![CDATA[ Test ]]></title>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(simpleRssResponse)
                .setHeader("Content-Type", "application/rss+xml")
        )

        // When
        apiService.getUserFeed(username)

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/feed/@testuser", request.path)
        assertNotNull(request.getHeader("User-Agent"))
    }

    @Test
    fun `getUserFeed with empty username handles correctly`() = runTest {
        // Given
        val username = ""
        val simpleRssResponse = """
            <rss version="2.0">
                <channel>
                    <item>
                        <title><![CDATA[ Empty Username Test ]]></title>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(simpleRssResponse)
                .setHeader("Content-Type", "application/rss+xml")
        )

        // When
        val result = apiService.getUserFeed(username)

        // Then
        assertNotNull(result)

        val request = mockWebServer.takeRequest()
        assertEquals("/feed/@", request.path)
    }
}