package test.primo.primofeedapp.data.datasource

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import test.primo.primofeedapp.data.ApiService
import test.primo.primofeedapp.data.model.ArticleResponse
import test.primo.primofeedapp.di.IoDispatcher
import javax.inject.Inject

class ArticleRemoteDataSourceImpl @Inject constructor(
    private val service: ApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ArticleRemoteDataSource {

    override suspend fun getAllArticle(username: String): ArticleResponse = withContext(ioDispatcher) {
        service.getUserFeed(username)
    }
}