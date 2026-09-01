package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MaterialCategory
import com.example.data.model.MaterialItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY category, name ASC")
    fun getAllMaterials(): Flow<List<MaterialItem>>

    @Query("SELECT * FROM materials WHERE category = :category ORDER BY name ASC")
    fun getMaterialsByCategory(category: MaterialCategory): Flow<List<MaterialItem>>

    @Query("SELECT * FROM materials WHERE currentStock <= minStockAlert")
    fun getLowStockMaterials(): Flow<List<MaterialItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<MaterialItem>)

    @Update
    suspend fun updateMaterial(material: MaterialItem)

    @Delete
    suspend fun deleteMaterial(material: MaterialItem)

    @Query("UPDATE materials SET currentStock = currentStock + :amount WHERE id = :id")
    suspend fun adjustStock(id: Long, amount: Double)

    @Query("SELECT COUNT(*) FROM materials")
    suspend fun getCount(): Int
}
