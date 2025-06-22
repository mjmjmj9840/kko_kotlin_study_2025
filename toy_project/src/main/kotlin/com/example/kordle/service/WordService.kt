package com.example.kordle.service

import com.example.kordle.entity.Word
import com.example.kordle.repository.WordRepository
import com.example.kordle.util.HangulUtils
import com.example.kordle.util.ValidationResult
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ResourceLoader
import java.io.BufferedReader
import java.io.InputStreamReader

@Service
class WordService(
    private val wordRepository: WordRepository,
    private val resourceLoader: ResourceLoader
) {
    
    @PostConstruct
    fun loadWords() {
        try {
            if (wordRepository.count() == 0L) {
                println("단어 데이터베이스가 비어있습니다. 초기화를 시작합니다.")
                initializeWords()
            } else {
                println("단어 데이터베이스에 ${wordRepository.count()}개의 단어가 있습니다.")
            }
        } catch (e: Exception) {
            println("단어 로딩 중 오류 발생: ${e.message}")
            throw IllegalStateException("단어 서비스 초기화 실패", e)
        }
    }
    
    private fun initializeWords() {
        try {
            val resource = resourceLoader.getResource("file:data/words.csv")
            if (!resource.exists()) {
                println("CSV 파일을 찾을 수 없습니다. 기본 단어를 로딩합니다.")
                loadDefaultWords()
                return
            }
            
            val reader = BufferedReader(InputStreamReader(resource.inputStream, "UTF-8"))
            val words = mutableListOf<Word>()
            val validationErrors = mutableListOf<String>()
            var lineNumber = 1
            
            reader.useLines { lines ->
                lines.forEach { line ->
                        lineNumber++
                        try {
                            val parts = line.split(";", limit = 3)
                            if (parts.size >= 3) {
                                val stage = parts[0].toIntOrNull() ?: 1
                                val word = parts[1].trim()
                                val answerString = parts[2].trim()
                                
                                // 데이터 검증 (null 안전 처리)
                                val safeAnswerString = answerString.takeIf { it.isNotBlank() } 
                                    ?: run {
                                        validationErrors.add("라인 $lineNumber: 자모 데이터가 비어있습니다.")
                                        return@forEach
                                    }
                                
                                val validation = HangulUtils.validateWordData(word, safeAnswerString)
                                if (!validation.isValid) {
                                    validationErrors.add("라인 $lineNumber: ${validation.message}")
                                    return@forEach
                                }
                                
                                // 경고 메시지 출력 (데이터 불일치)
                                if (validation.message.contains("불일치")) {
                                    println("경고 - 라인 $lineNumber: ${validation.message}")
                                }
                                
                                words.add(Word(
                                    text = word, 
                                    stage = stage, 
                                    answer = safeAnswerString
                                ))
                            } else {
                                validationErrors.add("라인 $lineNumber: 잘못된 CSV 형식 - $line")
                            }
                        } catch (e: Exception) {
                            validationErrors.add("라인 $lineNumber: 파싱 오류 - ${e.message}")
                        }
                    }
            }
            
            // 오류 처리
            if (validationErrors.isNotEmpty()) {
                println("CSV 데이터 검증 오류 발견:")
                validationErrors.forEach { println("  - $it") }
                if (words.isEmpty()) {
                    println("유효한 단어가 없습니다. 기본 단어를 로딩합니다.")
                    loadDefaultWords()
                    return
                }
            }
            
            if (words.isNotEmpty()) {
                wordRepository.saveAll(words)
                println("CSV에서 ${words.size}개의 단어를 성공적으로 로딩했습니다.")
                if (validationErrors.isNotEmpty()) {
                    println("${validationErrors.size}개의 오류가 있었지만 유효한 단어들은 로딩되었습니다.")
                }
            } else {
                println("CSV에서 유효한 단어를 찾을 수 없습니다. 기본 단어를 로딩합니다.")
                loadDefaultWords()
            }
        } catch (e: Exception) {
            println("CSV 로딩 중 오류 발생: ${e.message}")
            println("기본 단어를 로딩합니다.")
            loadDefaultWords()
        }
    }
    
    private fun loadDefaultWords() {
        try {
            val words = listOf(
                "꼬들", "나비", "가방", "다람쥐", "라면",
                "마음", "바람", "사랑", "아기", "자동차"
            )
            
            val wordEntities = words.mapNotNull { word ->
                try {
                    val jamos = HangulUtils.wordToSixJamos(word)
                    val answerString = HangulUtils.jamosToCSVString(jamos)
                    
                    // 기본 단어도 검증
                    val validation = HangulUtils.validateWordData(word, answerString)
                    if (!validation.isValid) {
                        println("기본 단어 검증 실패: $word - ${validation.message}")
                        return@mapNotNull null
                    }
                    
                    Word(
                        text = word, 
                        stage = 1, 
                        answer = answerString
                    )
                } catch (e: Exception) {
                    println("기본 단어 처리 실패: $word - ${e.message}")
                    null
                }
            }
            
            if (wordEntities.isEmpty()) {
                throw IllegalStateException("기본 단어 로딩에 실패했습니다.")
            }
            
            wordRepository.saveAll(wordEntities)
            println("${wordEntities.size}개의 기본 단어를 성공적으로 로딩했습니다.")
        } catch (e: Exception) {
            throw IllegalStateException("기본 단어 로딩 실패", e)
        }
    }
    
    fun getRandomWord(): Word {
        return try {
            val word = wordRepository.findRandomAllowedWord()
            if (word == null) {
                throw IllegalStateException("사용 가능한 단어가 없습니다.")
            }
            
            // 반환하는 단어의 유효성 검증
            val validation = HangulUtils.validateWordData(word.text, word.answer)
            if (!validation.isValid) {
                println("경고: 잘못된 단어 데이터 발견 - ${word.text}: ${validation.message}")
                // 다른 단어를 시도하거나 기본 단어 반환
                return getAlternativeWord()
            }
            
            word
        } catch (e: Exception) {
            println("랜덤 단어 조회 중 오류: ${e.message}")
            throw IllegalStateException("단어 조회 실패", e)
        }
    }
    
    private fun getAlternativeWord(): Word {
        val allWords = wordRepository.findAllowedWords()
        if (allWords.isEmpty()) {
            throw IllegalStateException("사용 가능한 단어가 전혀 없습니다.")
        }
        
        // 유효한 단어를 찾아서 반환
        for (word in allWords) {
            val validation = HangulUtils.validateWordData(word.text, word.answer)
            if (validation.isValid) {
                return word
            }
        }
        
        throw IllegalStateException("모든 단어 데이터가 유효하지 않습니다.")
    }
    
    fun isValidWord(word: String): Boolean {
        return try {
            if (word.isBlank()) return false
            wordRepository.existsById(word)
        } catch (e: Exception) {
            println("단어 유효성 검사 중 오류: ${e.message}")
            false
        }
    }
    
    fun getAllowedWords(): List<String> {
        return try {
            wordRepository.findAllowedWords().map { it.text }
        } catch (e: Exception) {
            println("허용 단어 목록 조회 중 오류: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 데이터베이스의 모든 단어 데이터 검증
     * 관리자 도구 또는 테스트용
     */
    fun validateAllWords(): List<ValidationResult> {
        return try {
            val allWords = wordRepository.findAllowedWords()
            allWords.map { word ->
                HangulUtils.validateWordData(word.text, word.answer)
            }
        } catch (e: Exception) {
            println("전체 단어 검증 중 오류: ${e.message}")
            listOf(ValidationResult(false, "전체 검증 실패: ${e.message}"))
        }
    }
    
    /**
     * 단어 개수 조회 (헬스 체크용)
     */
    fun getWordCount(): Long {
        return try {
            wordRepository.count()
        } catch (e: Exception) {
            println("단어 개수 조회 중 오류: ${e.message}")
            0L
        }
    }
    
    /**
     * 특정 stage의 단어 조회
     */
    fun getWordByStage(stage: Int): Word? {
        return try {
            wordRepository.findFirstByStage(stage)
        } catch (e: Exception) {
            println("Stage $stage 단어 조회 중 오류: ${e.message}")
            null
        }
    }
    
    /**
     * 최대 stage 조회
     */
    fun getMaxStage(): Int? {
        return try {
            wordRepository.findMaxStage()
        } catch (e: Exception) {
            println("최대 stage 조회 중 오류: ${e.message}")
            null
        }
    }
    
    /**
     * 특정 stage의 모든 단어 조회
     */
    fun getWordsByStage(stage: Int): List<Word> {
        return try {
            wordRepository.findByStage(stage)
        } catch (e: Exception) {
            println("Stage $stage 단어 목록 조회 중 오류: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * stage별 단어 통계
     */
    fun getStageStatistics(): Map<String, Any> {
        return try {
            val maxStage = getMaxStage() ?: 0
            val totalWords = wordRepository.count()
            val stageDistribution = mutableMapOf<Int, Long>()
            
            for (stage in 1..maxStage) {
                val count = wordRepository.findByStage(stage).size.toLong()
                stageDistribution[stage] = count
            }
            
            mapOf(
                "maxStage" to maxStage,
                "totalWords" to totalWords,
                "stageDistribution" to stageDistribution
            )
        } catch (e: Exception) {
            println("Stage 통계 조회 중 오류: ${e.message}")
            mapOf("error" to "통계 조회 실패: ${e.message}")
        }
    }
}