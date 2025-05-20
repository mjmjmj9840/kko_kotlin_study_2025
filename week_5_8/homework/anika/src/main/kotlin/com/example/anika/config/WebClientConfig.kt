package com.example.anika.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(PokeApiProperties::class)
class WebClientConfig {
    @Bean
    fun webClient(pokeApiProperties: PokeApiProperties): WebClient {
        return WebClient
            .builder()
            .baseUrl(pokeApiProperties.baseUrl)
            .build()
    }
}
