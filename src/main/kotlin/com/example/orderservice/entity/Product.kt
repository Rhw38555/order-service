package com.example.orderservice.entity

import com.example.orderservice.exception.ProductQuantityLessZeroException
import java.io.Serializable
import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "tb_product")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String = "",

    var price: BigDecimal = BigDecimal.ZERO,

    var quantity: Int,

) : BaseEntity(), Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (id != other.id) return false

        return true
    }
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    fun decreaseQuantity(quantityParam:Int = 1){
        if(quantity - quantityParam < 0){
            throw ProductQuantityLessZeroException()
        }
        quantity -= quantityParam
    }

    fun increaseQuantity(quantityParam:Int = 1){
        quantity += quantityParam
    }
}