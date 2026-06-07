package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val stepTitle: String,
    val sectionId: String,
    val bookmarkedAt: Long = System.currentTimeMillis()
)

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE stepTitle = :stepTitle")
    suspend fun deleteBookmarkByTitle(stepTitle: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE stepTitle = :stepTitle LIMIT 1)")
    fun isBookmarked(stepTitle: String): Flow<Boolean>
}
