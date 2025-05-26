package test.primo.primofeedapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id") val id: Long = 0,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("datetime") val dateTime: String,
    @ColumnInfo("content") val content: String
)