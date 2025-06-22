package com.example.kordle.controller

import com.example.kordle.dto.GuessRequest
import com.example.kordle.dto.StartGameRequest
import com.example.kordle.repository.WordRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(value = ["/schema.sql", "/test-data.sql"])
class GameControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var wordRepository: WordRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `게임 시작 API 테스트`() {
        // given
        val request = StartGameRequest("테스트유저")
        val requestJson = objectMapper.writeValueAsString(request)

        // when & then
        mockMvc.perform(
            post("/api/game/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.sessionId").exists())
            .andExpect(jsonPath("$.maxAttempts").value(6))
            .andExpect(jsonPath("$.currentStage").value(1))
            .andExpect(jsonPath("$.wordHint").exists())
    }

    @Test
    fun `게임 전체 플로우 테스트`() {
        // given - 게임 시작
        val startRequest = StartGameRequest("테스트유저")
        val startRequestJson = objectMapper.writeValueAsString(startRequest)

        val startResult = mockMvc.perform(
            post("/api/game/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(startRequestJson)
        )
            .andExpect(status().isOk)
            .andReturn()

        val startResponse = objectMapper.readTree(startResult.response.contentAsString)
        val sessionId = startResponse.get("sessionId").asLong()

        // when & then - 잘못된 추측
        val wrongGuessRequest = GuessRequest(listOf('ㅁ', 'ㅏ', 'ㅁ', 'ㅏ', 'ㅁ', 'ㅏ'))
        val wrongGuessJson = objectMapper.writeValueAsString(wrongGuessRequest)

        mockMvc.perform(
            post("/api/game/{sessionId}/guess", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongGuessJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.attemptsLeft").value(5))
            .andExpect(jsonPath("$.isCleared").value(false))
            .andExpect(jsonPath("$.isGameOver").value(false))

        // when & then - 정답 추측
        val correctGuessRequest = GuessRequest(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'))
        val correctGuessJson = objectMapper.writeValueAsString(correctGuessRequest)

        mockMvc.perform(
            post("/api/game/{sessionId}/guess", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(correctGuessJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.attemptsLeft").value(4))
            .andExpect(jsonPath("$.isCleared").value(true))
            .andExpect(jsonPath("$.isGameOver").value(true))
            .andExpect(jsonPath("$.feedback[0]").value("CORRECT"))
            .andExpect(jsonPath("$.feedback[1]").value("CORRECT"))
            .andExpect(jsonPath("$.feedback[2]").value("CORRECT"))
            .andExpect(jsonPath("$.feedback[3]").value("CORRECT"))
            .andExpect(jsonPath("$.feedback[4]").value("CORRECT"))
            .andExpect(jsonPath("$.feedback[5]").value("CORRECT"))
    }

    @Test
    fun `게임 기록 조회 API 테스트`() {
        // given - 게임 시작
        val startRequest = StartGameRequest("테스트유저")
        val startRequestJson = objectMapper.writeValueAsString(startRequest)

        val startResult = mockMvc.perform(
            post("/api/game/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(startRequestJson)
        )
            .andExpect(status().isOk)
            .andReturn()

        val startResponse = objectMapper.readTree(startResult.response.contentAsString)
        val sessionId = startResponse.get("sessionId").asLong()

        // given - 추측 수행
        val guessRequest = GuessRequest(listOf('ㅁ', 'ㅏ', 'ㅁ', 'ㅏ', 'ㅁ', 'ㅏ'))
        val guessJson = objectMapper.writeValueAsString(guessRequest)

        mockMvc.perform(
            post("/api/game/{sessionId}/guess", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(guessJson)
        )
            .andExpect(status().isOk)

        // when & then - 기록 조회
        mockMvc.perform(
            get("/api/game/{sessionId}/history", sessionId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].jamos").isArray)
            .andExpect(jsonPath("$[0].feedback").isArray)
    }

    @Test
    fun `잘못된 자모 개수로 추측 실패 테스트`() {
        // given - 게임 시작
        val startRequest = StartGameRequest("테스트유저")
        val startRequestJson = objectMapper.writeValueAsString(startRequest)

        val startResult = mockMvc.perform(
            post("/api/game/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(startRequestJson)
        )
            .andExpect(status().isOk)
            .andReturn()

        val startResponse = objectMapper.readTree(startResult.response.contentAsString)
        val sessionId = startResponse.get("sessionId").asLong()

        // when & then - 잘못된 자모 개수로 추측
        val invalidGuessRequest = GuessRequest(listOf('ㅁ', 'ㅏ', 'ㅁ'))  // 3개만
        val invalidGuessJson = objectMapper.writeValueAsString(invalidGuessRequest)

        mockMvc.perform(
            post("/api/game/{sessionId}/guess", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidGuessJson)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `존재하지 않는 세션으로 추측 실패 테스트`() {
        // given
        val guessRequest = GuessRequest(listOf('ㅁ', 'ㅏ', 'ㅁ', 'ㅏ', 'ㅁ', 'ㅏ'))
        val guessJson = objectMapper.writeValueAsString(guessRequest)

        // when & then
        mockMvc.perform(
            post("/api/game/{sessionId}/guess", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(guessJson)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `사용자 통계 조회 API 테스트`() {
        // given - 게임 시작 및 완료
        val startRequest = StartGameRequest("통계테스트유저")
        val startRequestJson = objectMapper.writeValueAsString(startRequest)

        val startResult = mockMvc.perform(
            post("/api/game/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(startRequestJson)
        )
            .andExpect(status().isOk)
            .andReturn()

        val startResponse = objectMapper.readTree(startResult.response.contentAsString)
        val sessionId = startResponse.get("sessionId").asLong()

        // 게임 완료
        val correctGuessRequest = GuessRequest(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'))
        val correctGuessJson = objectMapper.writeValueAsString(correctGuessRequest)

        mockMvc.perform(
            post("/api/game/{sessionId}/guess", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(correctGuessJson)
        )
            .andExpect(status().isOk)

        // when & then - 통계 조회
        mockMvc.perform(
            get("/api/game/stats/{nickname}", "통계테스트유저")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nickname").value("통계테스트유저"))
            .andExpect(jsonPath("$.totalGames").value(1))
            .andExpect(jsonPath("$.wins").value(1))
            .andExpect(jsonPath("$.winRate").value(100.0))
    }

    @Test
    fun `대시보드 조회 API 테스트`() {
        // when & then
        mockMvc.perform(
            get("/api/game/dashboard")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }
} 