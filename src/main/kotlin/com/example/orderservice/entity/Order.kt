package com.example.orderservice.entity

import com.example.orderservice.enum.OrderStatus
import com.example.orderservice.enum.PaymentType
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "tb_order")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL])
    var orderItems: List<OrderItem> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentType? = null,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.CREATED,

    var orderDate: LocalDateTime,

    ) : BaseEntity(), Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Order

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}