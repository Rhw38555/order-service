package com.example.orderservice.exception

sealed class ErrorException (
    val code: Int,
    override val message: String,
) : RuntimeException(message)

data class RequestBlankException(
    override val message: String = "요청 정보에서 공백 또는 Null은 불가능합니다.",
) : ErrorException(500, message)

data class CustomerNotFoundException(
    override val message: String = "고객 정보를 찾을 수 없습니다.",
) : ErrorException(404, message)

data class ProductNotFoundException(
    override val message: String = "상품 정보를 찾을 수 없습니다.",
) : ErrorException(404, message)

data class OrderNotFoundException(
    override val message: String = "주문 정보를 찾을 수 없습니다.",
) : ErrorException(404, message)

data class ProductQuantityLessZeroException(
    override val message: String = "상품 재고가 없습니다.",
) : ErrorException(404, message)

data class DifferentPaymentAmountException(
    override val message: String = "주문 결제 금액이 다릅니다.",
) : ErrorException(500, message)

data class DifferentOrderAndCustomerException(
    override val message: String = "고객과 주문 고객이 다릅니다.",
) : ErrorException(500, message)
