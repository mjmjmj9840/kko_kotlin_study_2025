package com.example.kordle.controller

import com.example.kordle.dto.*
import com.example.kordle.service.GameService
import com.example.kordle.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = ["*"])
class GameController(
    private val gameService: GameService,
    private val statsService: StatsService
) {
    
    @PostMapping("/start")
    fun startGame(@Valid @RequestBody request: StartGameRequest): ResponseEntity<StartGameResponse> {
        val response = gameService.createSession(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/{sessionId}/guess")
    fun makeGuess(
        @PathVariable sessionId: Long,
        @Valid @RequestBody request: GuessRequest
    ): ResponseEntity<GuessResponse> {
        return try {
            val response = gameService.processGuess(sessionId, request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.status(409).build()
        }
    }
    
    @GetMapping("/{sessionId}/history")
    fun getGameHistory(@PathVariable sessionId: Long): ResponseEntity<List<GuessHistoryResponse>> {
        val history = gameService.getHistory(sessionId)
        return ResponseEntity.ok(history)
    }
    
    @GetMapping("/stats/{nickname}")
    fun getUserStats(@PathVariable nickname: String): ResponseEntity<UserStatsResponse> {
        val stats = statsService.getStats(nickname)
        return ResponseEntity.ok(stats)
    }
    
    @GetMapping("/dashboard")
    fun getDashboard(): ResponseEntity<List<DashboardResponse>> {
        val dashboard = statsService.getDashboard()
        return ResponseEntity.ok(dashboard)
    }
}