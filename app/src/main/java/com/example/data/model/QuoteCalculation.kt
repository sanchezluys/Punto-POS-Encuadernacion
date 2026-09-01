package com.example.data.model

import java.util.Locale
import kotlin.math.ceil

data class BookFormatOption(
    val id: String,
    val name: String,
    val widthCm: Double,
    val lengthCm: Double,
    val description: String
) {
    val displaySize: String
        get() = "${widthCm} x ${lengthCm} cm"
}

object PredefinedBookFormats {
    val A5 = BookFormatOption("a5", "A5 (14.8 x 21.0 cm)", 14.8, 21.0, "Formato estándar para narrativa, cuadernos y diarios")
    val A4 = BookFormatOption("a4", "A4 (21.0 x 29.7 cm)", 21.0, 29.7, "Gran formato para atlas, catálogos y partituras")
    val A6 = BookFormatOption("a6", "A6 (10.5 x 14.8 cm)", 10.5, 14.8, "Libreta de bolsillo compacta y pasaportes")
    val CUADRADO_20 = BookFormatOption("sq20", "Cuadrado 20x20 cm", 20.0, 20.0, "Álbum fotográfico y libros de arte")
    val CUADRADO_15 = BookFormatOption("sq15", "Cuadrado 15x15 cm", 15.0, 15.0, "Mini álbum o poemarios")
    val B5 = BookFormatOption("b5", "B5 (17.6 x 25.0 cm)", 17.6, 25.0, "Cuaderno ejecutivo y sketchbook")
    val CUSTOM = BookFormatOption("custom", "Medida Personalizada", 14.8, 21.0, "Largo y ancho definidos a medida")

    val list = listOf(A5, A4, A6, CUADRADO_20, CUADRADO_15, B5, CUSTOM)
}

data class PaperOption(
    val id: String,
    val name: String,
    val grammageGsm: Int,
    val caliberMm: Double,
    val sheetsPerSignature: Int,
    val description: String,
    val unitCostPerSheet: Double
)

object PredefinedPapers {
    val BOND_80 = PaperOption("bond_80", "Bond Blanco 80g", 80, 0.100, 4, "Papel multiuso estándar para notas rápidas", 0.30)
    val AHUESADO_90 = PaperOption("ahuesado_90", "Ahuesado 90g Book Cream", 90, 0.135, 4, "Tono marfil cálido antifatiga para lectura y novela", 0.45)
    val OFFSET_100 = PaperOption("offset_100", "Offset Satinado 100g", 100, 0.145, 4, "Superficie lisa ideal para pluma estilográfica", 0.55)
    val KRAFT_120 = PaperOption("kraft_120", "Kraft Verjurado 120g", 120, 0.170, 4, "Textura rústica y fibras naturales resistentes", 0.65)
    val BRISTOL_160 = PaperOption("bristol_160", "Cartulina Bristol 160g", 160, 0.210, 3, "Gran cuerpo para bocetos, rotuladores y fotos", 0.95)
    val SKETCH_200 = PaperOption("sketch_200", "Dibujo Canson Sketch 200g", 200, 0.260, 2, "Gramaje artístico para carboncillo, grafito y tintas", 1.45)
    val ACUARELA_300 = PaperOption("acuarela_300", "Papel Acuarela 300g Algodón", 300, 0.420, 2, "100% algodón prensado en frío para técnicas húmedas", 2.80)

    val list = listOf(AHUESADO_90, BOND_80, OFFSET_100, KRAFT_120, BRISTOL_160, SKETCH_200, ACUARELA_300)
}

object SpineThicknessCalculator {
    fun calculateThicknessMm(
        sheetCount: Int,
        paperGrammageGsm: Int,
        isHardcover: Boolean = true
    ): Double {
        val caliberMm = when {
            paperGrammageGsm >= 300 -> 0.420
            paperGrammageGsm >= 200 -> 0.260
            paperGrammageGsm >= 160 -> 0.210
            paperGrammageGsm >= 120 -> 0.170
            paperGrammageGsm >= 100 -> 0.145
            paperGrammageGsm >= 90 -> 0.135
            else -> 0.100
        }
        val paperBlockMm = sheetCount * caliberMm
        val coverThicknessMm = if (isHardcover) 4.6 else 1.0 // 2 tapas cartón 2.2mm + guardas
        val hingeJointAllowanceMm = if (isHardcover) 1.4 else 0.4
        val total = paperBlockMm + coverThicknessMm + hingeJointAllowanceMm
        return String.format(Locale.US, "%.1f", total.coerceIn(3.0, 90.0)).toDouble()
    }
}

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
    val requiredMaterialsList: List<MaterialRequirement>,
    // Technical book specs
    val spineThicknessMm: Double = 16.0,
    val sheetCount: Int = 60,
    val pageCount: Int = 120,
    val signaturesCount: Int = 15,
    val widthCm: Double = 14.8,
    val lengthCm: Double = 21.0,
    val paperWeightGrams: Double = 220.0,
    val coverAreaCm2: Double = 850.0
)

data class MaterialRequirement(
    val materialName: String,
    val quantityNeeded: Double,
    val unit: String,
    val estimatedCost: Double
)

