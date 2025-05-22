package com.example.demo.pokemon

import com.example.demo.pokemon.model.MyPokemon
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pokemon")
class PokemonController(
    val pokeApiService: PokeApiService
) {

    @GetMapping("/{id}")
    fun getMyPokemon(@PathVariable id: Int) : MyPokemon {
        return runBlocking {
            pokeApiService.getMyPokemon(id)
        }
    }

    @GetMapping("/reactive/{id}")
    suspend fun getReactiveMyPokemon(@PathVariable id: Int) : MyPokemon {
        return pokeApiService.getMyPokemon(id)
    }

}
