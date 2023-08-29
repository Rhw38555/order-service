package com.example.orderservice.service

import com.example.orderservice.dto.*
import com.example.orderservice.entity.Order
import com.example.orderservice.entity.OrderItem
import com.example.orderservice.enum.OrderStatus
import com.example.orderservice.enum.PaymentType
import com.example.orderservice.exception.*
import com.example.orderservice.repository.CustomerRepository
import com.example.orderservice.repository.OrderRepository
import com.example.orderservice.repository.ProductRepository
import com.example.setting.GlobalSettings
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {

    // 주문 접수
    @Transactional
    fun createOrder(createOrderRequest: CreateOrderRequest) : CreateOrderResponse {

        // 고객, 상품 정보 확인
        val customer = customerRepository.findByIdOrNull(createOrderRequest.customerId)
            ?: throw CustomerNotFoundException()
        val product = productRepository.findByIdWithLock(createOrderRequest.productId)
            ?: throw ProductNotFoundException()

        val order = Order(customer=customer,
            status = OrderStatus.CREATED, orderDate = LocalDateTime.now())
        val orderItem = OrderItem(order=order, product=product, quantity = createOrderRequest.quantity)
        val orderItems = mutableListOf(orderItem)
        order.orderItems = orderItems

        // 결제 정보 확인, 존재하면 status 변경
        createOrderRequest.paymentMethod?.let{
            order.status = OrderStatus.COMPLETED
        }
        return with(orderRepository.save(order)){
            // 상품 재고 감소
            product.decreaseQuantity(createOrderRequest.quantity)
            productRepository.save(product)
            CreateOrderResponse(id!!, status.status)
        }
    }

    // 주문 완료
    @Transactional
    fun completeOrder(completeOrderRequest: CompleteOrderRequest): CompleteOrderResponse{

        //주문 정보 확인
        val order = orderRepository.findByIdOrNull(completeOrderRequest.orderId) ?: throw OrderNotFoundException()

        // 주문 결제 값 요청 결제 값 비교
        val orderAmount = order.orderItems[0].product.price.multiply(BigDecimal(order.orderItems[0].quantity)).toInt()
//        if(!completeOrderRequest.paymentAmount.equals(orderAmount)){
        if(completeOrderRequest.paymentAmount != orderAmount){
            throw DifferentPaymentAmountException()
        }

        // 주문 상태 변경
        order.paymentMethod = PaymentType.findByType(completeOrderRequest.paymentMethod)
        order.status = OrderStatus.COMPLETED

        return with(orderRepository.save(order)){
            CompleteOrderResponse(id!!, status.status)
        }
    }

    // 단일 주문 정보
    fun getOrder(orderId: Long) : OrderResponse {
        //주문 정보 확인
        val order = orderRepository.findByIdOrNull(orderId) ?: throw OrderNotFoundException()

        return with(order){
            val tmpOrderItems = order.orderItems.map { orderItem ->
                OrderItemResponse(
                    orderItem.product.id!!,
                    orderItem.product.name,
                    orderItem.product.price.intValueExact(),
                    orderItem.quantity
                )
            }

            val paymentMethodType = order.paymentMethod?.type ?: "결제전"

            OrderResponse(
                order.id!!,
                order.customer.id!!,
                tmpOrderItems,
                order.status.status,
                paymentMethodType,
                order.orderDate.format(GlobalSettings.dateFormatShort)
            )
        }
    }

    // 사용자 기준 주문 목록 정보
    fun getOrderList(customerId: Long) : OrderListResponse {
        //주문 정보 확인
        val orderList = orderRepository.findByCustomerId(customerId)
        val orderListResponse = mutableListOf<OrderResponse>()
        return with(orderList){


            val orderListResponse = map { order ->
                val tmpOrderItems = order.orderItems.map { orderItem ->
                    OrderItemResponse(
                        orderItem.product.id!!,
                        orderItem.product.name,
                        orderItem.product.price.intValueExact(),
                        orderItem.quantity
                    )
                }

                val paymentMethodType = order.paymentMethod?.type ?: "결제전"

                OrderResponse(
                    order.id!!,
                    order.customer.id!!,
                    tmpOrderItems,
                    order.status.status,
                    paymentMethodType,
                    order.orderDate.format(GlobalSettings.dateFormatShort)
                )
            }
            // 주문이 있는 경우 처리
            OrderListResponse(orderListResponse)
        }
    }

}
