package test.primo.primofeedapp.core.extention

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.toReadableDate(
    pattern: String = "MMM dd, yyyy",
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    return try {
        val instant = Instant.parse(this)
        val localDateTime = instant.atZone(zoneId).toLocalDateTime()
        localDateTime.format(DateTimeFormatter.ofPattern(pattern))
    } catch (e: Exception) {
        this
    }
}

fun String.extractParagraphs(): String {
    return try {
        val paragraphRegex = Regex("<p[^>]*>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
        val paragraphs = paragraphRegex.findAll(this)
            .map { it.groupValues[1].trim() }
            .filter { it.isNotEmpty() }
            .joinToString("</p><p>")
        paragraphs.ifEmpty { this }
    } catch (e: Exception) {
        this
    }
}

fun String.getImageFromFigure(): String? {
    return try {
        Regex("<figure.*?<img.*?src=\"([^\"]+)\".*?</figure>").find(this)?.groupValues?.get(1)
    } catch (e: Exception) {
        null
    }
}