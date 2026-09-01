package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus(val displayName: String, val badgeColorHex: Long) {
    COTIZACION("Presupuesto / Cotización", 0xFF6E7A82),
    CONFIRMADO("Confirmado en Cola", 0xFF2E7D32),
    EN_TALLER("En Taller / Proceso", 0xFFD4A017),
    TERMINADO("Listo para Entrega", 0xFF1976D2),
    ENTREGADO("Entregado", 0xFF388E3C),
    CANCELADO("Cancelado", 0xFFD32F2F)
}

enum class WorkshopStep(val displayName: String, val stepOrder: Int, val description: String) {
    PREPARACION("Plegado y Corte", 1, "Plegado de pliegos en cuadernillos y corte a escuadra"),
    COSTURA("Costura del Lomo", 2, "Costura a mano con hilo de lino y montaje de cintas"),
    ENCOLADO_PRENSADO("Encolado y Prensado", 3, "Aplicación de cola pH neutro, secado en prensa de madera"),
    CABEZADAS_LOMO("Cabezadas y Lomo", 4, "Colocación de cabezadas tejidas y tarlatana de refuerzo"),
    TAPAS_FORRADO("Confección de Tapas", 5, "Corte de cartón, forrado en tela/cuero y hendidos"),
    ESTAMPADO_FOIL("Estampado / Grabado", 6, "Estampado térmico con foil dorado o golpe en seco"),
    ENCARTE_FINAL("Encarte y Prensado 24h", 7, "Pegado de guardas, encarte en tapas y prensado final"),
    CONTROL_CALIDAD("Control de Calidad", 8, "Inspección de apertura, escuadra y empaque protector")
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String, // e.g. "ENC-2026-001"
    val customerName: String,
    val customerPhone: String = "",
    val customerEmail: String = "",
    val customerNotes: String = "",
    
    // Specifications
    val bindingTypeId: String,
    val bindingTypeName: String,
    val formatSize: String = "A5 (14.8 x 21 cm)", // A4, A5, A6, Cuadrado 20x20, Personalizado
    val pageCount: Int = 120,
    val paperType: String = "Ahuesado 90g Book Cream",
    val coverMaterial: String = "Tela de Lino",
    val coverColorHex: Long = 0xFF2D5A43,
    val customTextureUri: String? = null,
    val foilTitle: String = "",
    val foilSubtitle: String = "",
    val foilColor: String = "Dorado", // Dorado, Plateado, Cobre, Golpe Seco
    
    // Extras
    val hasRibbonBookmark: Boolean = true,
    val hasMetalCorners: Boolean = true,
    val hasElasticBand: Boolean = false,
    val hasMarbledEndpapers: Boolean = true,
    val hasSlipcase: Boolean = false,
    val quantity: Int = 1,
    
    // Pricing
    val materialCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val extrasCost: Double = 0.0,
    val subtotal: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val depositPaid: Double = 0.0,
    val balanceDue: Double = 0.0,
    
    // Status & Tracking
    val status: OrderStatus = OrderStatus.COTIZACION,
    val currentWorkshopStep: WorkshopStep = WorkshopStep.PREPARACION,
    val completedStepsMask: Int = 0, // Bitmask or step count
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val estimatedDeliveryDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 days default
    val deliveredAt: Long? = null,
    val receiverName: String = "",
    val receiverDniOrPhone: String = "",
    val deliveryNotes: String = "",
    val isDeliverySigned: Boolean = false
)
