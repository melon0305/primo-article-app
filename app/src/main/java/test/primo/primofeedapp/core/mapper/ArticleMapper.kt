package test.primo.primofeedapp.core.mapper

import test.primo.primofeedapp.core.extention.getImageFromFigure
import test.primo.primofeedapp.core.extention.toReadableDate
import test.primo.primofeedapp.data.model.ArticleEntity
import test.primo.primofeedapp.data.model.ArticleResponse
import test.primo.primofeedapp.domain.model.Article
import test.primo.primofeedapp.presentation.screen.list.ArticleUiState

object ArticleMapper {

    fun ArticleEntity.toDomain() = Article(
        title = title,
        dateTime = dateTime,
        content = content
    )

    fun ArticleResponse.toDomain(): List<Article> =
        channel?.articles?.map {
            Article(
                title = it.title ?: "",
                dateTime = it.dateTime ?: "",
                content = it.content ?: ""
            )
        } ?: emptyList()

    fun Article.toEntity() = ArticleEntity(
        title = title,
        dateTime = dateTime,
        content = content
    )

    fun Article.toUiState() = ArticleUiState(
        imageUrl = content.getImageFromFigure(),
        title = title,
        dateTime = dateTime.toReadableDate(),
        content = content
    )
}