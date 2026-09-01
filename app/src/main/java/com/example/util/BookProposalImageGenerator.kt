package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.BindingType
import com.example.data.model.QuoteResult
import com.example.data.model.SpineType
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProposalExportSpec(
    val bindingType: BindingType,
    val widthCm: Float,
    val lengthCm: Float,
    val spineThicknessMm: Float,
    val sheetCount: Int,
    val pageCount: Int,
    val paperType: String,
    val coverMaterial: String,
    val coverColorHex: Long,
    val foilTitle: String,
    val foilSubtitle: String,
    val foilColorType: String,
    val hasRibbon: Boolean,
    val hasCorners: Boolean,
    val hasSlipcase: Boolean,
    val hasEndpapers: Boolean,
    val clientName: String,
    val clientNotes: String,
    val quoteResult: QuoteResult,
    val estimatedDays: Int = 4
)

object BookProposalImageGenerator {

    /**
     * Generates a luxury, high-impact 1200x1900 editorial sales proposal bitmap card.
     */
    fun generateProposalBitmap(context: Context, spec: ProposalExportSpec): Bitmap {
        val width = 1200
        val height = 1900
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. BACKGROUND WITH GRADIENT & TEXTURED BORDER
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(
                    Color.rgb(20, 24, 28),     // Deep Obsidian Slate
                    Color.rgb(15, 18, 22),     // Dark Charcoal
                    Color.rgb(28, 22, 18)      // Deep Warm Espresso
                ),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Luxury Gold Double Border
        val borderGoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.rgb(212, 175, 55) // Rich Gold
            strokeWidth = 3f
        }
        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(90, 212, 175, 55)
            strokeWidth = 1f
        }
        canvas.drawRoundRect(RectF(30f, 30f, width - 30f, height - 30f), 24f, 24f, borderGoldPaint)
        canvas.drawRoundRect(RectF(40f, 40f, width - 40f, height - 40f), 18f, 18f, innerBorderPaint)

        // Corner Ornaments (Golden Filigrees)
        drawCornerOrnaments(canvas, width, height)

        // 2. HEADER SECTION: WORKSHOP BRANDING & TITLE
        var curY = 85f
        val headerTagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 180, 80)
            textSize = 22f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
        }
        canvas.drawText("✦ TALLER DE ENCUADERNACIÓN ARTESANAL & DISEÑO A MEDIDA ✦", width / 2f, curY, headerTagPaint)

        curY += 50f
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 4f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText("PROPUESTA DE CONFECCIÓN & MODELO 3D", width / 2f, curY, titlePaint)

        curY += 34f
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val refCode = "ENC-${(Math.abs(spec.bindingType.id.hashCode()) % 9000) + 1000}"
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 190, 200)
            textSize = 20f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }
        val clientTag = if (spec.clientName.isNotBlank()) " • Cliente: ${spec.clientName}" else ""
        canvas.drawText("Emisión: $dateFormatted • Ref: $refCode$clientTag", width / 2f, curY, subHeaderPaint)

        // 3. HERO 3D BOOK MOCKUP PRESENTATION BOX
        curY += 30f
        val mockupCardRect = RectF(65f, curY, width - 65f, curY + 540f)
        val mockupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                mockupCardRect.left, mockupCardRect.top,
                mockupCardRect.left, mockupCardRect.bottom,
                intArrayOf(Color.rgb(32, 38, 46), Color.rgb(22, 26, 32)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(mockupCardRect, 20f, 20f, mockupBgPaint)

        val mockupBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(120, 212, 175, 55)
            strokeWidth = 2f
        }
        canvas.drawRoundRect(mockupCardRect, 20f, 20f, mockupBorderPaint)

        // Render the Custom Book Mockup Graphic inside
        drawArtisanBookMockup(canvas, mockupCardRect, spec)

        // Mockup Footer Badge Overlay
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(220, 15, 18, 22)
        }
        val badgeRect = RectF(mockupCardRect.left + 20f, mockupCardRect.bottom - 46f, mockupCardRect.right - 20f, mockupCardRect.bottom - 10f)
        canvas.drawRoundRect(badgeRect, 10f, 10f, badgePaint)

        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(230, 200, 110)
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "📐 ${spec.widthCm}x${spec.lengthCm} cm • Lomo ${String.format(Locale.US, "%.1f", spec.spineThicknessMm)} mm • ${spec.sheetCount} Hojas (${spec.pageCount} Págs) • Apertura Plana 180°",
            width / 2f,
            badgeRect.centerY() + 6f,
            badgeTextPaint
        )

        // 4. SPECIFICATION GRID (4 CARDS: Estructura, Dimensiones, Materiales, Acabados)
        curY = mockupCardRect.bottom + 25f
        val specSectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(212, 175, 55)
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText("✦ ESPECIFICACIONES TÉCNICAS DE MANOFACTURA", 70f, curY + 10f, specSectionTitlePaint)

        curY += 25f
        val cardMargin = 65f
        val cardGap = 16f
        val cardW = (width - (cardMargin * 2) - cardGap) / 2f
        val cardH = 150f

        // Grid 2x2: Row 1
        drawSpecCard(
            canvas = canvas,
            rect = RectF(cardMargin, curY, cardMargin + cardW, curY + cardH),
            icon = "📖",
            title = "ESTILO & COSTURA",
            primaryValue = spec.bindingType.name,
            secondaryValue = "${spec.bindingType.category} • Costura Artesanal"
        )

        drawSpecCard(
            canvas = canvas,
            rect = RectF(cardMargin + cardW + cardGap, curY, width - cardMargin, curY + cardH),
            icon = "📐",
            title = "FORMATO & LOMO",
            primaryValue = "${spec.widthCm} × ${spec.lengthCm} cm",
            secondaryValue = "Lomo: ${String.format(Locale.US, "%.1f", spec.spineThicknessMm)} mm • ${spec.sheetCount} Hojas (${spec.pageCount} págs)"
        )

        // Grid Row 2
        curY += cardH + cardGap
        drawSpecCard(
            canvas = canvas,
            rect = RectF(cardMargin, curY, cardMargin + cardW, curY + cardH),
            icon = "📜",
            title = "PAPEL INTERIOR",
            primaryValue = spec.paperType,
            secondaryValue = "Cosido a mano en cuadernillos con hilo encerado"
        )

        val finishesDesc = buildString {
            append("Hot Stamping ")
            append(spec.foilColorType)
            if (spec.hasCorners) append(" • Cantoneras")
            if (spec.hasRibbon) append(" • Cinta de Seda")
            if (spec.hasSlipcase) append(" • Estuche")
        }

        drawSpecCard(
            canvas = canvas,
            rect = RectF(cardMargin + cardW + cardGap, curY, width - cardMargin, curY + cardH),
            icon = "✨",
            title = "CUBIERTA & ACABADOS",
            primaryValue = spec.coverMaterial,
            secondaryValue = finishesDesc
        )

        // 5. COMMERCIAL & PRICING BREAKDOWN BOX (HIGH IMPACT SALES CARD)
        curY += cardH + 25f
        val priceCardRect = RectF(65f, curY, width - 65f, curY + 230f)

        val priceBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                priceCardRect.left, priceCardRect.top,
                priceCardRect.right, priceCardRect.bottom,
                intArrayOf(Color.rgb(38, 30, 24), Color.rgb(24, 20, 18), Color.rgb(40, 32, 22)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(priceCardRect, 22f, 22f, priceBgPaint)

        val priceBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.rgb(212, 175, 55)
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(priceCardRect, 22f, 22f, priceBorderPaint)

        // Inside Commercial Box: Left details
        val leftX = priceCardRect.left + 30f
        var priceY = priceCardRect.top + 45f

        val priceLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 185, 90)
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("PRESUPUESTO ESTIMADO DE PRODUCCIÓN", leftX, priceY, priceLabelPaint)

        priceY += 36f
        val priceSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(210, 215, 220)
            textSize = 19f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText("• Cantidad: ${spec.quoteResult.quantity} pieza(s) artesanal(es)", leftX, priceY, priceSubPaint)
        priceY += 30f
        canvas.drawText("• Valor Unitario: $${String.format(Locale.US, "%.2f", spec.quoteResult.unitPrice)}", leftX, priceY, priceSubPaint)
        priceY += 30f
        val discountText = if (spec.quoteResult.totalDiscountAmount > 0) {
            "• Descuento aplicado: -$${String.format(Locale.US, "%.2f", spec.quoteResult.totalDiscountAmount)}"
        } else {
            "• Tiempo estimado de taller: ${spec.estimatedDays} a 6 días hábiles"
        }
        canvas.drawText(discountText, leftX, priceY, priceSubPaint)
        priceY += 30f
        canvas.drawText("• Incluye materiales premium y mano de obra de taller", leftX, priceY, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(160, 170, 180)
            textSize = 16f
        })

        // Inside Commercial Box: Right Big Total
        val rightCenterX = priceCardRect.right - 220f
        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(190, 195, 205)
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.1f
        }
        canvas.drawText("TOTAL ESTIMADO", rightCenterX, priceCardRect.top + 55f, totalLabelPaint)

        val totalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 215, 0) // Radiant Gold
            textSize = 54f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(10f, 0f, 4f, Color.argb(160, 0, 0, 0))
        }
        canvas.drawText("$${String.format(Locale.US, "%.2f", spec.quoteResult.total)}", rightCenterX, priceCardRect.top + 115f, totalValuePaint)

        // Deposit tag
        val depositRect = RectF(rightCenterX - 180f, priceCardRect.top + 140f, rightCenterX + 180f, priceCardRect.top + 185f)
        val depositBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 212, 175, 55)
        }
        canvas.drawRoundRect(depositRect, 10f, 10f, depositBg)

        val depositTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 20, 20)
            textSize = 17f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val deposit50 = spec.quoteResult.total * 0.50
        canvas.drawText("Anticipo Sugerido (50%): $${String.format(Locale.US, "%.2f", deposit50)}", rightCenterX, depositRect.centerY() + 6f, depositTextPaint)

        // 6. FOOTER: CRAFTSMANSHIP GUARANTEE & CALL TO ACTION
        curY = priceCardRect.bottom + 35f
        val footerGuaranteePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(230, 200, 120)
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✓ Pieza Única de Manufactura Artesanal • 100% Cosida y Ensamblada a Mano", width / 2f, curY, footerGuaranteePaint)

        curY += 28f
        val footerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(160, 170, 180)
            textSize = 16f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Esta propuesta digital contiene la configuración técnica para iniciar la confección en taller.", width / 2f, curY, footerSubPaint)

        curY += 24f
        val contactPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(130, 140, 150)
            textSize = 15f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Taller de Encuadernación y Restauración • Presupuesto válido por 15 días", width / 2f, curY, contactPaint)

        return bitmap
    }

    /**
     * Draws an eye-catching isometric 3D book mockup on the proposal canvas.
     */
    private fun drawArtisanBookMockup(canvas: Canvas, container: RectF, spec: ProposalExportSpec) {
        val cx = container.centerX()
        val cy = container.centerY() - 10f

        val baseColor = spec.coverColorHex.toInt()
        val bookW = 340f
        val bookH = 430f
        val spineDepth = 55f

        // Isometric offset coordinates
        val isoX = 40f
        val isoY = -25f

        // 1. Soft Drop Shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 0, 0, 0)
        }
        val shadowPath = Path().apply {
            moveTo(cx - (bookW / 2) + 20f, cy + (bookH / 2) + 20f)
            lineTo(cx + (bookW / 2) + isoX + 30f, cy + (bookH / 2) + isoY + 30f)
            lineTo(cx + (bookW / 2) + isoX + 20f, cy + (bookH / 2) + isoY + 50f)
            lineTo(cx - (bookW / 2) - spineDepth + 10f, cy + (bookH / 2) + 40f)
            close()
        }
        canvas.drawPath(shadowPath, shadowPaint)

        // 2. Paper Block (Bottom & Side Edge)
        val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(246, 238, 222) // Ahuesado ivory book edge
        }
        val paperSidePath = Path().apply {
            moveTo(cx + (bookW / 2), cy - (bookH / 2))
            lineTo(cx + (bookW / 2) + isoX, cy - (bookH / 2) + isoY)
            lineTo(cx + (bookW / 2) + isoX, cy + (bookH / 2) + isoY - 10f)
            lineTo(cx + (bookW / 2), cy + (bookH / 2) - 10f)
            close()
        }
        canvas.drawPath(paperSidePath, paperPaint)

        // Paper Edge Lines (Page texture effect)
        val pageLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 140, 110, 80)
            strokeWidth = 1.5f
        }
        for (i in 1..10) {
            val step = i * 40f
            if (step < bookH) {
                canvas.drawLine(
                    cx + (bookW / 2), cy - (bookH / 2) + step,
                    cx + (bookW / 2) + isoX, cy - (bookH / 2) + isoY + step,
                    pageLinePaint
                )
            }
        }

        // 3. Spine (Left Panel)
        val spinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx - (bookW / 2) - spineDepth, cy,
                cx - (bookW / 2), cy,
                intArrayOf(darkenColor(baseColor, 0.65f), baseColor),
                null,
                Shader.TileMode.CLAMP
            )
        }
        val spinePath = Path().apply {
            moveTo(cx - (bookW / 2) - spineDepth, cy - (bookH / 2) + 15f)
            lineTo(cx - (bookW / 2), cy - (bookH / 2))
            lineTo(cx - (bookW / 2), cy + (bookH / 2))
            lineTo(cx - (bookW / 2) - spineDepth, cy + (bookH / 2) + 15f)
            close()
        }
        canvas.drawPath(spinePath, spinePaint)

        // Spine ribs / stitches depending on spine type
        val isStitched = spec.bindingType.spineType == SpineType.EXPOSED_COPTIC ||
                spec.bindingType.spineType == SpineType.FRENCH_EXTERNAL ||
                spec.bindingType.spineType == SpineType.JAPANESE_EXTERNAL

        if (isStitched) {
            val stitchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(240, 230, 190) // Waxed linen thread
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            for (k in 1..6) {
                val sy = cy - (bookH / 2) + (k * 60f)
                canvas.drawLine(cx - (bookW / 2) - spineDepth + 5f, sy + 15f, cx - (bookW / 2) - 3f, sy, stitchPaint)
                canvas.drawLine(cx - (bookW / 2) - spineDepth + 15f, sy + 10f, cx - (bookW / 2) - 10f, sy + 5f, stitchPaint)
            }
        } else {
            // Raised cords / Nervios
            val ribPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(120, 255, 255, 255)
                strokeWidth = 3f
            }
            for (k in 1..4) {
                val ry = cy - (bookH / 2) + (k * 85f)
                canvas.drawLine(cx - (bookW / 2) - spineDepth + 5f, ry + 15f, cx - (bookW / 2), ry, ribPaint)
            }
        }

        // 4. Front Cover (Face)
        val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx - (bookW / 2), cy - (bookH / 2),
                cx + (bookW / 2), cy + (bookH / 2),
                intArrayOf(lightenColor(baseColor, 1.15f), baseColor, darkenColor(baseColor, 0.85f)),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        val coverRect = RectF(cx - (bookW / 2), cy - (bookH / 2), cx + (bookW / 2), cy + (bookH / 2))
        canvas.drawRoundRect(coverRect, 8f, 8f, coverPaint)

        // Embossed Cover Frame
        val coverFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(90, 255, 255, 255)
            strokeWidth = 2f
        }
        canvas.drawRoundRect(
            RectF(coverRect.left + 16f, coverRect.top + 16f, coverRect.right - 16f, coverRect.bottom - 16f),
            6f, 6f, coverFramePaint
        )

        // 5. Hot Stamping Foil Engraving (Title & Subtitle)
        val foilColor = when (spec.foilColorType.lowercase()) {
            "plata" -> Color.rgb(220, 230, 242)
            "cobre", "bronce" -> Color.rgb(205, 127, 50)
            "holográfico", "oro rosa" -> Color.rgb(220, 160, 180)
            else -> Color.rgb(245, 205, 75) // Golden Foil
        }

        val foilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foilColor
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, Color.argb(190, 0, 0, 0))
        }

        val titleText = if (spec.foilTitle.isNotBlank()) spec.foilTitle else spec.bindingType.name
        canvas.drawText(titleText, coverRect.centerX(), coverRect.centerY() - 20f, foilPaint)

        if (spec.foilSubtitle.isNotBlank()) {
            val foilSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = foilColor
                textSize = 17f
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                setShadowLayer(3f, 0f, 1f, Color.argb(160, 0, 0, 0))
            }
            canvas.drawText(spec.foilSubtitle, coverRect.centerX(), coverRect.centerY() + 18f, foilSubPaint)
        }

        // Small decorative foil star
        canvas.drawText("✦", coverRect.centerX(), coverRect.centerY() + 55f, foilPaint)

        // 6. Silk Ribbon Bookmark (if enabled)
        if (spec.hasRibbon) {
            val ribbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(196, 30, 58) // Wine Ruby silk ribbon
                style = Paint.Style.STROKE
                strokeWidth = 14f
                strokeCap = Paint.Cap.ROUND
            }
            val ribbonPath = Path().apply {
                moveTo(coverRect.centerX() + 30f, coverRect.top - 10f)
                cubicTo(
                    coverRect.centerX() + 60f, coverRect.centerY(),
                    coverRect.centerX() + 20f, coverRect.bottom + 20f,
                    coverRect.centerX() + 50f, coverRect.bottom + 55f
                )
            }
            canvas.drawPath(ribbonPath, ribbonPaint)
        }

        // 7. Metal Corner Protectors (if enabled)
        if (spec.hasCorners) {
            val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(218, 165, 32) // Golden Brass corner
            }
            // Top right corner
            val trCorner = Path().apply {
                moveTo(coverRect.right - 35f, coverRect.top)
                lineTo(coverRect.right, coverRect.top)
                lineTo(coverRect.right, coverRect.top + 35f)
                close()
            }
            canvas.drawPath(trCorner, cornerPaint)

            // Bottom right corner
            val brCorner = Path().apply {
                moveTo(coverRect.right - 35f, coverRect.bottom)
                lineTo(coverRect.right, coverRect.bottom)
                lineTo(coverRect.right, coverRect.bottom - 35f)
                close()
            }
            canvas.drawPath(brCorner, cornerPaint)
        }
    }

    private fun drawSpecCard(canvas: Canvas, rect: RectF, icon: String, title: String, primaryValue: String, secondaryValue: String) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(28, 34, 40)
        }
        canvas.drawRoundRect(rect, 14f, 14f, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(60, 212, 175, 55)
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(rect, 14f, 14f, borderPaint)

        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 28f
        }
        canvas.drawText(icon, rect.left + 16f, rect.top + 42f, iconPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(212, 175, 55)
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText(title, rect.left + 54f, rect.top + 38f, titlePaint)

        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val clippedVal = if (primaryValue.length > 28) primaryValue.take(26) + "…" else primaryValue
        canvas.drawText(clippedVal, rect.left + 18f, rect.top + 80f, valPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(165, 175, 185)
            textSize = 14f
            typeface = Typeface.DEFAULT
        }
        val clippedSub = if (secondaryValue.length > 40) secondaryValue.take(38) + "…" else secondaryValue
        canvas.drawText(clippedSub, rect.left + 18f, rect.top + 115f, subPaint)
    }

    private fun drawCornerOrnaments(canvas: Canvas, w: Int, h: Int) {
        val ornamentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(212, 175, 55)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val size = 28f
        // Top Left
        canvas.drawLine(35f, 35f, 35f + size, 35f, ornamentPaint)
        canvas.drawLine(35f, 35f, 35f, 35f + size, ornamentPaint)

        // Top Right
        canvas.drawLine(w - 35f, 35f, w - 35f - size, 35f, ornamentPaint)
        canvas.drawLine(w - 35f, 35f, w - 35f, 35f + size, ornamentPaint)

        // Bottom Left
        canvas.drawLine(35f, h - 35f, 35f + size, h - 35f, ornamentPaint)
        canvas.drawLine(35f, h - 35f, 35f, h - 35f - size, ornamentPaint)

        // Bottom Right
        canvas.drawLine(w - 35f, h - 35f, w - 35f - size, h - 35f, ornamentPaint)
        canvas.drawLine(w - 35f, h - 35f, w - 35f, h - 35f - size, ornamentPaint)
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    private fun lightenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    /**
     * Saves the proposal bitmap to the app cache directory and returns a content Uri via FileProvider.
     */
    fun saveProposalImageToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cacheFolder = File(context.cacheDir, "shared_images")
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs()
            }
            val imageFile = File(cacheFolder, "propuesta_encuadernacion_${System.currentTimeMillis()}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves the proposal bitmap into the Android device's Pictures / Gallery.
     */
    fun saveToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val filename = "Propuesta_Encuadernacion_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.png"
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Encuadernacion")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "Encuadernacion")
                if (!appDir.exists()) appDir.mkdirs()
                val image = File(appDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            Toast.makeText(context, "Imagen guardada en Galería", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al guardar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Shares the image URI and formatted commercial message via Android Send Intent.
     */
    fun shareProposal(context: Context, imageUri: Uri, spec: ProposalExportSpec) {
        val shareMessage = buildString {
            append("📚 *PROPUESTA DE ENCUADERNACIÓN ARTESANAL*\n\n")
            append("• *Modelo:* ${spec.bindingType.name}\n")
            append("• *Dimensiones:* ${spec.widthCm} x ${spec.lengthCm} cm (Lomo: ${String.format(Locale.US, "%.1f", spec.spineThicknessMm)} mm)\n")
            append("• *Contenido:* ${spec.sheetCount} hojas (${spec.pageCount} páginas) en ${spec.paperType}\n")
            append("• *Cubiertas:* ${spec.coverMaterial} con grabado Hot Stamping ${spec.foilColorType}\n")
            if (spec.foilTitle.isNotBlank()) append("• *Título personalizado:* \"${spec.foilTitle}\"\n")
            append("• *Cantidad:* ${spec.quoteResult.quantity} unidad(es)\n")
            append("• *Presupuesto Total:* $${String.format(Locale.US, "%.2f", spec.quoteResult.total)}\n")
            append("• *Anticipo 50%:* $${String.format(Locale.US, "%.2f", spec.quoteResult.total * 0.5)}\n")
            append("• *Tiempo estimado:* ${spec.estimatedDays} a 6 días hábiles de confección a mano.\n\n")
            append("✨ _Se adjunta ficha técnica y modelo 3D renderizado para su evaluación._")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            putExtra(Intent.EXTRA_SUBJECT, "Propuesta de Encuadernación Artesanal - ${spec.bindingType.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir propuesta con el cliente")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
