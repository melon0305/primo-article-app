package test.primo.primofeedapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import test.primo.primofeedapp.core.extention.extractParagraphs
import test.primo.primofeedapp.core.extention.getImageFromFigure
import test.primo.primofeedapp.core.extention.toReadableDate
import java.time.ZoneId

class StringExtensionsTest {

    @Test
    fun `toReadableDate with valid ISO string returns formatted date`() {
        val isoString = "2023-12-25T10:30:45Z"
        val result = isoString.toReadableDate()
        assertEquals("Dec 25, 2023", result)
    }

    @Test
    fun `toReadableDate with valid ISO string and custom pattern returns custom formatted date`() {
        val isoString = "2023-12-25T10:30:45Z"
        val customPattern = "dd/MM/yyyy"
        val result = isoString.toReadableDate(customPattern)
        assertEquals("25/12/2023", result)
    }

    @Test
    fun `toReadableDate with valid ISO string and custom timezone returns formatted date in timezone`() {
        val isoString = "2023-12-25T10:30:45Z"
        val customZone = ZoneId.of("America/New_York")
        val result = isoString.toReadableDate(zoneId = customZone)
        assertEquals("Dec 25, 2023", result)
    }

    @Test
    fun `toReadableDate with custom pattern and timezone returns correctly formatted date`() {
        val isoString = "2023-12-25T10:30:45Z"
        val customPattern = "yyyy-MM-dd HH:mm"
        val customZone = ZoneId.of("UTC")
        val result = isoString.toReadableDate(customPattern, customZone)
        assertEquals("2023-12-25 10:30", result)
    }

    @Test
    fun `toReadableDate with invalid date string returns original string`() {
        val invalidString = "invalid-date"
        val result = invalidString.toReadableDate()
        assertEquals("invalid-date", result)
    }

    @Test
    fun `toReadableDate with empty string returns empty string`() {
        val emptyString = ""
        val result = emptyString.toReadableDate()
        assertEquals("", result)
    }

    @Test
    fun `toReadableDate with malformed ISO string returns original string`() {
        val malformedString = "2023-12-25T10:30:45"
        val result = malformedString.toReadableDate()
        assertEquals("2023-12-25T10:30:45", result)
    }

    @Test
    fun `extractParagraphs with valid HTML returns paragraphs content`() {
        val htmlString = "<div><p>First paragraph</p><p>Second paragraph</p></div>"
        val result = htmlString.extractParagraphs()
        assertEquals("First paragraph</p><p>Second paragraph", result)
    }

    @Test
    fun `extractParagraphs with nested HTML tags returns cleaned content`() {
        val htmlString =
            "<p>This is <strong>bold</strong> text</p><p>Another <em>italic</em> paragraph</p>"
        val result = htmlString.extractParagraphs()
        assertEquals(
            "This is <strong>bold</strong> text</p><p>Another <em>italic</em> paragraph",
            result
        )
    }

    @Test
    fun `extractParagraphs with paragraph attributes returns content without attributes`() {
        val htmlString =
            """<p class="highlight" id="para1">First paragraph</p><p style="color: red;">Second paragraph</p>"""
        val result = htmlString.extractParagraphs()
        assertEquals("First paragraph</p><p>Second paragraph", result)
    }

    @Test
    fun `extractParagraphs with empty paragraphs filters them out`() {
        val htmlString = "<p>Valid paragraph</p><p></p><p>   </p><p>Another valid paragraph</p>"
        val result = htmlString.extractParagraphs()
        assertEquals("Valid paragraph</p><p>Another valid paragraph", result)
    }

    @Test
    fun `extractParagraphs with no paragraphs returns original string`() {
        val htmlString = "<div>No paragraphs here</div>"
        val result = htmlString.extractParagraphs()
        assertEquals("<div>No paragraphs here</div>", result)
    }

    @Test
    fun `extractParagraphs with empty string returns empty string`() {
        val emptyString = ""
        val result = emptyString.extractParagraphs()
        assertEquals("", result)
    }

    @Test
    fun `extractParagraphs with multiline paragraph content returns content`() {
        val htmlString = """<p>This is a paragraph
        with multiple lines
        of content</p>"""
        val result = htmlString.extractParagraphs()
        assertEquals("This is a paragraph\n        with multiple lines\n        of content", result)
    }

    @Test
    fun `getImageFromFigure with valid figure tag returns image src`() {
        val htmlString =
            """<figure><img src="https://example.com/image.jpg" alt="Example"></figure>"""
        val result = htmlString.getImageFromFigure()
        assertEquals("https://example.com/image.jpg", result)
    }

    @Test
    fun `getImageFromFigure with figure attributes returns image src`() {
        val htmlString =
            """<figure id="fig1" class="main-figure"><img src="/assets/local-image.gif" alt="Local image"></figure>"""
        val result = htmlString.getImageFromFigure()
        assertEquals("/assets/local-image.gif", result)
    }

    @Test
    fun `getImageFromFigure with img having multiple attributes returns src`() {
        val htmlString =
            """<figure><img class="thumbnail" id="img1" src="https://cdn.example.com/thumb.webp" alt="Thumbnail" width="100" height="100"></figure>"""
        val result = htmlString.getImageFromFigure()
        assertEquals("https://cdn.example.com/thumb.webp", result)
    }

    @Test
    fun `getImageFromFigure with no figure tag returns null`() {
        val htmlString = """<div><img src="https://example.com/image.jpg" alt="Example"></div>"""
        val result = htmlString.getImageFromFigure()
        assertNull(result)
    }

    @Test
    fun `getImageFromFigure with figure but no img tag returns null`() {
        val htmlString = """<figure><div>No image here</div></figure>"""
        val result = htmlString.getImageFromFigure()
        assertNull(result)
    }

    @Test
    fun `getImageFromFigure with empty string returns null`() {
        val emptyString = ""
        val result = emptyString.getImageFromFigure()
        assertNull(result)
    }

    @Test
    fun `getImageFromFigure with malformed HTML returns null`() {
        val malformedString = """<figure><img src="image.jpg" alt="Example"""
        val result = malformedString.getImageFromFigure()
        assertNull(result)
    }

    @Test
    fun `getImageFromFigure with multiple figures returns first image src`() {
        val htmlString = """
            <figure><img src="https://example.com/first.jpg" alt="First"></figure>
            <figure><img src="https://example.com/second.jpg" alt="Second"></figure>
        """
        val result = htmlString.getImageFromFigure()
        assertEquals("https://example.com/first.jpg", result)
    }

    @Test
    fun `getImageFromFigure with img tag having src with quotes returns correct src`() {
        val htmlString =
            """<figure><img src='https://example.com/single-quote.jpg' alt="Example"></figure>"""
        val result = htmlString.getImageFromFigure()
        assertNull(result)
    }
}