package com.example.orderservice.controller

import com.example.orderservice.dto.*
import com.example.orderservice.entity.Customer
import com.example.orderservice.entity.Order
import com.example.orderservice.entity.OrderItem
import com.example.orderservice.entity.Product
import com.example.orderservice.enum.OrderStatus
import com.example.orderservice.enum.PaymentType
import com.example.orderservice.service.OrderService
import com.example.setting.GlobalSettings
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDateTime

@WebMvcTest(OrderController::class)
@ActiveProfiles("test")
class OrderControllerTest(
    @Autowired
    private val mockMvc: MockMvc,

    @MockBean
    private val orderService: OrderService,
) : DescribeSpec({

    val objectMapper = ObjectMapper().registerKotlinModule()

    describe("POST /orders"){
        val createOrderRequest = CreateOrderRequest(1, 1, 1, null, null)
        val createOrderResponse = CreateOrderResponse(1, OrderStatus.CREATED.status)

        context("유효한 주문이 호출되면"){

            // mock
            BDDMockito.given(orderService.createOrder(createOrderRequest))
                .willReturn(createOrderResponse)

            val result = mockMvc.post("/orders") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(createOrderRequest)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

            it("CreateOrderResponse 응답값을 전달한다"){
                val response = objectMapper.readValue(result.response.contentAsString, CreateOrderResponse::class.java)
                response.orderId shouldBe 1L
                response.status shouldBe OrderStatus.CREATED.status
            }
        }
    }

    describe("PATCH /orders"){
        val createOrderRequest = CompleteOrderRequest(1, "신용카드", 10000)
        val createOrderResponse = CompleteOrderResponse(1, OrderStatus.COMPLETED.status)

        context("유효한 주문 완료 요청이 호출되면"){

            // mock
            BDDMockito.given(orderService.completeOrder(createOrderRequest))
                .willReturn(createOrderResponse)

            val result = mockMvc.patch("/orders") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(createOrderRequest)
            }.andExpect {
                status { isOk() }
            }.andReturn()

            it("CompleteOrderResponse 응답값을 전달한다"){
                val response = objectMapper.readValue(result.response.contentAsString, CompleteOrderResponse::class.java)
                response.orderId shouldBe 1L
                response.status shouldBe OrderStatus.COMPLETED.status
            }
        }
    }

    describe("GET /{orderId}"){


        val customer = Customer(1, "test1", "1234")
        val product = Product(1, "상품1", BigDecimal(10000), 100)
        val order = Order(id=1L, customer=customer, status = OrderStatus.COMPLETED,
            orderDate = LocalDateTime.now(), paymentMethod = PaymentType.CREDITCARD)
        val orderItem = OrderItem(1, order, product, 1)
        val orderItems = mutableListOf(orderItem)
        order.orderItems = orderItems

        val orderItemResponse = OrderItemResponse(product.id!!, product.name,
            product.price.intValueExact(), orderItem.quantity)

        val orderResponse = OrderResponse(
            order.id!!, customer.id!!, mutableListOf(orderItemResponse),
            order.status.status, order.paymentMethod!!.type, order.orderDate.format(GlobalSettings.dateFormatShort)
        )

        context("유효한 주문 정보 조회가 호출되면"){

            // mock
            BDDMockito.given(orderService.getOrder(1L))
                .willReturn(orderResponse)

            val result = mockMvc.get("/orders/1") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
            }.andReturn()

            it("OrderResponse 응답값을 전달한다"){
                val response = objectMapper.readValue(result.response.contentAsString, OrderResponse::class.java)
                response.orderId shouldBe 1L
                response.status shouldBe OrderStatus.COMPLETED.status
            }
        }
    }

    describe("GET /customers/{customerId}"){


        val customer = Customer(1, "test1", "1234")
        val product = Product(1, "상품1", BigDecimal(10000), 100)
        val order = Order(id=1L, customer=customer, status = OrderStatus.COMPLETED,
            orderDate = LocalDateTime.now(), paymentMethod = PaymentType.CREDITCARD)
        val orderItem = OrderItem(1, order, product, 1)
        val orderItems = mutableListOf(orderItem)
        order.orderItems = orderItems

        val orderItemResponse = OrderItemResponse(product.id!!, product.name,
            product.price.intValueExact(), orderItem.quantity)

        val orderResponse = OrderResponse(
            order.id!!, customer.id!!, mutableListOf(orderItemResponse),
            order.status.status, order.paymentMethod!!.type, order.orderDate.format(GlobalSettings.dateFormatShort)
        )

        val orderListResponse = OrderListResponse(listOf(orderResponse))

        context("유효한 주문 정보 조회가 호출되면"){

            // mock
            BDDMockito.given(orderService.getOrderList(1L))
                .willReturn(orderListResponse)

            val result = mockMvc.get("/orders/customers/1") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
            }.andReturn()

            it("OrderResponse 응답값을 전달한다"){
                val response = objectMapper.readValue(result.response.contentAsString, OrderListResponse::class.java)
                response.orderList!![0].orderId shouldBe 1L
                response.orderList!![0].status shouldBe OrderStatus.COMPLETED.status
            }
        }
    }
})