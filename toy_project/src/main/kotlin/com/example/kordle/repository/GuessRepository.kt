package com.example.kordle.repository

import com.example.kordle.entity.Guess
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GuessRepository : JpaRepository<Guess, Long> {

    fun findBySessionIdOrderByGuessedAtAsc(sessionId: Long): List<Guess>
}