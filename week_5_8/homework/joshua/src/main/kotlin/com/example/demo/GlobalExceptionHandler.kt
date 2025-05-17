package com.example.demo

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ArithmeticException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleArithmeticException(ex: ArithmeticException): ErrorResponse {
        return ErrorResponse (
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "error",
        )
    }

    data class ErrorResponse (
        val status : Int,
        val message: String,
    )
}