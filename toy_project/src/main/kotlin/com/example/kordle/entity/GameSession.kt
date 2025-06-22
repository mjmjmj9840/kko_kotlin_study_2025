package com.example.kordle.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "game_sessions")
data class GameSession(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val nickname: String,
    
    @Column(nullable = false)
    val answer: String,
    
    @Column(nullable = false)
    val stage: Int = 1,  // 게임의 stage 정보
    
    @Column(nullable = false)
    var attemptsLeft: Int = 6,
    
    @Column(nullable = false)
    var isCleared: Boolean = false,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)