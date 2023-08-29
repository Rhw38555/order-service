package com.example.orderservice.enum

enum class PaymentType(
    val type: String
) {
    CREDITCARD("신용카드"),
    BANKBOOK("통장거래");

    // 타입 찾기
    companion object {
        fun findByType(type: String): PaymentType? {
            return enumValues<PaymentType>().find { it.type == type }
        }
    }
}
