package com.example.kordle.service

import com.example.kordle.dto.*
import com.example.kordle.entity.*
import com.example.kordle.repository.*
import com.example.kordle.util.HangulUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GameService(
    private val gameSessionRepository: GameSessionRepository,
    private val guessRepository: GuessRepository,
    private val wordService: WordService,
    private val statsService: StatsService
) {
    
    fun createSession(request: StartGameRequest): StartGameResponse {
        return try {
            // 닉네임 검증
            val nickname = request.nickname.trim()
            if (nickname.isBlank()) {
                throw IllegalArgumentException("닉네임이 비어있습니다.")
            }
            if (nickname.length > 10) {
                throw IllegalArgumentException("닉네임은 10자 이하여야 합니다.")
            }
            
            // 사용자의 다음 도전 stage 확인
            val nextStage = statsService.getNextStage(nickname)
            
            // 해당 stage의 단어 조회
            val wordEntity = getWordForStage(nextStage)
                ?: throw IllegalStateException("Stage $nextStage 에 해당하는 단어를 찾을 수 없습니다.")
            
            // 단어 데이터의 유효성 재검증 (null 안전 처리)
            val safeAnswer = wordEntity.answer.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("단어의 자모 데이터가 비어있습니다: ${wordEntity.text}")
            
            val validation = HangulUtils.validateWordData(wordEntity.text, safeAnswer)
            if (!validation.isValid) {
                throw IllegalStateException("선택된 단어 데이터가 유효하지 않습니다: ${validation.message}")
            }
            
            val session = GameSession(
                nickname = nickname,
                answer = wordEntity.answer,
                stage = wordEntity.stage  // stage 정보도 저장
            )
            
            val savedSession = gameSessionRepository.save(session)
            
            StartGameResponse(
                sessionId = savedSession.id,
                maxAttempts = 6,
                currentStage = wordEntity.stage,
                wordHint = "Stage ${wordEntity.stage}: ${wordEntity.text.length}글자 단어"
            )
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException("게임 세션 생성 실패: ${e.message}", e)
        }
    }
    
    /**
     * 특정 stage에 해당하는 단어를 조회
     */
    private fun getWordForStage(stage: Int): Word? {
        // 먼저 해당 stage의 단어가 있는지 확인
        val stageWord = wordService.getWordByStage(stage)
        if (stageWord != null) {
            return stageWord
        }
        
        // 해당 stage가 없으면 최대 stage 확인
        val maxStage = wordService.getMaxStage()
        if (maxStage == null || maxStage == 0) {
            // stage 개념이 없는 경우 랜덤 단어 반환
            return wordService.getRandomWord()
        }
        
        // 사용자가 모든 stage를 완료한 경우 랜덤 단어 제공
        if (stage > maxStage) {
            return wordService.getRandomWord()
        }
        
        // 그 외의 경우 1stage부터 다시 시작
        return wordService.getWordByStage(1)
    }
    
    fun processGuess(sessionId: Long, request: GuessRequest): GuessResponse {
        return try {
            val session = gameSessionRepository.findById(sessionId)
                .orElseThrow { IllegalArgumentException("게임 세션을 찾을 수 없습니다: $sessionId") }
                
            if (session.isCleared || session.attemptsLeft <= 0) {
                throw IllegalStateException("게임이 이미 종료되었습니다.")
            }
            
            // 6글자 자모 검증
            if (request.jamos.size != 6) {
                throw IllegalArgumentException("정확히 6개의 자모를 입력해야 합니다. 현재: ${request.jamos.size}개")
            }
            
            // 자모 유효성 검증
            if (!HangulUtils.validateJamos(request.jamos)) {
                throw IllegalArgumentException("유효하지 않은 자모가 포함되어 있습니다.")
            }
            
            // 빈 자모 검사
            if (request.jamos.any { it == ' ' || it.isWhitespace() }) {
                throw IllegalArgumentException("모든 자모를 입력해야 합니다.")
            }
            
            // 세션에 저장된 정답 자모 파싱 및 검증
            val answerJamos = try {
                HangulUtils.parseJamosFromString(session.answer)
            } catch (e: Exception) {
                throw IllegalStateException("정답 데이터가 손상되었습니다: ${e.message}")
            }
            
            val feedback = generateJamoFeedback(request.jamos, answerJamos)
            val isCorrect = feedback.all { it == LetterFeedback.CORRECT }
            
            // 세션 상태 업데이트
            session.attemptsLeft--
            if (isCorrect) {
                session.isCleared = true
            }
            
            val guess = Guess(
                session = session,
                jamos = request.jamos,
                feedback = feedback
            )
            
            // 데이터베이스 저장
            val savedSession = gameSessionRepository.save(session)
            guessRepository.save(guess)
            
            val isGameOver = savedSession.isCleared || savedSession.attemptsLeft <= 0
            
            // 누적 피드백 계산
            val cumulativeFeedback = calculateCumulativeFeedback(savedSession.id)
            
            // 게임 종료 시 통계 업데이트
            if (isGameOver) {
                try {
                    statsService.updateStats(
                        nickname = savedSession.nickname, 
                        isWin = savedSession.isCleared, 
                        attempts = 6 - savedSession.attemptsLeft,
                        currentStage = savedSession.stage
                    )
                } catch (e: Exception) {
                    // 통계 업데이트 실패는 게임 진행에 영향을 주지 않음
                    println("통계 업데이트 중 오류 발생: ${e.message}")
                }
            }
            
            GuessResponse(
                feedback = feedback,
                attemptsLeft = savedSession.attemptsLeft,
                isCleared = savedSession.isCleared,
                isGameOver = isGameOver,
                cumulativeFeedback = cumulativeFeedback
            )
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException("추측 처리 중 오류가 발생했습니다: ${e.message}", e)
        }
    }
    
    private fun generateJamoFeedback(guessJamos: List<Char>, answerJamos: List<Char>): List<LetterFeedback> {
        if (guessJamos.size != 6 || answerJamos.size != 6) {
            throw IllegalArgumentException("추측과 정답 모두 6개의 자모여야 합니다.")
        }
        
        val feedback = mutableListOf<LetterFeedback>()
        val answerJamosCopy = answerJamos.toMutableList()
        
        // 첫 번째 패스: 정확한 위치 체크
        for (i in guessJamos.indices) {
            if (guessJamos[i] == answerJamos[i]) {
                feedback.add(LetterFeedback.CORRECT)
                answerJamosCopy[i] = ' ' // 이미 사용된 자모 표시
            } else {
                feedback.add(LetterFeedback.ABSENT) // 일단 ABSENT로 설정
            }
        }
        
        // 두 번째 패스: 잘못된 위치 체크
        for (i in guessJamos.indices) {
            if (feedback[i] == LetterFeedback.ABSENT) {
                val jamoIndex = answerJamosCopy.indexOf(guessJamos[i])
                if (jamoIndex != -1) {
                    feedback[i] = LetterFeedback.PRESENT
                    answerJamosCopy[jamoIndex] = ' ' // 사용된 자모 표시
                }
            }
        }
        
        return feedback
    }
    
    @Transactional(readOnly = true)
    fun getHistory(sessionId: Long): List<GuessHistoryResponse> {
        return try {
            // 세션 존재 여부 확인
            if (!gameSessionRepository.existsById(sessionId)) {
                throw IllegalArgumentException("게임 세션을 찾을 수 없습니다: $sessionId")
            }
                
            guessRepository.findBySessionIdOrderByGuessedAtAsc(sessionId)
                .map { GuessHistoryResponse(it.jamos, it.feedback) }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException("게임 기록 조회 중 오류가 발생했습니다: ${e.message}", e)
        }
    }
    
    /**
     * 세션 유효성 검증
     */
    @Transactional(readOnly = true)
    fun validateSession(sessionId: Long): Boolean {
        return try {
            gameSessionRepository.existsById(sessionId)
        } catch (e: Exception) {
            println("세션 유효성 검증 중 오류: ${e.message}")
            false
        }
    }
    
    /**
     * 세션 정보 조회 (디버깅용)
     */
    @Transactional(readOnly = true)
    fun getSessionInfo(sessionId: Long): GameSession? {
        return try {
            gameSessionRepository.findById(sessionId).orElse(null)
        } catch (e: Exception) {
            println("세션 정보 조회 중 오류: ${e.message}")
            null
        }
    }
    
    /**
     * 게임 통계 (전체)
     */
    @Transactional(readOnly = true)
    fun getGameStatistics(): Map<String, Any> {
        return try {
            val totalSessions = gameSessionRepository.count()
            val completedSessions = gameSessionRepository.countByIsCleared(true)
            val activeSessions = gameSessionRepository.countByAttemptsLeftGreaterThanAndIsCleared(0, false)
            
            mapOf(
                "totalSessions" to totalSessions,
                "completedSessions" to completedSessions,
                "activeSessions" to activeSessions,
                "completionRate" to if (totalSessions > 0) (completedSessions.toDouble() / totalSessions * 100) else 0.0
            )
        } catch (e: Exception) {
            println("게임 통계 조회 중 오류: ${e.message}")
            mapOf(
                "error" to "통계 조회 실패: ${e.message}"
            )
        }
    }
    
    /**
     * 세션의 누적 피드백 계산
     * CORRECT > PRESENT > ABSENT 순서로 우선순위 적용
     */
    @Transactional(readOnly = true)
    private fun calculateCumulativeFeedback(sessionId: Long): Map<Char, LetterFeedback> {
        return try {
            val allGuesses = guessRepository.findBySessionIdOrderByGuessedAtAsc(sessionId)
            val cumulativeFeedback = mutableMapOf<Char, LetterFeedback>()
            
            // 모든 추측을 순회하면서 자모별 최고 피드백 계산
            for (guess in allGuesses) {
                for (i in guess.jamos.indices) {
                    if (i < guess.feedback.size) {
                        val jamo = guess.jamos[i]
                        val currentFeedback = guess.feedback[i]
                        
                        // 현재 누적된 피드백과 비교하여 더 높은 우선순위로 업데이트
                        val existingFeedback = cumulativeFeedback[jamo]
                        cumulativeFeedback[jamo] = getBetterFeedback(existingFeedback, currentFeedback)
                    }
                }
            }
            
            cumulativeFeedback
        } catch (e: Exception) {
            println("누적 피드백 계산 중 오류: ${e.message}")
            emptyMap()
        }
    }
    
    /**
     * 두 피드백 중 더 높은 우선순위를 반환
     * 우선순위: CORRECT > PRESENT > ABSENT
     */
    private fun getBetterFeedback(existing: LetterFeedback?, new: LetterFeedback): LetterFeedback {
        if (existing == null) return new
        
        return when {
            existing == LetterFeedback.CORRECT -> LetterFeedback.CORRECT
            new == LetterFeedback.CORRECT -> LetterFeedback.CORRECT
            existing == LetterFeedback.PRESENT -> LetterFeedback.PRESENT
            new == LetterFeedback.PRESENT -> LetterFeedback.PRESENT
            else -> LetterFeedback.ABSENT
        }
    }
}