package com.example.anika.pokemon.application.port.`in`

import com.example.anika.pokemon.domain.Pokemon

interface PokemonUseCase {
    fun getPokemon(id: Int): Pokemon
}
