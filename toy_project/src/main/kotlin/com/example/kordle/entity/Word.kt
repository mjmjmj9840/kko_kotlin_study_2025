package com.example.kordle.entity

import jakarta.persistence.*

@Entity
@Table(name = "words")
data class Word(
    @Id
    val text: String,
    val stage: Int = 1,
    @Column(nullable = false)
    val answer: String = ""
)