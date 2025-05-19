package kr.study.elan.kotlin.controller

import kr.study.elan.kotlin.domain.ErrorResponse
import kr.study.elan.kotlin.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUserNotFoundException(e: UserNotFoundException): ErrorResponse {
        return ErrorResponse(e.message ?: e.localizedMessage)
    }

}