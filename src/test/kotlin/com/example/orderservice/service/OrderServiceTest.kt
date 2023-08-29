package com.example.orderservice.service

import com.example.orderservice.dto.CompleteOrderRequest
import com.example.orderservice.dto.CreateOrderRequest
import com.example.orderservice.dto.OrderItemResponse
import com.example.orderservice.entity.Order
import com.example.orderservice.entity.Customer
import com.example.orderservice.entity.OrderItem
import com.example.orderservice.enum.OrderStatus
import com.example.orderservice.entity.Product
import com.example.orderservice.enum.PaymentType
import com.example.orderservice.exception.*
import com.example.orderservice.repository.CustomerRepository
import com.example.orderservice.repository.OrderRepository
import com.example.orderservice.repository.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class OrderServiceTest(

): BehaviorSpec({
    val customerRepository = mockk<CustomerRepository>()
    val productRepository = mockk<ProductRepository>()
    val orderRepository = mockk<OrderRepository>()
    val orderService = OrderService(customerRepository, productRepository, orderRepository)


//    beforeTest {
//        clearAllMocks()
//    }

    given("주문 정보가 주어졌을 때") {

        val customer = Customer(1, "test1", "1234")
        val product = Product(1, "상품1", BigDecimal(10000), 100)
        val order = Order(id=1L, customer=customer,
            status = OrderStatus.CREATED, orderDate = LocalDateTime.now())
        val orderItem = OrderItem(1, order, product, 1)
        val orderItems = mutableListOf(orderItem)
        order.orderItems = orderItems

        and("고객 정보가 존재하지 않으면"){
            val createOrderRequest = CreateOrderRequest(10000, 10000, 1, null, null)
            every { customerRepository.findByIdOrNull(createOrderRequest.customerId) } returns null

            then("고객 정보를 찾을수 없어 CustomerNotFoundException 에러가 발생한다."){
                shouldThrow<CustomerNotFoundException> {
                    orderService.createOrder(createOrderRequest)
                }
            }
        }

        and("상품 정보가 존재하지 않으면"){
            val createOrderRequest = CreateOrderRequest(1, 10000, 1, null, null)
            every { customerRepository.findByIdOrNull(createOrderRequest.customerId) } returns customer
            every { productRepository.findByIdWithLock(createOrderRequest.productId) } returns null

            then("상품 정보를 찾을수 없어 ProductNotFoundException 에러가 발생한다."){
                shouldThrow<ProductNotFoundException> {
                    orderService.createOrder(createOrderRequest)
                }
            }
        }

        val createOrderRequest = CreateOrderRequest(1, 1, 1, null, null)

        every { customerRepository.findByIdOrNull(createOrderRequest.customerId) } returns customer
        every { productRepository.findByIdWithLock(createOrderRequest.productId) } returns product
        every { orderRepository.save(any()) } returns order

        // 재고 감소
        every { productRepository.save(any()) } returns product

        `when`("주문 정보를 사용해 주문을 생성(접수)한다.") {
            val result = orderService.createOrder(createOrderRequest)
            then("주문 정보가 정상 생성(접수)된다.") {
                result.status shouldBe OrderStatus.CREATED.status
                product.quantity shouldBe 99
            }
        }
    }

    given("100명의 사용자가 하나의 상품을 동시에 주문했을 때") {
        val product = Product(1, "상품1", BigDecimal(10000), 100)

        `when`("100명의 사용자가 주문 정보를 사용해 주문을 생성(접수)한다.") {

            // 동시성 테스트를 위한 100명 요청 생성
            val threadCount = 101
            val executorService = Executors.newFixedThreadPool(16)
            val latch = CountDownLatch(threadCount)
            for (i in 1 .. threadCount) {
                executorService.submit {
                    try{
                        val customer = Customer(i.toLong(), "test1", "1234")
                        val order = Order(id=i.toLong(), customer=customer,
                            status = OrderStatus.CREATED, orderDate = LocalDateTime.now())
                        val orderItem = OrderItem(i.toLong(), order, product, 1)
                        val orderItems = mutableListOf(orderItem)
                        order.orderItems = orderItems
                        val createOrderRequest = CreateOrderRequest(1, 1, 1, null, null)
                        every { customerRepository.findByIdOrNull(createOrderRequest.customerId) } returns customer
                        every { productRepository.findByIdWithLock(createOrderRequest.productId) } returns product
                        every { orderRepository.save(any()) } returns order

                        // 재고 감소
                        every { productRepository.save(any()) } returns product
                        orderService.createOrder(createOrderRequest)
                    }finally{
                        latch.countDown()
                    }
                }
            }
            // Thread 모두 종료될때까지 대기
            latch.await()

            then("100개의 주문 정보가 정상 생성(접수)되고 상품의 재고는 0개가 된다.") {
                product.quantity shouldBe 0
            }
        }
    }


    given("주문 완료 정보가 주어졌을 때") {

        val customer = Customer(1, "test1", "1234")
        val product = Product(1, "상품1", BigDecimal(10000), 100)
        val order = Order(id=1L, customer=customer,
            status = OrderStatus.CREATED, orderDate = LocalDateTime.now())
        val orderItem = OrderItem(1, order, product, 1)
        val orderItems = mutableListOf(orderItem)
        order.orderItems = orderItems


        and("주문 정보가 존재하지 않으면"){
            val completeOrderRequest = CompleteOrderRequest(1,"신용카드", 10000)
            every { orderRepository.findByIdOrNull(completeOrderRequest.orderId) } returns null
            then("주문 정보를 찾을수 없어 OrderNotFoundException 에러가 발생한다."){
                shouldThrow<OrderNotFoundException> {
                    orderService.completeOrder(completeOrderRequest)
                }
            }
        }

        and("주문 결제 금액이 다르면"){
            val completeOrderRequest = CompleteOrderRequest(1,"신용카드", 15000)
            every { orderRepository.findByIdOrNull(completeOrderRequest.orderId) } returns order
            then("결제 금액이 달라 DifferentPaymentAmountException 에러가 발생한다."){
                shouldThrow<DifferentPaymentAmountException> {
                    orderService.completeOrder(completeOrderRequest)
                }
            }
        }

        val completeOrderRequest = CompleteOrderRequest(1, "신용카드", 10000)

        order.paymentMethod = PaymentType.findByType(completeOrderRequest.paymentMethod)
        order.status = OrderStatus.COMPLETED
        every { orderRepository.findByIdOrNull(completeOrderRequest.orderId) } returns order
        every { orderRepository.save(any()) } returns order

        `when`("주문 완료 정보를 사용해 주문을 완료한다.") {
            val result = orderService.completeOrder(completeOrderRequest)
            then("주문 완료로 변경된다.") {
                result.status shouldBe OrderStatus.COMPLETED.status
            }
        }
    }


    given("주문 ID가 주어졌을 때") {
        val orderId = 1L
        val customer = Customer(1, "test1", "1234")
        val product = Product(1, "상품1", BigDecimal(10000), 100)
        val order = Order(id=1L, customer=customer, status = OrderStatus.COMPLETED,
            orderDate = LocalDateTime.now(), paymentMethod = PaymentType.CREDITCARD)
        val orderItem = OrderItem(1, order, product, 1)
        val orderItems = mutableListOf(orderItem)
        order.orderItems = orderItems

        val orderItemResponse = OrderItemResponse(product.id!!, product.name,
            product.price.intValueExact(), orderItem.quantity)

        and("주문 정보가 존재하지 않으면"){
            val customerId = 1L
            val orderId = 2L
            every { orderRepository.findByIdOrNull(orderId) } returns null
            then("고객 정보를 찾을수 없어 OrderNotFoundException 에러가 발생한다."){
                shouldThrow<OrderNotFoundException> {
                    orderService.getOrder(orderId)
                }
            }
        }

        `when`("주문 ID로 주문 정보를 조회한다.") {
            every { orderRepository.findByIdOrNull(orderId) } returns order
            val result = orderService.getOrder(orderId)
            then("주문 정보를 조회한다.") {
                result.status shouldBe OrderStatus.COMPLETED.status
                result.paymentMethod shouldBe PaymentType.CREDITCARD.type
                result.orderItems shouldBe mutableListOf(orderItemResponse)
            }
        }
    }

    given("고객 ID가 주어졌을 때") {
        val customerId = 1L
        val customer = Customer(1, "test1", "1234")
        val product = Product(1, "상품1", BigDecimal(10000), 100)
        val order = Order(id=1L, customer=customer, status = OrderStatus.COMPLETED,
            orderDate = LocalDateTime.now(), paymentMethod = PaymentType.CREDITCARD)
        val orderItem = OrderItem(1, order, product, 1)
        val orderItems = mutableListOf(orderItem)
        order.orderItems = orderItems

        val orderItemResponse = OrderItemResponse(product.id!!, product.name,
            product.price.intValueExact(), orderItem.quantity)

        and("주문 정보가 존재하지 않으면"){
            val customerId = 1L
            val orderId = 2L
            every { orderRepository.findByIdOrNull(orderId) } returns null
            then("고객 정보를 찾을수 없어 OrderNotFoundException 에러가 발생한다."){
                shouldThrow<OrderNotFoundException> {
                    orderService.getOrder(orderId)
                }
            }
        }

        `when`("주문 ID로 주문 정보를 조회한다.") {
            every { orderRepository.findByCustomerId(customerId) } returns listOf(order)
            val result = orderService.getOrderList(customerId)
            then("주문 리스트를 조회한다.") {
                result.orderList!![0].status shouldBe OrderStatus.COMPLETED.status
                result.orderList!![0].paymentMethod shouldBe PaymentType.CREDITCARD.type
                result.orderList!![0].orderItems shouldBe mutableListOf(orderItemResponse)
            }
        }
    }

})