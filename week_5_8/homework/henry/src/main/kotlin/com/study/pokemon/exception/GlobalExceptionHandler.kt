package com.study.pokemon.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CustomException::class)
    fun handleException(
        ex: CustomException,
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse> =
        ResponseEntity
            .status(ex.status)
            .body(
                ExceptionResponse(
                    status = ex.status.value(),
                    message = ex.message,
                    path = request.requestURI,
                    exception = ex.javaClass.name,
                ),
            )
}
