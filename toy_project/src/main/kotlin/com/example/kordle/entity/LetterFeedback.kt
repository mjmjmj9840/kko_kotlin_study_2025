package com.example.kordle.entity

enum class LetterFeedback {
    CORRECT,    // 정답 위치 (초록)
    PRESENT,    // 단어 내 포함 (노랑)
    ABSENT      // 불일치 (회색)
}