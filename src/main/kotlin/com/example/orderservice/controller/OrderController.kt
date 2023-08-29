package com.example.orderservice.controller

import com.example.orderservice.dto.*
import com.example.orderservice.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping
    fun createOrder(@RequestBody createOrderReqeust: CreateOrderRequest) : ResponseEntity<CreateOrderResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.createOrder(createOrderReqeust))
    }

    @PatchMapping
    fun completeOrder(@RequestBody completeOrderRequest: CompleteOrderRequest) : ResponseEntity<CompleteOrderResponse> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(orderService.completeOrder(completeOrderRequest))
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long) : ResponseEntity<OrderResponse> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(orderService.getOrder(orderId))
    }

    @GetMapping("/customers/{customerId}")
    fun getOrderList(@PathVariable customerId: Long) : ResponseEntity<OrderListResponse> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(orderService.getOrderList(customerId))
    }

}