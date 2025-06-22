package com.example.kordle.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "guesses")
data class Guess(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    val session: GameSession,
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guess_jamos", joinColumns = [JoinColumn(name = "guess_id")])
    @Column(name = "jamo")
    val jamos: List<Char>,
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "guess_feedback", joinColumns = [JoinColumn(name = "guess_id")])
    @Column(name = "feedback")
    val feedback: List<LetterFeedback>,
    
    @Column(nullable = false)
    val guessedAt: LocalDateTime = LocalDateTime.now()
)