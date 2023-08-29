package com.example.orderservice.entity

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "tb_order_item")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    var quantity: Int,

    ) : BaseEntity(), Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderItem

        if (id != other.id) return false

        return true
    }
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}