package test.primo.primofeedapp.data

import retrofit2.http.GET
import retrofit2.http.Path
import test.primo.primofeedapp.data.model.ArticleResponse

interface ApiService {

    @GET("feed/@{username}")
    suspend fun getUserFeed(@Path("username") username: String) : ArticleResponse

}