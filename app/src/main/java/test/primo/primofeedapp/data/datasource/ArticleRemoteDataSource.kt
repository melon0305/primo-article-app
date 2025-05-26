package test.primo.primofeedapp.data.datasource

import test.primo.primofeedapp.data.model.ArticleResponse

interface ArticleRemoteDataSource {

    suspend fun getAllArticle(username: String): ArticleResponse
}