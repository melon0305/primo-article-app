package test.primo.primofeedapp.data.datasource

import kotlinx.coroutines.flow.Flow
import test.primo.primofeedapp.data.model.ArticleEntity

interface ArticleLocalDataSource {

    fun getAllArticle(): Flow<List<ArticleEntity>>

    suspend fun refreshArticles(article: List<ArticleEntity>)

}