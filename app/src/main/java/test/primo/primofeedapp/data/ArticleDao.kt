package test.primo.primofeedapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import test.primo.primofeedapp.data.model.ArticleEntity

@Dao
interface ArticleDao {

    @Query("SELECT * FROM article")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(articles: List<ArticleEntity>)

    @Query("DELETE FROM article")
    suspend fun deleteAllArticles()

    @Transaction
    suspend fun refreshArticle(articles: List<ArticleEntity>) {
        deleteAllArticles()
        insertArticle(articles)
    }
}