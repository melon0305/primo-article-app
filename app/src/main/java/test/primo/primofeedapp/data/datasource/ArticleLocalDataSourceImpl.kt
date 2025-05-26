package test.primo.primofeedapp.data.datasource

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import test.primo.primofeedapp.data.ArticleDao
import test.primo.primofeedapp.data.model.ArticleEntity
import test.primo.primofeedapp.di.IoDispatcher
import javax.inject.Inject

class ArticleLocalDataSourceImpl @Inject constructor(
    private val articleDao: ArticleDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ArticleLocalDataSource {

    override fun getAllArticle(): Flow<List<ArticleEntity>> =
        articleDao.getAllArticles()
            .flowOn(ioDispatcher)

    override suspend fun refreshArticles(article: List<ArticleEntity>) =
        withContext(ioDispatcher) {
            articleDao.refreshArticle(article)
        }

}