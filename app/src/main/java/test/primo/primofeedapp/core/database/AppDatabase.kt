package test.primo.primofeedapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import test.primo.primofeedapp.data.ArticleDao
import test.primo.primofeedapp.data.model.ArticleEntity

@Database(
    entities = [
        ArticleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao
}