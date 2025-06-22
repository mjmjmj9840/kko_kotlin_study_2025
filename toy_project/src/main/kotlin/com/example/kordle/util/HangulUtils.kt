package com.example.kordle.util

object HangulUtils {
    
    // 한글 자음 리스트 (초성, 종성)
    private val CHOSUNG = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )
    
    // 한글 모음 리스트 (중성)
    private val JUNGSUNG = charArrayOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )
    
    // 한글 종성 리스트 (받침)
    private val JONGSUNG = charArrayOf(
        ' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
        'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )
    
    // 복합모음 분해 매핑
    private val COMPLEX_VOWEL_MAP = mapOf(
        'ㅘ' to listOf('ㅗ', 'ㅏ'),
        'ㅙ' to listOf('ㅗ', 'ㅐ'),
        'ㅚ' to listOf('ㅗ', 'ㅣ'),
        'ㅝ' to listOf('ㅜ', 'ㅓ'),
        'ㅞ' to listOf('ㅜ', 'ㅔ'),
        'ㅟ' to listOf('ㅜ', 'ㅣ'),
        'ㅢ' to listOf('ㅡ', 'ㅣ')
    )
    
    // 겹받침 분해 매핑
    private val COMPLEX_JONGSUNG_MAP = mapOf(
        'ㄳ' to listOf('ㄱ', 'ㅅ'),
        'ㄵ' to listOf('ㄴ', 'ㅈ'),
        'ㄶ' to listOf('ㄴ', 'ㅎ'),
        'ㄺ' to listOf('ㄹ', 'ㄱ'),
        'ㄻ' to listOf('ㄹ', 'ㅁ'),
        'ㄼ' to listOf('ㄹ', 'ㅂ'),
        'ㄽ' to listOf('ㄹ', 'ㅅ'),
        'ㄾ' to listOf('ㄹ', 'ㅌ'),
        'ㄿ' to listOf('ㄹ', 'ㅍ'),
        'ㅀ' to listOf('ㄹ', 'ㅎ'),
        'ㅄ' to listOf('ㅂ', 'ㅅ')
    )
    
    /**
     * 한글 단어를 자모 단위로 분해 (게임 규칙에 맞게 정확히 6글자로)
     * CSV 데이터와 일관성을 유지하기 위해 개선된 로직
     */
    fun decomposeWord(word: String): List<Char> {
        if (word.isBlank()) {
            throw IllegalArgumentException("단어가 비어있습니다.")
        }
        
        val result = mutableListOf<Char>()
        
        for (char in word) {
            if (isHangulSyllable(char)) {
                val decomposed = decomposeSyllableToGameFormat(char)
                result.addAll(decomposed)
            } else if (isHangulJamo(char)) {
                // 이미 자모인 경우
                result.add(char)
            } else {
                throw IllegalArgumentException("한글이 아닌 문자가 포함되어 있습니다: $char")
            }
        }
        
        return normalizeToSixChars(result)
    }
    
    /**
     * 한글 음절을 게임 형식에 맞게 자모로 분해
     * CSV 데이터 형식과 일치하도록 수정
     */
    private fun decomposeSyllableToGameFormat(syllable: Char): List<Char> {
        if (!isHangulSyllable(syllable)) {
            return listOf(syllable)
        }
        
        val code = syllable.code - 0xAC00
        val chosungIndex = code / (21 * 28)
        val jungsungIndex = (code % (21 * 28)) / 28
        val jongsungIndex = code % 28
        
        val result = mutableListOf<Char>()
        
        // 초성 추가 (쌍자음을 분리하여 추가 - CSV 형식에 맞춤)
        val chosung = CHOSUNG[chosungIndex]
        when (chosung) {
            'ㄲ' -> result.addAll(listOf('ㄱ', 'ㄱ'))  // 꼬들의 경우 ㄲ -> ㄱ,ㄱ
            'ㄸ' -> result.addAll(listOf('ㄷ', 'ㄷ'))
            'ㅃ' -> result.addAll(listOf('ㅂ', 'ㅂ'))
            'ㅆ' -> result.addAll(listOf('ㅅ', 'ㅅ'))
            'ㅉ' -> result.addAll(listOf('ㅈ', 'ㅈ'))
            else -> result.add(chosung)
        }
        
        // 중성 추가 (복합모음은 분리하지 않음 - 게임 형식에 맞춤)
        val jungsung = JUNGSUNG[jungsungIndex]
        result.add(jungsung)
        
        // 종성 추가 (겹받침 처리)
        if (jongsungIndex > 0) {
            val jongsung = JONGSUNG[jongsungIndex]
            if (COMPLEX_JONGSUNG_MAP.containsKey(jongsung)) {
                result.addAll(COMPLEX_JONGSUNG_MAP[jongsung]!!)
            } else {
                result.add(jongsung)
            }
        }
        
        return result
    }
    
    /**
     * 한글 음절인지 확인
     */
    private fun isHangulSyllable(char: Char): Boolean {
        return char.code in 0xAC00..0xD7A3
    }
    
    /**
     * 한글 자모인지 확인
     */
    private fun isHangulJamo(char: Char): Boolean {
        return char.code in 0x1100..0x11FF || char.code in 0x3130..0x318F
    }
    
    /**
     * 자모 리스트를 6글자로 맞춤 (패딩 또는 트림)
     */
    fun normalizeToSixChars(jamos: List<Char>): List<Char> {
        return when {
            jamos.size == 6 -> jamos
            jamos.size < 6 -> jamos + List(6 - jamos.size) { ' ' }
            else -> jamos.take(6)
        }
    }
    
    /**
     * 6글자 자모 리스트를 문자열로 변환 (디스플레이용)
     */
    fun jamosToDisplayString(jamos: List<Char>): String {
        return jamos.joinToString("")
    }
    
    /**
     * 단어를 정확히 6글자 자모로 분해
     */
    fun wordToSixJamos(word: String): List<Char> {
        val decomposed = decomposeWord(word)
        return normalizeToSixChars(decomposed)
    }
    
    /**
     * CSV에서 읽은 자모 문자열을 파싱 (예: "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]")
     * 예외 처리 강화
     */
    fun parseJamosFromString(jamoString: String): List<Char> {
        return try {
            val cleaned = jamoString.trim()
            if (cleaned.isEmpty()) {
                throw IllegalArgumentException("자모 문자열이 비어있습니다.")
            }
            
            val result = cleaned
                .removeSurrounding("[", "]")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { 
                    if (it.length != 1) {
                        throw IllegalArgumentException("잘못된 자모 형식: $it")
                    }
                    it[0] 
                }
            
            if (result.size != 6) {
                throw IllegalArgumentException("자모는 정확히 6개여야 합니다. 현재: ${result.size}개")
            }
            
            result
        } catch (e: Exception) {
            throw IllegalArgumentException("자모 문자열 파싱 실패: $jamoString", e)
        }
    }
    
    /**
     * 자모 리스트가 유효한지 검증
     */
    fun validateJamos(jamos: List<Char>): Boolean {
        if (jamos.size != 6) return false
        
        return jamos.all { jamo ->
            jamo == ' ' || isHangulJamo(jamo) || 
            CHOSUNG.contains(jamo) || 
            JUNGSUNG.contains(jamo) || 
            JONGSUNG.contains(jamo)
        }
    }
    
    /**
     * 단어 데이터 검증 (CSV 로딩 시 사용)
     */
    fun validateWordData(word: String, jamosString: String?): ValidationResult {
        return try {
            if (word.isBlank()) {
                return ValidationResult(false, "단어가 비어있습니다.")
            }
            
            if (jamosString.isNullOrBlank()) {
                return ValidationResult(false, "자모 문자열이 null이거나 비어있습니다.")
            }
            
            val parsedJamos = parseJamosFromString(jamosString)
            val decomposedJamos = decomposeWord(word)
            val normalizedJamos = normalizeToSixChars(decomposedJamos)
            
            if (!validateJamos(parsedJamos)) {
                return ValidationResult(false, "잘못된 자모 데이터: $jamosString")
            }
            
            // 자모 일치 여부 검사 (경고 수준)
            if (parsedJamos != normalizedJamos) {
                return ValidationResult(
                    true, 
                    "자모 불일치 경고 - 단어: $word, CSV: $parsedJamos, 분해결과: $normalizedJamos"
                )
            }
            
            ValidationResult(true, "유효한 데이터")
        } catch (e: Exception) {
            ValidationResult(false, "검증 실패: ${e.message}")
        }
    }
    
    /**
     * 자모를 CSV 형식 문자열로 변환
     */
    fun jamosToCSVString(jamos: List<Char>): String {
        return "[${jamos.joinToString(",")}]"
    }
}

/**
 * 단어 데이터 검증 결과
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String
)