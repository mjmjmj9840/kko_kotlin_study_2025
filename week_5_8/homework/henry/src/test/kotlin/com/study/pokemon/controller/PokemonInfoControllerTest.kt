package com.study.pokemon.controller

import com.ninjasquad.springmockk.MockkBean
import com.study.pokemon.dto.request.HttpPokemonData
import com.study.pokemon.exception.CustomException
import com.study.pokemon.service.PokemonService
import io.mockk.coEvery
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@WebMvcTest(PokemonInfoController::class)
class PokemonInfoControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var pokemonService: PokemonService

    @Test
    fun `포켓몬 정보가 정상적으로 응답하는지`() {
        // Given
        val pokemonId = 1
        val pokemon =
            HttpPokemonData(
                id = pokemonId,
                name = "test",
                height = 1,
                sprites =
                    HttpPokemonData.SpritesData(
                        frontDefault = "front_default",
                        backDefault = "back_default",
                    ),
                stats =
                    listOf(
                        HttpPokemonData.StatData(
                            baseStat = 45,
                            stat =
                                HttpPokemonData.StatData.StatInfoData(
                                    name = "hp",
                                    url = "https://test.co/1",
                                ),
                        ),
                        HttpPokemonData.StatData(
                            baseStat = 49,
                            stat =
                                HttpPokemonData.StatData.StatInfoData(
                                    name = "attack",
                                    url = "https://test.co/2",
                                ),
                        ),
                    ),
            )

        coEvery { pokemonService.getPokemonInfo(pokemonId) } returns pokemon.toDomain()

        // When
        val result =
            mockMvc
                .perform(get("/pokemon/$pokemonId"))
                .andExpect {
                    status().isOk
                    jsonPath("$.id").value(pokemonId)
                    jsonPath("$.name").value(pokemon.name)
                    jsonPath("$.height").value(pokemon.height)
                    jsonPath("$.sprites").isMap
                    jsonPath("$.sprites.front_default").value(pokemon.sprites.frontDefault)
                    jsonPath("$.sprites.back_default").value(pokemon.sprites.backDefault)
                    jsonPath("$.stats").isArray
                }

        // Then
        assertThat(result).isNotNull
    }

    @Test
    fun `포켓몬 정보가 없을 때 에러가 발생하는지`() {
        // Given
        val pokemonId = -1

        coEvery { pokemonService.getPokemonInfo(pokemonId) } throws
            CustomException(
                message = "포켓몬 못 찾음",
                status = HttpStatus.NOT_FOUND,
            )

        // When
        val result =
            mockMvc
                .perform(get("/pokemon/$pokemonId"))
                .andExpect {
                    status().isNotFound
                }

        // Then
        assertThat(result).isNotNull
    }
}
