package com.example.kordle.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_stats")
data class UserStats(
    @Id
    val nickname: String,
    
    @Column(nullable = false)
    var totalGames: Int = 0,
    
    @Column(nullable = false)
    var wins: Int = 0,
    
    @Column(nullable = false)
    var currentStreak: Int = 0,
    
    @Column(nullable = false)
    var maxStreak: Int = 0,
    
    @Column(nullable = false)
    var completedStage: Int = 0,
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "attempt_distribution", joinColumns = [JoinColumn(name = "nickname")])
    @MapKeyColumn(name = "attempts")
    @Column(name = "count")
    var attemptDistribution: MutableMap<Int, Int> = mutableMapOf()
)