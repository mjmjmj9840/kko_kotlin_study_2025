package com.example.kordle.service

import com.example.kordle.dto.GuessRequest
import com.example.kordle.dto.StartGameRequest
import com.example.kordle.entity.*
import com.example.kordle.repository.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.assertThrows
import java.util.*

@ExtendWith(MockKExtension::class)
class GameServiceTest {

    @MockK
    private lateinit var gameSessionRepository: GameSessionRepository

    @MockK
    private lateinit var guessRepository: GuessRepository

    @MockK
    private lateinit var wordService: WordService

    @MockK
    private lateinit var statsService: StatsService

    @InjectMockKs
    private lateinit var gameService: GameService

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `게임 세션 생성 성공 테스트`() {
        // given
        val request = StartGameRequest("테스트유저")
        val word = Word(text = "코들", answer = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]", stage = 1)
        val gameSession = GameSession(id = 1L, nickname = "테스트유저", answer = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]", stage = 1)

        every { statsService.getNextStage("테스트유저") } returns 1
        every { wordService.getWordByStage(1) } returns word
        every { gameSessionRepository.save(any()) } returns gameSession

        // when
        val response = gameService.createSession(request)

        // then
        assertEquals(1L, response.sessionId)
        assertEquals(6, response.maxAttempts)
        assertEquals(1, response.currentStage)
        assertNotNull(response.wordHint)

        verify { gameSessionRepository.save(any()) }
        verify { statsService.getNextStage("테스트유저") }
        verify { wordService.getWordByStage(1) }
    }

    @Test
    fun `빈 닉네임으로 게임 세션 생성 실패 테스트`() {
        // given
        val request = StartGameRequest("")

        // when & then
        assertThrows<IllegalArgumentException> {
            gameService.createSession(request)
        }
    }

    @Test
    fun `긴 닉네임으로 게임 세션 생성 실패 테스트`() {
        // given
        val request = StartGameRequest("매우긴닉네임테스트입니다열글자초과")

        // when & then
        assertThrows<IllegalArgumentException> {
            gameService.createSession(request)
        }
    }

    @Test
    fun `추측 처리 성공 테스트 - 정답`() {
        // given
        val sessionId = 1L
        val request = GuessRequest(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'))
        val gameSession = GameSession(
            id = sessionId,
            nickname = "테스트유저",
            answer = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]",
            stage = 1,
            attemptsLeft = 6,
            isCleared = false
        )
        val updatedSession = gameSession.copy(attemptsLeft = 5, isCleared = true)

        every { gameSessionRepository.findById(sessionId) } returns Optional.of(gameSession)
        every { gameSessionRepository.save(any()) } returns updatedSession
        every { guessRepository.save(any()) } returns mockk()
        every { statsService.updateStats(any(), any(), any(), any()) } just runs

        // when
        val response = gameService.processGuess(sessionId, request)

        // then
        assertEquals(
            listOf(
                LetterFeedback.CORRECT, LetterFeedback.CORRECT, LetterFeedback.CORRECT,
                LetterFeedback.CORRECT, LetterFeedback.CORRECT, LetterFeedback.CORRECT
            ), response.feedback
        )
        assertEquals(5, response.attemptsLeft)
        assertTrue(response.isCleared)
        assertTrue(response.isGameOver)

        verify { gameSessionRepository.save(any()) }
        verify { guessRepository.save(any()) }
        verify { statsService.updateStats("테스트유저", true, 1, 1) }
    }

    @Test
    fun `추측 처리 성공 테스트 - 부분 정답`() {
        // given
        val sessionId = 1L
        val request = GuessRequest(listOf('ㅋ', 'ㅏ', 'ㄷ', 'ㅗ', 'ㄹ', 'ㅇ'))  // 일부만 맞음
        val gameSession = GameSession(
            id = sessionId,
            nickname = "테스트유저",
            answer = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]",
            stage = 1,
            attemptsLeft = 6,
            isCleared = false
        )
        val updatedSession = gameSession.copy(attemptsLeft = 5, isCleared = false)

        every { gameSessionRepository.findById(sessionId) } returns Optional.of(gameSession)
        every { gameSessionRepository.save(any()) } returns updatedSession
        every { guessRepository.save(any()) } returns mockk()

        // when
        val response = gameService.processGuess(sessionId, request)

