package com.example.orderservice.controller

import com.example.orderservice.exception.ErrorException
import com.example.orderservice.exception.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ExceptionHandlerAdvice {

    @ExceptionHandler(ErrorException::class)
    @ResponseStatus(HttpStatus.OK)
    fun handleLectureException(ex: ErrorException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(ex.code, ex.message)
        return ResponseEntity.status(HttpStatus.OK).body(errorResponse)
    }
}

