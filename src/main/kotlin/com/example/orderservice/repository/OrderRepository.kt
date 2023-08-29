package com.example.orderservice.repository

import com.example.orderservice.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByCustomerId(customerId: Long) : List<Order>
}