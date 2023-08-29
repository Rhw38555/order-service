package com.example.orderservice.exception

data class ErrorResponse (
    val code:Int,
    val message: String,
)