object QuoteCalculator {
    val standardFormats = PredefinedBookFormats.list
    val standardPapers = PredefinedPapers.list

    fun calculate(
        bindingType: BindingType,
        pageCount: Int = 120,
        formatSize: String = "A5 (14.8 x 21 cm)",
        paperType: String = "Ahuesado 90g Book Cream",
        coverMaterial: String = "Tela de Lino",
        hasRibbon: Boolean = true,
        hasMetalCorners: Boolean = true,
        hasElasticBand: Boolean = false,
        hasMarbledEndpapers: Boolean = true,
        hasSlipcase: Boolean = false,
        hasFoil: Boolean = true,
        quantity: Int = 1,
        customDiscountPercent: Double = 0.0,
        widthCmOverride: Double? = null,
        lengthCmOverride: Double? = null,
        grammageOverride: Int? = null,
        sheetCountOverride: Int? = null
    ): QuoteResult {
        // Resolve Sheets & Pages
        val effectiveSheets = sheetCountOverride ?: (pageCount / 2).coerceAtLeast(10)
        val effectivePages = effectiveSheets * 2

        // Resolve Width & Length
        val (widthCm, lengthCm) = if (widthCmOverride != null && lengthCmOverride != null) {
            Pair(widthCmOverride, lengthCmOverride)
        } else when {
            formatSize.contains("A4", ignoreCase = true) -> Pair(21.0, 29.7)
            formatSize.contains("Cuadrado 20x20", ignoreCase = true) -> Pair(20.0, 20.0)
            formatSize.contains("Cuadrado 15x15", ignoreCase = true) -> Pair(15.0, 15.0)
            formatSize.contains("A6", ignoreCase = true) -> Pair(10.5, 14.8)
            formatSize.contains("B5", ignoreCase = true) -> Pair(17.6, 25.0)
            else -> Pair(14.8, 21.0) // A5 standard
        }

        // Resolve Paper Grammage
        val grammage = grammageOverride ?: when {
            paperType.contains("300g", ignoreCase = true) || paperType.contains("Acuarela", ignoreCase = true) -> 300
            paperType.contains("200g", ignoreCase = true) || paperType.contains("Sketch", ignoreCase = true) -> 200
            paperType.contains("160g", ignoreCase = true) || paperType.contains("Bristol", ignoreCase = true) -> 160
            paperType.contains("120g", ignoreCase = true) || paperType.contains("Kraft", ignoreCase = true) -> 120
            paperType.contains("100g", ignoreCase = true) -> 100
            paperType.contains("80g", ignoreCase = true) -> 80
            else -> 90 // Ahuesado
        }

        val isHardcover = bindingType.spineType != SpineType.OPEN_SPINE &&
                bindingType.spineType != SpineType.JAPANESE_EXTERNAL

        // Exact spine thickness calculation
        val spineThicknessMm = SpineThicknessCalculator.calculateThicknessMm(
            sheetCount = effectiveSheets,
            paperGrammageGsm = grammage,
            isHardcover = isHardcover
        )

        // Signatures count
        val sheetsPerSignature = when {
            grammage >= 300 -> 2
            grammage >= 200 -> 2
            grammage >= 160 -> 3
            else -> 4 // 16 pages per signature
        }
        val signaturesCount = ceil(effectiveSheets.toDouble() / sheetsPerSignature).toInt()

        // Area & Multipliers based on real dimensions relative to standard A5 (14.8 x 21 cm = 310.8 cm2)
        val standardA5Area = 14.8 * 21.0
        val bookArea = widthCm * lengthCm
        val sizeMultiplier = (bookArea / standardA5Area).coerceIn(0.6, 2.5)

        // Paper sheet cost
        val pagesPerLargeSheet = if (widthCm > 16.0 || lengthCm > 23.0) 8 else 16
        val pliegosNeeded = (effectivePages.toDouble() / pagesPerLargeSheet).coerceAtLeast(3.0)

        val paperUnitCost = when {
            grammage >= 300 -> 2.80
            grammage >= 200 -> 1.45
            grammage >= 160 -> 0.95
            grammage >= 120 -> 0.65
            grammage >= 100 -> 0.55
            grammage <= 80 -> 0.30
            else -> 0.45 // 90g ahuesado
        }
        val calculatedPaperCost = pliegosNeeded * paperUnitCost

        // Cover material calculation: (Width * 2 + SpineCm + 4cm turn-in) * (Length + 4cm turn-in)
        val spineCm = spineThicknessMm / 10.0
        val coverWidthCm = (widthCm * 2) + spineCm + 4.0
        val coverHeightCm = lengthCm + 4.0
        val coverAreaCm2 = coverWidthCm * coverHeightCm

        val coverCost = when {
            coverMaterial.contains("Cuero", ignoreCase = true) -> 14.50 * sizeMultiplier
            coverMaterial.contains("Lino", ignoreCase = true) -> 7.50 * sizeMultiplier
            coverMaterial.contains("Marmoleado", ignoreCase = true) -> 5.80 * sizeMultiplier
            coverMaterial.contains("Seda", ignoreCase = true) -> 9.00 * sizeMultiplier
            else -> 4.00 * sizeMultiplier // Kraft / Cartulina
        }

        // Technical weight in grams
        val paperWeightGrams = ((widthCm / 100.0) * (lengthCm / 100.0) * effectiveSheets * 2 * grammage * 0.5) +
                (if (isHardcover) 180.0 else 40.0)

        // Extras
        var extrasSum = 0.0
        val requirements = mutableListOf<MaterialRequirement>()

        requirements.add(
            MaterialRequirement(
                materialName = "Papel interior ($paperType, ${grammage}g/m²)",
                quantityNeeded = String.format(Locale.US, "%.1f", pliegosNeeded * quantity).toDouble(),
                unit = "pliegos",
                estimatedCost = calculatedPaperCost * quantity
            )
        )

        requirements.add(
            MaterialRequirement(
                materialName = "Cubierta ($coverMaterial - ${String.format(Locale.US, "%.0f", coverWidthCm)}x${String.format(Locale.US, "%.0f", coverHeightCm)} cm)",
                quantityNeeded = 1.0 * quantity,
                unit = "corte",
                estimatedCost = coverCost * quantity
            )
        )

        if (isHardcover) {
            requirements.add(
                MaterialRequirement(
                    materialName = "Cartón Gris Prensado 2.2mm (2 tapas + lomo)",
                    quantityNeeded = String.format(Locale.US, "%.2f", 0.25 * sizeMultiplier * quantity).toDouble(),
                    unit = "pliego",
                    estimatedCost = 0.90 * sizeMultiplier * quantity
                )
            )
        }

        // Thread requirement
        val threadMeters = (signaturesCount * (lengthCm * 1.5 + 10.0) / 100.0) * quantity
        requirements.add(
            MaterialRequirement(
                materialName = "Hilo de Lino Encerado ($signaturesCount cuadernillos)",
                quantityNeeded = String.format(Locale.US, "%.2f", threadMeters).toDouble(),
                unit = "metros",
                estimatedCost = 0.40 * quantity
            )
        )

        if (hasRibbon) {
            extrasSum += 1.50
            requirements.add(
                MaterialRequirement(
                    materialName = "Cinta Señaladora Raso (${String.format(Locale.US, "%.0f", lengthCm + 12.0)} cm)",
                    quantityNeeded = String.format(Locale.US, "%.2f", (lengthCm + 12.0) / 100.0 * quantity).toDouble(),
                    unit = "metros",
                    estimatedCost = 0.30 * quantity
                )
            )
        }

        if (hasMetalCorners) {
            extrasSum += 3.20
            requirements.add(
                MaterialRequirement(
                    materialName = "Esquineros de Bronce / Latón (4 uds)",
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
                    materialName = "Elástico Plano de Cierre (${String.format(Locale.US, "%.0f", lengthCm * 1.8)} cm)",
                    quantityNeeded = String.format(Locale.US, "%.2f", (lengthCm * 1.8) / 100.0 * quantity).toDouble(),
                    unit = "metros",
                    estimatedCost = 0.40 * quantity
                )
            )
        }

        if (hasMarbledEndpapers) {
            extrasSum += 4.50 * sizeMultiplier
            requirements.add(
                MaterialRequirement(
                    materialName = "Guardas Marmoleadas Florentinas (${String.format(Locale.US, "%.1f", widthCm * 2)}x${String.format(Locale.US, "%.1f", lengthCm)} cm)",
                    quantityNeeded = String.format(Locale.US, "%.2f", 0.5 * sizeMultiplier * quantity).toDouble(),
                    unit = "pliego",
                    estimatedCost = 1.75 * sizeMultiplier * quantity
                )
            )
        }

        if (hasSlipcase) {
            extrasSum += 12.00 * sizeMultiplier
            requirements.add(
                MaterialRequirement(
                    materialName = "Caja Contenedora / Slipcase a medida",
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
                    materialName = "Foil Térmico Metalizado para Estampado",
                    quantityNeeded = 0.2 * quantity,
                    unit = "metros",
                    estimatedCost = 0.60 * quantity
                )
            )
        }

        // Labor calculation based on binding complexity, signatures, and size
        val pageFactor = (effectivePages - 80).coerceAtLeast(0) * 0.04
        val laborCost = (bindingType.basePrice * 0.45 * sizeMultiplier) + pageFactor

        val unitPriceRaw = (bindingType.basePrice * sizeMultiplier) + calculatedPaperCost + (coverCost * 0.6) + extrasSum
        val unitPrice = String.format(Locale.US, "%.2f", unitPriceRaw).toDouble()

        val subtotalRaw = unitPrice * quantity

        // Volume discounts
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
            requiredMaterialsList = requirements,
            spineThicknessMm = spineThicknessMm,
            sheetCount = effectiveSheets,
            pageCount = effectivePages,
            signaturesCount = signaturesCount,
            widthCm = widthCm,
            lengthCm = lengthCm,
            paperWeightGrams = paperWeightGrams,
            coverAreaCm2 = coverAreaCm2
        )
    }
}

