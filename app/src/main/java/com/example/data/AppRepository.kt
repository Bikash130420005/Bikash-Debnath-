package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val bookmarkDao: BookmarkDao,
    private val quizAttemptDao: QuizAttemptDao
) {
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val allAttempts: Flow<List<QuizAttemptEntity>> = quizAttemptDao.getAllAttempts()

    suspend fun insertBookmark(bookmark: BookmarkEntity) {
        bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun deleteBookmarkByTitle(title: String) {
        bookmarkDao.deleteBookmarkByTitle(title)
    }

    fun isBookmarked(title: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(title)
    }

    suspend fun insertAttempt(attempt: QuizAttemptEntity) {
        quizAttemptDao.insertAttempt(attempt)
    }

    suspend fun clearAllAttempts() {
        quizAttemptDao.clearAllAttempts()
    }
}
