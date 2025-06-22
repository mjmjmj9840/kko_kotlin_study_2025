package com.example.kordle.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class HangulUtilsTest {

    @Test
    fun `한글 단어를 자모로 분해하는 테스트`() {
        // given
        val word = "코들"
        
        // when
        val result = HangulUtils.decomposeWord(word)
        
        // then
        assertEquals(6, result.size)
        assertEquals(listOf('ㅋ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ', ' '), result)
    }
    
    @Test
    fun `쌍자음이 포함된 단어 분해 테스트`() {
        // given
        val word = "꼬들"
        
        // when
        val result = HangulUtils.decomposeWord(word)
        
        // then
        assertEquals(6, result.size)
        assertEquals(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'), result) // ㄲ -> ㄱ,ㄱ
    }
    
    @Test
    fun `겹받침이 포함된 단어 분해 테스트`() {
        // given
        val word = "닭"
        
        // when
        val result = HangulUtils.decomposeWord(word)
        
        // then
        assertEquals(6, result.size)
        assertEquals(listOf('ㄷ', 'ㅏ', 'ㄹ', 'ㄱ', ' ', ' '), result) // ㄺ -> ㄹ,ㄱ
    }
    
    @Test
    fun `빈 문자열 처리 테스트`() {
        // given
        val word = ""
        
        // when & then
        assertThrows<IllegalArgumentException> {
            HangulUtils.decomposeWord(word)
        }
    }
    
    @Test
    fun `한글이 아닌 문자 포함 시 예외 테스트`() {
        // given
        val word = "코들A"
        
        // when & then
        assertThrows<IllegalArgumentException> {
            HangulUtils.decomposeWord(word)
        }
    }
    
    @Test
    fun `자모 리스트를 6글자로 정규화하는 테스트`() {
        // given
        val shortJamos = listOf('ㄱ', 'ㅏ', 'ㅁ')
        val exactJamos = listOf('ㄱ', 'ㅏ', 'ㅁ', 'ㅅ', 'ㅏ', 'ㅎ')
        val longJamos = listOf('ㄱ', 'ㅏ', 'ㅁ', 'ㅅ', 'ㅏ', 'ㅎ', 'ㅏ', 'ㅁ')
        
        // when
        val shortResult = HangulUtils.normalizeToSixChars(shortJamos)
        val exactResult = HangulUtils.normalizeToSixChars(exactJamos)
        val longResult = HangulUtils.normalizeToSixChars(longJamos)
        
        // then
        assertEquals(6, shortResult.size)
        assertEquals(listOf('ㄱ', 'ㅏ', 'ㅁ', ' ', ' ', ' '), shortResult)
        assertEquals(6, exactResult.size)
        assertEquals(exactJamos, exactResult)
        assertEquals(6, longResult.size)
        assertEquals(listOf('ㄱ', 'ㅏ', 'ㅁ', 'ㅅ', 'ㅏ', 'ㅎ'), longResult)
    }
    
    @Test
    fun `CSV 문자열에서 자모 파싱 테스트`() {
        // given
        val csvString = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]"
        
        // when
        val result = HangulUtils.parseJamosFromString(csvString)
        
        // then
        assertEquals(6, result.size)
        assertEquals(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'), result)
    }
    
    @Test
    fun `잘못된 CSV 문자열 파싱 예외 테스트`() {
        // given
        val invalidCsvString = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ]"  // 5개만 있음
        
        // when & then
        assertThrows<IllegalArgumentException> {
            HangulUtils.parseJamosFromString(invalidCsvString)
        }
    }
    
    @Test
    fun `자모 유효성 검증 테스트`() {
        // given
        val validJamos = listOf('ㄱ', 'ㅏ', 'ㅁ', 'ㅅ', 'ㅏ', 'ㅎ')
        val invalidJamosWrongSize = listOf('ㄱ', 'ㅏ', 'ㅁ', 'ㅅ', 'ㅏ')
        val invalidJamosWithNumbers = listOf('ㄱ', 'ㅏ', 'ㅁ', '1', 'ㅏ', 'ㅎ')
        
        // when & then
        assertTrue(HangulUtils.validateJamos(validJamos))
        assertFalse(HangulUtils.validateJamos(invalidJamosWrongSize))
        assertFalse(HangulUtils.validateJamos(invalidJamosWithNumbers))
    }
    
    @Test
    fun `단어 데이터 검증 테스트`() {
        // given
        val validWord = "코들"
        val validJamosString = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]"
        
        // when
        val result = HangulUtils.validateWordData(validWord, validJamosString)
        
        // then
        assertTrue(result.isValid)
    }
    
    @Test
    fun `빈 단어 데이터 검증 테스트`() {
        // given
        val emptyWord = ""
        val jamosString = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]"
        
        // when
        val result = HangulUtils.validateWordData(emptyWord, jamosString)
        
        // then
        assertFalse(result.isValid)
        assertTrue(result.message.contains("단어가 비어있습니다"))
    }
    
    @Test
    fun `null 자모 문자열 검증 테스트`() {
        // given
        val word = "코들"
        val nullJamosString: String? = null
        
        // when
        val result = HangulUtils.validateWordData(word, nullJamosString)
        
        // then
        assertFalse(result.isValid)
        assertTrue(result.message.contains("자모 문자열이 null이거나 비어있습니다"))
    }
} 