package com.example.orderservice.dto

import com.example.orderservice.exception.RequestBlankException
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateOrderRequest(
    val customerId: Long,
    val productId: Long,
    val quantity: Int,
    val paymentMethod: String?,
    val paymentAmount: Int?,
) {
    init {
        // 필수요소 공백 금지
        requireNotBlank("고객 ID", customerId)
        requireNotBlank("상품 ID", productId)
        requireNotBlank("상품 수량", quantity)
    }

    private fun requireNotBlank(fieldName: String, value: Any){
        if (value == null) {
            throw RequestBlankException("$fieldName 은(는) 값이 존재해야 합니다.")
        }
    }
}

data class CreateOrderResponse(
    val orderId: Long,
    val status: String,
)

data class CompleteOrderRequest(
    val orderId: Long,
    val paymentMethod: String,
    val paymentAmount: Int,
) {
    init {
        // 필수요소 공백 금지
        requireNotBlank("주문 ID", orderId)
        requireNotBlank("결제 방법", paymentMethod)
        requireNotBlank("결제 금액", paymentAmount)
    }

    private fun requireNotBlank(fieldName: String, value: Any){
        if (value == null) {
            throw RequestBlankException("$fieldName 은(는) 값이 존재해야 합니다.")
        }
    }
}

data class CompleteOrderResponse(
    val orderId: Long,
    val status: String,
)

data class OrderItemResponse(
    val productId: Long,
    val productName: String,
    val price: Int,
    val myQuantity : Int,
)

data class OrderResponse(
    val orderId: Long,
    val customerId: Long,
    val orderItems: List<OrderItemResponse>,
    val status: String,
    val paymentMethod: String,
    val orderDate: String,
)

data class OrderListResponse(
    val orderList: List<OrderResponse>?
)