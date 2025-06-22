package com.example.kordle.repository

import com.example.kordle.entity.Word
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WordRepository : JpaRepository<Word, String> {
    
    // JPQL 쿼리 - Word는 엔티티명, 실제 테이블은 'words'
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
    
    // stage 범위 내의 단어 개수 조회
    @Query("SELECT COUNT(w) FROM Word w WHERE w.stage <= :maxStage")
    fun countByStageUpTo(maxStage: Int): Long
}