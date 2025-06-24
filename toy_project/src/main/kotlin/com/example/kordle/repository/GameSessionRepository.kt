package com.example.kordle.repository

import com.example.kordle.entity.GameSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GameSessionRepository : JpaRepository<GameSession, Long> {
    
    // 게임 완료 여부별 카운트
    fun countByIsCleared(isCleared: Boolean): Long
    
    // 활성 게임 세션 카운트 (시도 횟수가 남아있고 아직 클리어되지 않은 게임)
    fun countByAttemptsLeftGreaterThanAndIsCleared(attemptsLeft: Int, isCleared: Boolean): Long
}