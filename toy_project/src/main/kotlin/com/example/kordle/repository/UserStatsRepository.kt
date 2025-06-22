package com.example.kordle.repository

import com.example.kordle.entity.UserStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserStatsRepository : JpaRepository<UserStats, String>