package com.example

import com.example.data.model.BindingType
import com.example.data.model.DefaultMaterials
import com.example.data.model.MaterialCategory
import com.example.data.model.MaterialItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PredefinedBindingTypes
import com.example.data.model.QuoteCalculator
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
  fun testInventoryAlertsAndDefaults() {
    val defaults = DefaultMaterials.initialList
    assertTrue("Muestrario inicial de materiales debe tener elementos", defaults.isNotEmpty())

    val lowStockItem = MaterialItem(
      id = 1,
      name = "Hilo de Lino Encerado 0.6mm",
      category = MaterialCategory.HILOS,
      unit = "carretes",
      unitCost = 4.50,
      currentStock = 2.0,
      minStockAlert = 5.0
    )

    assertTrue("Debe detectar alerta de bajo stock", lowStockItem.isLowStock)

    val regularStockItem = MaterialItem(
      id = 2,
      name = "Cartón Gris 2.5mm",
      category = MaterialCategory.CARTONES,
      unit = "pliegos",
      unitCost = 2.80,
      currentStock = 25.0,
      minStockAlert = 10.0
    )

    assertFalse("No debe alertar con stock suficiente", regularStockItem.isLowStock)
  }

  @Test
  fun testOrderStatusWorkflow() {
    val order = OrderEntity(
      id = 101,
      orderNumber = "ORD-2026-001",
      customerName = "Editorial Bellas Artes",
      customerPhone = "+34 600 123 456",
      bindingTypeId = "tapa_dura",
      bindingTypeName = "Tapa Dura Clásica",
      pageCount = 240,
      formatSize = "A4",
      paperType = "Ahuesado 100g",
      coverMaterial = "Piel Cabra",
      coverColorHex = 0xFF4A2A18,
      hasRibbonBookmark = true,
      hasMetalCorners = true,
      foilTitle = "Antología Poética",
      foilSubtitle = "2026",
      quantity = 5,
      totalAmount = 225.0,
      status = OrderStatus.COTIZACION
    )

    assertEquals(OrderStatus.COTIZACION, order.status)
    val confirmed = order.copy(status = OrderStatus.CONFIRMADO)
    assertEquals(OrderStatus.CONFIRMADO, confirmed.status)
    val inWorkshop = confirmed.copy(status = OrderStatus.EN_TALLER)
    assertEquals(OrderStatus.EN_TALLER, inWorkshop.status)
    val delivered = inWorkshop.copy(status = OrderStatus.ENTREGADO)
    assertEquals(OrderStatus.ENTREGADO, delivered.status)
  }

  @Test
  fun testSpineAndDimensionCalculations() {
    val formats = com.example.data.model.PredefinedBookFormats.list
    assertTrue("Debe haber formatos predefinidos", formats.isNotEmpty())

    val papers = com.example.data.model.PredefinedPapers.list
    assertTrue("Debe haber papeles predefinidos", papers.isNotEmpty())

    // 60 sheets of 90g hardcover book
    val spineThicknessHardcover = com.example.data.model.SpineThicknessCalculator.calculateThicknessMm(
      sheetCount = 60,
      paperGrammageGsm = 90,
      isHardcover = true
    )
    assertTrue("Lomo tapa dura debe tener grosor adecuado", spineThicknessHardcover > 10.0 && spineThicknessHardcover < 25.0)

    // 60 sheets of softcover / open spine book
    val spineThicknessSoftcover = com.example.data.model.SpineThicknessCalculator.calculateThicknessMm(
      sheetCount = 60,
      paperGrammageGsm = 90,
      isHardcover = false
    )
    assertTrue("Lomo sin tapas duras debe ser más delgado", spineThicknessSoftcover < spineThicknessHardcover)
  }

  @Test
  fun testLatinAmericanBindingDiversity() {
    val bindings = PredefinedBindingTypes.list
    
    // External sewing
    assertTrue("Debe incluir costura externa", bindings.any { it.spineType == SpineType.FRENCH_EXTERNAL || it.spineType == SpineType.EXPOSED_COPTIC })
    // No spine / open spine
    assertTrue("Debe incluir sin lomo / lomo expuesto", bindings.any { it.spineType == SpineType.OPEN_SPINE })
    // French internal and external
    assertTrue("Debe incluir costura francesa interna", bindings.any { it.spineType == SpineType.FRENCH_INTERNAL })
    assertTrue("Debe incluir costura francesa externa", bindings.any { it.spineType == SpineType.FRENCH_EXTERNAL })
    // Japanese internal and external
    assertTrue("Debe incluir costura japonesa interna", bindings.any { it.spineType == SpineType.JAPANESE_INTERNAL })
    assertTrue("Debe incluir costura japonesa externa", bindings.any { it.spineType == SpineType.JAPANESE_EXTERNAL })
  }

  @Test
  fun testProposalExportSpecIntegrity() {
    val binding = PredefinedBindingTypes.list.first { it.spineType == SpineType.EXPOSED_COPTIC }
    val quote = com.example.data.model.QuoteCalculator.calculate(
      bindingType = binding,
      pageCount = 120,
      formatSize = "A5 (14.8 x 21.0 cm)",
      paperType = "Ahuesado 90g Book Cream",
      coverMaterial = "Cuero Vacuno Envejecido",
      hasRibbon = true,
      hasMetalCorners = true,
      hasElasticBand = false,
      hasMarbledEndpapers = true,
      hasSlipcase = true,
      hasFoil = true,
      quantity = 1,
      customDiscountPercent = 0.0
    )

    val spec = com.example.util.ProposalExportSpec(
      bindingType = binding,
      widthCm = 14.8f,
      lengthCm = 21.0f,
      spineThicknessMm = 16.2f,
      sheetCount = 60,
      pageCount = 120,
      paperType = "Ahuesado 90g Book Cream",
      coverMaterial = "Cuero Vacuno Envejecido",
      coverColorHex = 0xFF5C2C16,
      foilTitle = "DIARIO DE VIAJES",
      foilSubtitle = "EDICIÓN DE COLECCIÓN",
      foilColorType = "Oro",
      hasRibbon = true,
      hasCorners = true,
      hasSlipcase = true,
      hasEndpapers = true,
      clientName = "María Fernanda",
      clientNotes = "Con estuche a medida y papel ahuesado",
      quoteResult = quote,
      estimatedDays = 5
    )

    assertEquals("Costura Copta Expuesta", spec.bindingType.name)
    assertEquals(14.8f, spec.widthCm, 0.01f)
    assertEquals(21.0f, spec.lengthCm, 0.01f)
    assertEquals(16.2f, spec.spineThicknessMm, 0.01f)
    assertTrue("Debe tener foil dorado", spec.foilColorType == "Oro")
    assertTrue("Total cotizado debe ser mayor a 0", spec.quoteResult.total > 0)
    assertEquals("María Fernanda", spec.clientName)
  }
}

