package com.example.kordle.service

import com.example.kordle.dto.UserStatsResponse
import com.example.kordle.dto.DashboardResponse
import com.example.kordle.entity.UserStats
import com.example.kordle.repository.UserStatsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StatsService(
    private val userStatsRepository: UserStatsRepository
) {
    
    fun updateStats(nickname: String, isWin: Boolean, attempts: Int, currentStage: Int = 1) {
        val stats = userStatsRepository.findById(nickname)
            .orElse(UserStats(nickname = nickname))
            
        stats.totalGames++
        
        if (isWin) {
            stats.wins++
            stats.currentStreak++
            stats.maxStreak = maxOf(stats.maxStreak, stats.currentStreak)
            
            if (currentStage > stats.completedStage) {
                stats.completedStage = currentStage
            }
            
            val currentCount = stats.attemptDistribution[attempts] ?: 0
            stats.attemptDistribution[attempts] = currentCount + 1
        } else {
            stats.currentStreak = 0
        }
        
        userStatsRepository.save(stats)
    }
    
    @Transactional(readOnly = true)
    fun getStats(nickname: String): UserStatsResponse {
        val stats = userStatsRepository.findById(nickname)
            .orElse(UserStats(nickname = nickname))
            
        val winRate = if (stats.totalGames > 0) {
            stats.wins.toDouble() / stats.totalGames.toDouble() * 100
        } else {
            0.0
        }
        
        return UserStatsResponse(
            nickname = stats.nickname,
            totalGames = stats.totalGames,
            wins = stats.wins,
            winRate = winRate,
            currentStreak = stats.currentStreak,
            maxStreak = stats.maxStreak,
            attemptDistribution = stats.attemptDistribution,
            completedStage = stats.completedStage
        )
    }
    
    @Transactional(readOnly = true)
    fun getCompletedStage(nickname: String): Int {
        return userStatsRepository.findById(nickname)
            .map { it.completedStage }
            .orElse(0)
    }
    
    @Transactional(readOnly = true)
    fun getNextStage(nickname: String): Int {
        val completedStage = getCompletedStage(nickname)
        return completedStage + 1
    }
    
    fun resetProgress(nickname: String) {
        userStatsRepository.findById(nickname).ifPresent { stats ->
            stats.completedStage = 0
            userStatsRepository.save(stats)
        }
    }
    
    @Transactional(readOnly = true)
    fun getDashboard(): List<DashboardResponse> {
        return userStatsRepository.findAll()
            .filter { it.totalGames > 0 }  // 게임을 한 번이라도 플레이한 사용자만
            .map { stats ->
                val winRate = if (stats.totalGames > 0) {
                    stats.wins.toDouble() / stats.totalGames.toDouble() * 100
                } else {
                    0.0
                }
                
                DashboardResponse(
                    nickname = stats.nickname,
                    totalGames = stats.totalGames,
                    wins = stats.wins,
                    winRate = winRate,
                    maxStageCleared = stats.completedStage
                )
            }
    }
}