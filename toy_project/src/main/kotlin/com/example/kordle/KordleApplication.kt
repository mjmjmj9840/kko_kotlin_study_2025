package com.example.kordle

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KordleApplication

fun main(args: Array<String>) {
    runApplication<KordleApplication>(*args)
}