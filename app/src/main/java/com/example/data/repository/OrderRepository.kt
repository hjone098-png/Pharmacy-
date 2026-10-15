package com.example.data.repository

import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrdersFlow()

    suspend fun getOrderById(id: Long): OrderEntity? = orderDao.getOrderById(id)

    suspend fun saveOrder(order: OrderEntity): Long = orderDao.insertOrder(order)

    suspend fun deleteOrder(order: OrderEntity) = orderDao.deleteOrder(order)

    suspend fun deleteOrderById(id: Long) = orderDao.deleteOrderById(id)

    suspend fun getCount(): Int = orderDao.getCount()
}
