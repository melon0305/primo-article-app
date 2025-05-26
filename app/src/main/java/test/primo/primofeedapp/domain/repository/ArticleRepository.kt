package test.primo.primofeedapp.domain.repository

import kotlinx.coroutines.flow.Flow
import test.primo.primofeedapp.domain.model.Article

interface ArticleRepository {

    fun getAllArticle(username: String): Flow<List<Article>>
}