package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MaterialCategory(val displayName: String) {
    CUBIERTAS("Cueros y Telas"),
    PAPELES("Papeles y Cartulinas"),
    CARTONES("Cartones Estructurales"),
    HILOS("Hilos y Cabezadas"),
    METALES("Herrajes y Metales"),
    QUIMICOS("Adhesivos y Acabados")
}

@Entity(tableName = "materials")
data class MaterialItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: MaterialCategory,
    val unit: String, // e.g. "hojas", "m²", "metros", "pares", "ml", "pliegos"
    val unitCost: Double,
    val currentStock: Double,
    val minStockAlert: Double,
    val notes: String = ""
) {
    val isLowStock: Boolean
        get() = currentStock <= minStockAlert
}

object DefaultMaterials {
    val initialList = listOf(
        MaterialItem(
            name = "Cuero Vacuno Curtido Natural (Marrón)",
            category = MaterialCategory.CUBIERTAS,
            unit = "dm²",
            unitCost = 1.20,
            currentStock = 150.0,
            minStockAlert = 30.0,
            notes = "Calibre 1.2mm, ideal para repujado y grabado térmico"
        ),
        MaterialItem(
            name = "Tela de Lino Holandés (Verde Esmeralda)",
            category = MaterialCategory.CUBIERTAS,
            unit = "metros",
            unitCost = 6.50,
            currentStock = 18.0,
            minStockAlert = 5.0,
            notes = "Respaldo de papel para encolar sin traspasar"
        ),
        MaterialItem(
            name = "Tela de Algodón Premium (Azul Marino)",
            category = MaterialCategory.CUBIERTAS,
            unit = "metros",
            unitCost = 5.80,
            currentStock = 22.0,
            minStockAlert = 5.0,
            notes = "Tejido cerrado de alta resistencia"
        ),
        MaterialItem(
            name = "Papel Ahuesado Book Cream 90g (Pliego 70x100)",
            category = MaterialCategory.PAPELES,
            unit = "pliegos",
            unitCost = 0.45,
            currentStock = 350.0,
            minStockAlert = 80.0,
            notes = "Tono cálido, libre de ácido, ideal lectura"
        ),
        MaterialItem(
            name = "Papel Kraft Verjurado 120g",
            category = MaterialCategory.PAPELES,
            unit = "pliegos",
            unitCost = 0.35,
            currentStock = 200.0,
            minStockAlert = 50.0,
            notes = "Textura artesanal rústica"
        ),
        MaterialItem(
            name = "Papel Acuarela 300g 100% Algodón",
            category = MaterialCategory.PAPELES,
            unit = "pliegos",
            unitCost = 2.80,
            currentStock = 45.0,
            minStockAlert = 15.0,
            notes = "Grano fino para artbooks y bocetos"
        ),
        MaterialItem(
            name = "Papel Marmoleado al Agua (Florentino)",
            category = MaterialCategory.PAPELES,
            unit = "pliegos",
            unitCost = 3.50,
            currentStock = 28.0,
            minStockAlert = 10.0,
            notes = "Para guardas interiores y medias pastas"
        ),
        MaterialItem(
            name = "Cartón Gris Prensado 2.5mm (Kappa)",
            category = MaterialCategory.CARTONES,
            unit = "pliegos",
            unitCost = 1.80,
            currentStock = 60.0,
            minStockAlert = 15.0,
            notes = "Densidad óptima para tapas duras indeformables"
        ),
        MaterialItem(
            name = "Hilo de Lino Encerado 4 Cabos",
            category = MaterialCategory.HILOS,
            unit = "bobinas (100m)",
            unitCost = 8.50,
            currentStock = 12.0,
            minStockAlert = 3.0,
            notes = "Excelente tensión y deslizamiento sin nudos"
        ),
        MaterialItem(
            name = "Esquineros Metálicos de Bronce Envejecido",
            category = MaterialCategory.METALES,
            unit = "sets (4 uds)",
            unitCost = 1.20,
            currentStock = 85.0,
            minStockAlert = 20.0,
            notes = "Protección de esquinas para tapas duras"
        ),
        MaterialItem(
            name = "Cinta Señaladora de Raso 6mm (Dorado/Borgoña)",
            category = MaterialCategory.HILOS,
            unit = "metros",
            unitCost = 0.25,
            currentStock = 120.0,
            minStockAlert = 25.0,
            notes = "Cinta de registro satinada doble cara"
        ),
        MaterialItem(
            name = "Foil Dorado para Estampado Térmico",
            category = MaterialCategory.QUIMICOS,
            unit = "metros",
            unitCost = 0.80,
            currentStock = 40.0,
            minStockAlert = 10.0,
            notes = "Fijación por calor para tipos móviles y clisés"
        ),
        MaterialItem(
            name = "Cola PVA pH Neutro para Encuadernación",
            category = MaterialCategory.QUIMICOS,
            unit = "kg",
            unitCost = 4.20,
            currentStock = 14.0,
            minStockAlert = 4.0,
            notes = "Adhesivo flexible que no cristaliza ni amarillea"
        )
    )
}
