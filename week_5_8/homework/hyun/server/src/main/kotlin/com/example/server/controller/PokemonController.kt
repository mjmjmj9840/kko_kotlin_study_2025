package com.example.server.controller

import com.example.server.dto.PokemonInfoDto
import com.example.server.service.PokemonService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PokemonController (
    private val pokemonService: PokemonService
) {

    @GetMapping("/pokemon/info")
    fun getPocketMonster(@RequestParam("id") id: String?): PokemonInfoDto {
        if (id == null) {
            throw NullPointerException()
        }

        return pokemonService.getPocketMonsterInfoById(id)
    }
}