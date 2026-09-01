package com.example

import com.example.data.model.BindingType
import com.example.data.model.DefaultMaterials
import com.example.data.model.MaterialCategory
import com.example.data.model.MaterialItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PredefinedBindingTypes
import com.example.data.model.QuoteCalculator
import com.example.data.model.SpineThicknessCalculator
import com.example.data.model.SpineType
import com.example.data.model.WorkshopStep
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testCatalogIntegrity() {
    val bindings = PredefinedBindingTypes.list
    assertTrue("Debe existir al menos un tipo de encuadernación en catálogo", bindings.isNotEmpty())
    
    bindings.forEach { binding ->
      assertNotNull("ID no debe ser nulo", binding.id)
      assertTrue("Nombre no debe estar vacío", binding.name.isNotBlank())
      assertTrue("Precio base debe ser mayor a 0", binding.basePrice > 0.0)
      assertTrue("Debe tener páginas por defecto válidas", binding.defaultPages > 0)
      assertTrue("Debe tener descripción", binding.description.isNotBlank())
      assertTrue("Debe tener usos recomendados", binding.recommendedUses.isNotEmpty())
    }
  }

  @Test
  fun testQuoteCalculationBasic() {
    val hardCover = PredefinedBindingTypes.list.first { it.id == "tapa_dura" }
    val quote = QuoteCalculator.calculate(
      bindingType = hardCover,
      pageCount = 160,
      formatSize = "A5 (14.8 x 21 cm)",
      paperType = "Ahuesado Libro 90g",
      coverMaterial = "Tela de Lino",
      hasRibbon = true,
      hasMetalCorners = true,
      hasElasticBand = false,
      hasMarbledEndpapers = false,
      hasSlipcase = false,
      hasFoil = true,
      quantity = 1,
      customDiscountPercent = 0.0
    )

    assertTrue("Precio unitario debe ser positivo", quote.unitPrice > 0.0)
    assertEquals(1, quote.quantity)
    assertEquals(quote.unitPrice, quote.total, 0.01)
    assertTrue("Costo de papel debe ser positivo", quote.paperCost > 0.0)
    assertTrue("Costo de cubierta debe ser positivo", quote.coverMaterialCost > 0.0)
    assertTrue("Costo de mano de obra debe ser positivo", quote.laborCost > 0.0)
    assertTrue("Debe incluir lista de materiales requeridos", quote.requiredMaterialsList.isNotEmpty())
  }

  @Test
  fun testQuoteVolumeDiscount() {
    val hardCover = PredefinedBindingTypes.list.first { it.id == "tapa_dura" }
    
    // Single unit
    val quoteSingle = QuoteCalculator.calculate(
      bindingType = hardCover,
      pageCount = 100,
      formatSize = "A5 (14.8 x 21 cm)",
      paperType = "Ahuesado Libro 90g",
      coverMaterial = "Tela de Lino",
      hasRibbon = false,
      hasMetalCorners = false,
      hasElasticBand = false,
      hasMarbledEndpapers = false,
      hasSlipcase = false,
      hasFoil = false,
      quantity = 1
    )

    // 10 units (should trigger volume discount)
    val quoteVolume = QuoteCalculator.calculate(
      bindingType = hardCover,
      pageCount = 100,
      formatSize = "A5 (14.8 x 21 cm)",
      paperType = "Ahuesado Libro 90g",
      coverMaterial = "Tela de Lino",
      hasRibbon = false,
      hasMetalCorners = false,
      hasElasticBand = false,
      hasMarbledEndpapers = false,
      hasSlipcase = false,
      hasFoil = false,
      quantity = 10
    )

    assertTrue("Descuento por volumen debe aplicarse a 10 unidades", quoteVolume.volumeDiscountPercent > 0.0)
    assertTrue("Total con 10 unidades debe reflejar descuento", quoteVolume.total < (quoteSingle.unitPrice * 10))
  }

  @Test
  fun testQuoteCustomDiscount() {
    val hardCover = PredefinedBindingTypes.list.first { it.id == "tapa_dura" }
    val quoteWithoutDiscount = QuoteCalculator.calculate(
      bindingType = hardCover,
      pageCount = 100,
      formatSize = "A5 (14.8 x 21 cm)",
      paperType = "Ahuesado Libro 90g",
      coverMaterial = "Tela de Lino",
      hasRibbon = false,
      hasMetalCorners = false,
      hasElasticBand = false,
      hasMarbledEndpapers = false,
      hasSlipcase = false,
      hasFoil = false,
      quantity = 1,
      customDiscountPercent = 0.0
    )

    val quoteWithDiscount = QuoteCalculator.calculate(
      bindingType = hardCover,
      pageCount = 100,
      formatSize = "A5 (14.8 x 21 cm)",
      paperType = "Ahuesado Libro 90g",
      coverMaterial = "Tela de Lino",
      hasRibbon = false,
      hasMetalCorners = false,
      hasElasticBand = false,
      hasMarbledEndpapers = false,
      hasSlipcase = false,
      hasFoil = false,
      quantity = 1,
      customDiscountPercent = 15.0
    )

    assertEquals(15.0, quoteWithDiscount.customDiscountPercent, 0.01)
    assertTrue("Total con descuento debe ser menor", quoteWithDiscount.total < quoteWithoutDiscount.total)
  }

  @Test
  fun testSpineThicknessCalculation() {
    val thickness80g = SpineThicknessCalculator.calculateThicknessMm(
      pageCount = 200,
      paperWeightGsm = 80,
      isHardcover = true
    )
    assertTrue("Grosor de lomo para 200 págs debe ser mayor a 8mm", thickness80g >= 8.0)

    val thicknessWatercolor = SpineThicknessCalculator.calculateThicknessMm(
      pageCount = 100,
      paperWeightGsm = 300,
      isHardcover = true
    )
    assertTrue("Papel acuarela de 300g debe ser más grueso por página", thicknessWatercolor > thickness80g * 0.5)
  }

  @Test
  fun testInventoryAlertsAndDefaults() {
    val defaults = DefaultMaterials.initialList
    assertTrue("Muestrario inicial de materiales debe tener elementos", defaults.isNotEmpty())

    val lowStockItem = MaterialItem(
      id = 1,
      name = "Hilo de Lino Encerado 0.6mm",
      category = MaterialCategory.HILOS,
      currentStock = 2.0,
      minimumStock = 5.0,
      unit = "carretes",
      costPerUnit = 4.50
    )

    assertTrue("Debe detectar alerta de bajo stock", lowStockItem.isLowStock)

    val regularStockItem = MaterialItem(
      id = 2,
      name = "Cartón Gris 2.5mm",
      category = MaterialCategory.CARTON,
      currentStock = 25.0,
      minimumStock = 10.0,
      unit = "pliegos",
      costPerUnit = 2.80
    )

    assertFalse("No debe alertar con stock suficiente", regularStockItem.isLowStock)
  }

  @Test
  fun testOrderStatusWorkflow() {
    val order = OrderEntity(
      id = 101,
      orderNumber = "ORD-2026-001",
      clientName = "Editorial Bellas Artes",
      clientPhone = "+34 600 123 456",
      bindingTypeId = "tapa_dura",
      bindingTypeName = "Tapa Dura Clásica",
      pageCount = 240,
      formatSize = "A4",
      paperType = "Ahuesado 100g",
      coverMaterial = "Piel Cabra",
      coverColorHex = 0xFF4A2A18,
      hasRibbon = true,
      hasMetalCorners = true,
      hasFoilStamping = true,
      foilTitle = "Antología Poética",
      foilSubtitle = "2026",
      quantity = 5,
      unitPrice = 45.0,
      totalPrice = 225.0,
      status = OrderStatus.PENDIENTE
    )

    assertEquals(OrderStatus.PENDIENTE, order.status)
    val confirmed = order.copy(status = OrderStatus.CONFIRMADO)
    assertEquals(OrderStatus.CONFIRMADO, confirmed.status)
    val inWorkshop = confirmed.copy(status = OrderStatus.EN_TALLER)
    assertEquals(OrderStatus.EN_TALLER, inWorkshop.status)
    val delivered = inWorkshop.copy(status = OrderStatus.ENTREGADO)
    assertEquals(OrderStatus.ENTREGADO, delivered.status)
  }
}

