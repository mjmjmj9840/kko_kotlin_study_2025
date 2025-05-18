package com.example.demo.pokemon.model

data class MyPokemon (
    val id: Int,
    val name: String,
    val height: Int,
    val sprites: PokemonSprite,
    val stats : List<MyPokemonStat>
) {
    companion object {
        fun fromPokemon(pokemon: Pokemon) = MyPokemon(
            id = pokemon.id,
            name = pokemon.name,
            height = pokemon.height,
            sprites = pokemon.sprites,
            stats = pokemon.stats.map(MyPokemonStat::fromPokemonStat)
        )
    }
}