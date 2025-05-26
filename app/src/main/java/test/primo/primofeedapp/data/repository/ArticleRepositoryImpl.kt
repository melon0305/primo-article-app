package test.primo.primofeedapp.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import test.primo.primofeedapp.core.mapper.ArticleMapper.toDomain
import test.primo.primofeedapp.core.mapper.ArticleMapper.toEntity
import test.primo.primofeedapp.data.datasource.ArticleLocalDataSource
import test.primo.primofeedapp.data.datasource.ArticleRemoteDataSource
import test.primo.primofeedapp.di.IoDispatcher
import test.primo.primofeedapp.domain.model.Article
import test.primo.primofeedapp.domain.repository.ArticleRepository
import javax.inject.Inject

class ArticleRepositoryImpl @Inject constructor(
    private val local: ArticleLocalDataSource,
    private val remote: ArticleRemoteDataSource,
    @IoDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ArticleRepository {

    override fun getAllArticle(username: String): Flow<List<Article>> = flow {
        var isFetch = false
        local.getAllArticle()
            .collect { articles ->
                emit(articles.map { it.toDomain() })
                //delay(500)
                if (!isFetch) {
                    isFetch = true
                    fetchArticles(username)
                }
            }
    }.flowOn(dispatcher)

    private suspend fun fetchArticles(username: String) = withContext(dispatcher) {
        val articles = remote
            .getAllArticle(username)
            .toDomain()
        local.refreshArticles(articles.map { it.toEntity() })
    }

}