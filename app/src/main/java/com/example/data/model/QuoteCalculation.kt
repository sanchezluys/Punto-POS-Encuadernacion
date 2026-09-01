package com.example.data.model

data class QuoteResult(
    val basePrice: Double,
    val paperCost: Double,
    val coverMaterialCost: Double,
    val extrasCost: Double,
    val laborCost: Double,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double,
    val volumeDiscountPercent: Double,
    val customDiscountPercent: Double,
    val totalDiscountAmount: Double,
    val total: Double,
    val requiredMaterialsList: List<MaterialRequirement>
)

data class MaterialRequirement(
    val materialName: String,
    val quantityNeeded: Double,
    val unit: String,
    val estimatedCost: Double
)

object QuoteCalculator {
    fun calculate(
        bindingType: BindingType,
        pageCount: Int,
        formatSize: String,
        paperType: String,
        coverMaterial: String,
        hasRibbon: Boolean,
        hasMetalCorners: Boolean,
        hasElasticBand: Boolean,
        hasMarbledEndpapers: Boolean,
        hasSlipcase: Boolean,
        hasFoil: Boolean,
        quantity: Int,
        customDiscountPercent: Double = 0.0
    ): QuoteResult {
        // Size multiplier
        val sizeMultiplier = when {
            formatSize.contains("A4", ignoreCase = true) -> 1.45
            formatSize.contains("Cuadrado 20x20", ignoreCase = true) -> 1.30
            formatSize.contains("A6", ignoreCase = true) -> 0.80
            else -> 1.0 // A5
        }

        // Paper sheet cost (1 pliego = approx 8-16 pages depending on format)
        val pagesPerSheet = if (formatSize.contains("A4")) 8 else 16
        val sheetsNeeded = (pageCount.toDouble() / pagesPerSheet).coerceAtLeast(4.0)
        
        val paperUnitCost = when {
            paperType.contains("Acuarela", ignoreCase = true) -> 2.80
            paperType.contains("Kraft", ignoreCase = true) -> 0.35
            paperType.contains("Marmoleado", ignoreCase = true) -> 3.50
            else -> 0.45 // Ahuesado
        }
        val calculatedPaperCost = sheetsNeeded * paperUnitCost

        // Cover material cost
        val coverCost = when {
            coverMaterial.contains("Cuero", ignoreCase = true) -> 14.50 * sizeMultiplier
            coverMaterial.contains("Lino", ignoreCase = true) -> 7.50 * sizeMultiplier
            coverMaterial.contains("Marmoleado", ignoreCase = true) -> 5.80 * sizeMultiplier
            coverMaterial.contains("Seda", ignoreCase = true) -> 9.00 * sizeMultiplier
            else -> 4.00 * sizeMultiplier // Kraft / Cartulina
        }

        // Extras
        var extrasSum = 0.0
        val requirements = mutableListOf<MaterialRequirement>()

        requirements.add(
            MaterialRequirement(
                materialName = "Papel interior ($paperType)",
                quantityNeeded = sheetsNeeded * quantity,
                unit = "pliegos",
                estimatedCost = calculatedPaperCost * quantity
            )
        )

        requirements.add(
            MaterialRequirement(
                materialName = "Material Cubierta ($coverMaterial)",
                quantityNeeded = 1.0 * quantity,
                unit = "unidad",
                estimatedCost = coverCost * quantity
            )
        )

        if (bindingType.spineType != SpineType.EXPOSED_COPTIC && bindingType.spineType != SpineType.SPIRAL_WIRE) {
            requirements.add(
                MaterialRequirement(
                    materialName = "Cartón Gris Prensado 2.5mm",
                    quantityNeeded = 0.25 * sizeMultiplier * quantity,
                    unit = "pliego",
                    estimatedCost = 0.90 * sizeMultiplier * quantity
                )
            )
        }

        if (hasRibbon) {
            extrasSum += 1.50
            requirements.add(
                MaterialRequirement(
                    materialName = "Cinta Señaladora Raso",
                    quantityNeeded = 0.4 * quantity,
                    unit = "metros",
                    estimatedCost = 0.30 * quantity
                )
            )
        }

        if (hasMetalCorners) {
            extrasSum += 3.20
            requirements.add(
                MaterialRequirement(
                    materialName = "Esquineros de Bronce (4 uds)",
                    quantityNeeded = 1.0 * quantity,
                    unit = "set",
                    estimatedCost = 1.20 * quantity
                )
            )
        }

        if (hasElasticBand) {
            extrasSum += 2.00
            requirements.add(
                MaterialRequirement(
                    materialName = "Elástico Plano de Cierre",
                    quantityNeeded = 0.35 * quantity,
                    unit = "metros",
                    estimatedCost = 0.40 * quantity
                )
            )
        }

        if (hasMarbledEndpapers) {
            extrasSum += 4.50 * sizeMultiplier
            requirements.add(
                MaterialRequirement(
                    materialName = "Guardas Marmoleadas Florentinas",
                    quantityNeeded = 0.5 * sizeMultiplier * quantity,
                    unit = "pliego",
                    estimatedCost = 1.75 * sizeMultiplier * quantity
                )
            )
        }

        if (hasSlipcase) {
            extrasSum += 12.00 * sizeMultiplier
            requirements.add(
                MaterialRequirement(
                    materialName = "Caja Contenedora / Slipcase",
                    quantityNeeded = 1.0 * quantity,
                    unit = "unidad",
                    estimatedCost = 4.50 * sizeMultiplier * quantity
                )
            )
        }

        if (hasFoil) {
            extrasSum += 5.00
            requirements.add(
                MaterialRequirement(
                    materialName = "Foil Térmico Metalizado",
                    quantityNeeded = 0.2 * quantity,
                    unit = "metros",
                    estimatedCost = 0.60 * quantity
                )
            )
        }

        // Labor calculation
        val pageFactor = (pageCount - 80).coerceAtLeast(0) * 0.04
        val laborCost = (bindingType.basePrice * 0.45 * sizeMultiplier) + pageFactor

        val unitPriceRaw = (bindingType.basePrice * sizeMultiplier) + calculatedPaperCost + (coverCost * 0.6) + extrasSum
        val unitPrice = String.format(java.util.Locale.US, "%.2f", unitPriceRaw).toDouble()

        val subtotalRaw = unitPrice * quantity
        
        // Automatic volume discounts
        val volumeDiscount = when {
            quantity >= 25 -> 20.0
            quantity >= 10 -> 15.0
            quantity >= 5 -> 10.0
            quantity >= 3 -> 5.0
            else -> 0.0
        }

        val effectiveDiscountPercent = (volumeDiscount + customDiscountPercent).coerceAtMost(50.0)
        val discountAmount = (subtotalRaw * (effectiveDiscountPercent / 100.0))
        val total = (subtotalRaw - discountAmount).coerceAtLeast(0.0)

        return QuoteResult(
            basePrice = bindingType.basePrice,
            paperCost = calculatedPaperCost,
            coverMaterialCost = coverCost,
            extrasCost = extrasSum,
            laborCost = laborCost,
            unitPrice = unitPrice,
            quantity = quantity,
            subtotal = subtotalRaw,
            volumeDiscountPercent = volumeDiscount,
            customDiscountPercent = customDiscountPercent,
            totalDiscountAmount = discountAmount,
            total = total,
            requiredMaterialsList = requirements
        )
    }
}
