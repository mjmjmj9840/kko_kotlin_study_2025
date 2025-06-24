package com.example.kordle.repository

import com.example.kordle.entity.Word
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WordRepository : JpaRepository<Word, String> {
    
    // JPQL 쿼리 -  테이블 이름(words)이 아닌 엔티티 이름(Word)를 기준으로 작성
    @Query("SELECT w FROM Word w")
    fun findAllowedWords(): List<Word>
    
    // H2 데이터베이스용 네이티브 쿼리로 랜덤 단어 조회
    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    fun findRandomAllowedWord(): Word?
    
    // 특정 stage의 단어 조회
    @Query("SELECT w FROM Word w WHERE w.stage = :stage")
    fun findByStage(stage: Int): List<Word>
    
    // 특정 stage의 첫 번째 단어 조회 (순서대로)
    @Query("SELECT w FROM Word w WHERE w.stage = :stage ORDER BY w.text")
    fun findFirstByStage(stage: Int): Word?
    
    // 최대 stage 조회
    @Query("SELECT MAX(w.stage) FROM Word w")
    fun findMaxStage(): Int?
}