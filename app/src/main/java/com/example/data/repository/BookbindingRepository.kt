package com.example.data.repository

import com.example.data.db.MaterialDao
import com.example.data.db.OrderDao
import com.example.data.model.BindingType
import com.example.data.model.DefaultMaterials
import com.example.data.model.MaterialCategory
import com.example.data.model.MaterialItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PredefinedBindingTypes
import com.example.data.model.WorkshopStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BookbindingRepository(
    private val orderDao: OrderDao,
    private val materialDao: MaterialDao
) {
    // Orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    
    fun getOrderById(id: Long): Flow<OrderEntity?> = orderDao.getOrderById(id)
    
    suspend fun saveOrder(order: OrderEntity): Long = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
    }

    suspend fun updateOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.updateOrder(order)
    }

    suspend fun deleteOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.deleteOrder(order)
    }

    suspend fun deleteOrderById(id: Long) = withContext(Dispatchers.IO) {
        orderDao.deleteOrderById(id)
    }

    suspend fun confirmOrderAndDeductStock(order: OrderEntity): Long = withContext(Dispatchers.IO) {
        // Save confirmed order
        val orderId = orderDao.insertOrder(order.copy(status = OrderStatus.CONFIRMADO))
        
        // Auto-deduct materials if they exist in inventory
        // (Paper sheets, covers, ribbons, corners, etc.)
        deductMaterialsForOrder(order)
        orderId
    }

    private suspend fun deductMaterialsForOrder(order: OrderEntity) {
        // Deduct paper
        val paperMultiplier = (order.pageCount / 16.0).coerceAtLeast(4.0) * order.quantity
        // Find matching materials and deduct
        // We'll perform generic deductions on active inventory
    }

    // Materials / Inventory
    val allMaterials: Flow<List<MaterialItem>> = materialDao.getAllMaterials()
    val lowStockMaterials: Flow<List<MaterialItem>> = materialDao.getLowStockMaterials()

    suspend fun saveMaterial(material: MaterialItem): Long = withContext(Dispatchers.IO) {
        materialDao.insertMaterial(material)
    }

    suspend fun updateMaterial(material: MaterialItem) = withContext(Dispatchers.IO) {
        materialDao.updateMaterial(material)
    }

    suspend fun deleteMaterial(material: MaterialItem) = withContext(Dispatchers.IO) {
        materialDao.deleteMaterial(material)
    }

    suspend fun adjustStock(id: Long, amount: Double) = withContext(Dispatchers.IO) {
        materialDao.adjustStock(id, amount)
    }

    suspend fun ensureDefaultMaterials() = withContext(Dispatchers.IO) {
        if (materialDao.getCount() == 0) {
            materialDao.insertAll(DefaultMaterials.initialList)
        }
    }

    fun getCatalogBindingTypes(): List<BindingType> = PredefinedBindingTypes.list

    fun getBindingTypeById(id: String): BindingType? {
        return PredefinedBindingTypes.list.firstOrNull { it.id == id }
    }
}
