package kr.study.elan.kstudy.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/pokemon")
class PokemonController(
    private val pokemonWebClient: WebClient
) {
    @GetMapping("/{id}")
    fun delegateApi(@PathVariable id: String): Mono<Any> {
        return pokemonWebClient
            .get()
            .uri { uriBuilder ->
                uriBuilder.path("/pokemon/${id}")
                    .build()
            }
            .retrieve()
            .bodyToMono<Any>()
    }
}