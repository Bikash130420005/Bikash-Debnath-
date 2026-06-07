package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab {
    DOCS, QUIZ, BOOKMARKS
}

data class QuizState(
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswered: Boolean = false,
    val isFinished: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db.bookmarkDao(), db.quizAttemptDao())
    }

    // Reactive database streams
    val bookmarkedItems: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active bottom/navigation tab
    private val _activeTab = MutableStateFlow(MainTab.DOCS)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    // Active sub-section within DOCS (e.g., "install", "credentials")
    private val _currentSectionId = MutableStateFlow("install")
    val currentSectionId: StateFlow<String> = _currentSectionId.asStateFlow()

    // Global document search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Quiz interactive state
    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    fun setActiveTab(tab: MainTab) {
        _activeTab.value = tab
    }

    fun setSectionId(id: String) {
        _currentSectionId.value = id
        _activeTab.value = MainTab.DOCS
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Bookmarking toggle logic
    fun toggleBookmark(stepTitle: String, sectionId: String) {
        viewModelScope.launch {
            val isCurrentlyBookmarked = bookmarkedItems.value.any { it.stepTitle == stepTitle }
            if (isCurrentlyBookmarked) {
                repository.deleteBookmarkByTitle(stepTitle)
            } else {
                repository.insertBookmark(BookmarkEntity(stepTitle = stepTitle, sectionId = sectionId))
            }
        }
    }

    // Quiz functions
    fun selectOption(optionIndex: Int) {
        val current = _quizState.value
        if (!current.isAnswered) {
            _quizState.value = current.copy(selectedOptionIndex = optionIndex)
        }
    }

    fun submitAnswer() {
        val current = _quizState.value
        if (current.selectedOptionIndex != null && !current.isAnswered) {
            val isCorrect = current.selectedOptionIndex == GuideData.QUIZ_QUESTIONS[current.currentQuestionIndex].correctIndex
            val newScore = if (isCorrect) current.score + 1 else current.score
            _quizState.value = current.copy(
                score = newScore,
                isAnswered = true
            )
        }
    }

    fun nextQuestion() {
        val current = _quizState.value
        if (current.isAnswered) {
            val nextIndex = current.currentQuestionIndex + 1
            if (nextIndex < GuideData.QUIZ_QUESTIONS.size) {
                _quizState.value = QuizState(
                    currentQuestionIndex = nextIndex,
                    score = current.score
                )
            } else {
                // Submit final results to the Room database
                _quizState.value = current.copy(isFinished = true)
                viewModelScope.launch {
                    repository.insertAttempt(
                        QuizAttemptEntity(
                            score = current.score,
                            totalQuestions = GuideData.QUIZ_QUESTIONS.size
                        )
                    )
                }
            }
        }
    }

    fun restartQuiz() {
        _quizState.value = QuizState()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllAttempts()
        }
    }
}
