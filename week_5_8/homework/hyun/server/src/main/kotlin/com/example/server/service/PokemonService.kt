package com.example.server.service

import com.example.server.dto.PokemonInfoDto
import com.example.server.repository.PokemonApiRepository
import org.springframework.stereotype.Service

@Service
class PokemonService(
    private val pokemonApiRepository: PokemonApiRepository
) {

    fun getPocketMonsterInfoById(id: String): PokemonInfoDto {
        val pokemonInfo = pokemonApiRepository.getPocketMonsterInfoById(id)

        return PokemonInfoDto.valueOf(pokemonInfo)
    }
}