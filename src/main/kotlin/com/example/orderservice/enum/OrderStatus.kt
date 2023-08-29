package com.example.orderservice.enum

enum class OrderStatus(
    val status: String
) {
    CREATED("접수"),
    COMPLETED("완료"),
    FAILED("실패"),
    CANCELLED("취소");
}