        // then - 피드백 검증 (정확한 로직에 따라)
        assertEquals(6, response.feedback.size)
        assertEquals(5, response.attemptsLeft)
        assertFalse(response.isCleared)
        assertFalse(response.isGameOver)
    }

    @Test
    fun `존재하지 않는 세션으로 추측 처리 실패 테스트`() {
        // given
        val sessionId = 999L
        val request = GuessRequest(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'))

        every { gameSessionRepository.findById(sessionId) } returns Optional.empty()

        // when & then
        assertThrows<IllegalArgumentException> {
            gameService.processGuess(sessionId, request)
        }
    }

    @Test
    fun `이미 종료된 게임에 추측 처리 실패 테스트`() {
        // given
        val sessionId = 1L
        val request = GuessRequest(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'))
        val completedSession = GameSession(
            id = sessionId,
            nickname = "테스트유저",
            answer = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]",
            stage = 1,
            attemptsLeft = 0,
            isCleared = true
        )

        every { gameSessionRepository.findById(sessionId) } returns Optional.of(completedSession)

        // when & then
        assertThrows<IllegalStateException> {
            gameService.processGuess(sessionId, request)
        }
    }

    @Test
    fun `잘못된 자모 개수로 추측 처리 실패 테스트`() {
        // given
        val sessionId = 1L
        val request = GuessRequest(listOf('ㅋ', 'ㅗ', 'ㄷ'))  // 3개만
        val gameSession = GameSession(
            id = sessionId,
            nickname = "테스트유저",
            answer = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]",
            stage = 1,
            attemptsLeft = 6,
            isCleared = false
        )

        every { gameSessionRepository.findById(sessionId) } returns Optional.of(gameSession)

        // when & then
        assertThrows<IllegalArgumentException> {
            gameService.processGuess(sessionId, request)
        }
    }

    @Test
    fun `게임 기록 조회 성공 테스트`() {
        // given
        val sessionId = 1L
        val guesses = listOf(
            Guess(
                id = 1L, session = mockk(), jamos = listOf('ㅋ', 'ㅏ', 'ㄷ', 'ㅗ', 'ㄹ', 'ㅇ'),
                feedback = listOf(
                    LetterFeedback.CORRECT, LetterFeedback.PRESENT, LetterFeedback.CORRECT,
                    LetterFeedback.PRESENT, LetterFeedback.CORRECT, LetterFeedback.CORRECT
                )
            )
        )

        every { gameSessionRepository.existsById(sessionId) } returns true
        every { guessRepository.findBySessionIdOrderByGuessedAtAsc(sessionId) } returns guesses

        // when
        val history = gameService.getHistory(sessionId)

        // then
        assertEquals(1, history.size)
        assertEquals(listOf('ㅋ', 'ㅏ', 'ㄷ', 'ㅗ', 'ㄹ', 'ㅇ'), history[0].jamos)
        assertEquals(6, history[0].feedback.size)
    }

    @Test
    fun `존재하지 않는 세션의 게임 기록 조회 실패 테스트`() {
        // given
        val sessionId = 999L

        every { gameSessionRepository.existsById(sessionId) } returns false

        // when & then
        assertThrows<IllegalArgumentException> {
            gameService.getHistory(sessionId)
        }
    }

    @Test
    fun `세션 유효성 검증 테스트`() {
        // given
        val validSessionId = 1L
        val invalidSessionId = 999L

        every { gameSessionRepository.existsById(validSessionId) } returns true
        every { gameSessionRepository.existsById(invalidSessionId) } returns false

        // when & then
        assertTrue(gameService.validateSession(validSessionId))
        assertFalse(gameService.validateSession(invalidSessionId))
    }

    @Test
    fun `게임 통계 조회 테스트`() {
        // given
        every { gameSessionRepository.count() } returns 100L
        every { gameSessionRepository.countByIsCleared(true) } returns 60L
        every { gameSessionRepository.countByAttemptsLeftGreaterThanAndIsCleared(0, false) } returns 25L

        // when
        val stats = gameService.getGameStatistics()

        // then
        assertEquals(100L, stats["totalSessions"])
        assertEquals(60L, stats["completedSessions"])
        assertEquals(25L, stats["activeSessions"])
        assertEquals(60.0, stats["completionRate"])
    }
} 