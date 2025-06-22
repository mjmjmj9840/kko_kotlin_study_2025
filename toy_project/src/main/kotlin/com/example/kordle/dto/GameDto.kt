package com.example.kordle.dto

import com.example.kordle.entity.LetterFeedback

data class StartGameRequest(
    val nickname: String
)

data class StartGameResponse(
    val sessionId: Long,
    val maxAttempts: Int = 6,
    val currentStage: Int = 1,
    val wordHint: String? = null
)

data class GuessRequest(
    val jamos: List<Char>
)

data class GuessResponse(
    val feedback: List<LetterFeedback>,
    val attemptsLeft: Int,
    val isCleared: Boolean,
    val isGameOver: Boolean = false,
    val cumulativeFeedback: Map<Char, LetterFeedback> = emptyMap()
)

data class GuessHistoryResponse(
    val jamos: List<Char>,
    val feedback: List<LetterFeedback>
)

data class UserStatsResponse(
    val nickname: String,
    val totalGames: Int,
    val wins: Int,
    val winRate: Double,
    val currentStreak: Int,
    val maxStreak: Int,
    val attemptDistribution: Map<Int, Int>,
    val completedStage: Int = 0
)

data class DashboardResponse(
    val nickname: String,
    val totalGames: Int,
    val wins: Int,
    val winRate: Double,
    val maxStageCleared: Int
)