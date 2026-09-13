package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BindingType
import com.example.data.model.SpineType
import com.example.ui.theme.FoilCopper
import com.example.ui.theme.FoilGold
import com.example.ui.theme.FoilSilver
import java.util.Locale

/**
 * Visor 2D del libro artesanal:
 * Muestra el conjunto completo de tapas (contratapa y portada) y lomo con su espesor real,
 * cajetines de bisagra, cantoneras, costuras o nervios y estampados.
 * Permite acercar y alejar mediante gestos táctiles (pellizco/doble tap) y controles dedicados.
 */
@Composable
fun BookCover2DViewer(
    modifier: Modifier = Modifier,
    bindingType: BindingType,
    coverColor: Color = Color(bindingType.defaultColorHex),
    customTextureBitmap: Bitmap? = null,
    foilTitle: String = "",
    foilSubtitle: String = "",
    foilColorType: String = "Dorado",
    hasRibbon: Boolean = bindingType.hasRibbon,
    hasCornerGuards: Boolean = bindingType.hasCornerGuards,
    ribbonColor: Color = Color(0xFFC41E3A),
    widthCm: Float = 14.8f,
    lengthCm: Float = 21.0f,
    spineThicknessMm: Float = 16.0f,
    sheetCount: Int = 60,
    grammageGsm: Int = 90,
    zoomScale: Float = 1.0f,
    onZoomChange: (Float) -> Unit = {}
) {
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Reset pan if zoom drops back to normal
    LaunchedEffect(zoomScale) {
        if (zoomScale <= 1.05f) {
            panOffset = Offset.Zero
        }
    }

    val animatedZoom by animateFloatAsState(
        targetValue = zoomScale,
        animationSpec = tween(durationMillis = 200),
        label = "coverZoom"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFCFDFE),
                        Color(0xFFF1F3F6),
                        Color(0xFFE2E6EC)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newZoom = (zoomScale * zoom).coerceIn(0.7f, 4.0f)
                    onZoomChange(newZoom)
                    if (newZoom > 1.05f) {
                        panOffset += pan
                    } else {
                        panOffset = Offset.Zero
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (zoomScale > 1.15f) {
                            onZoomChange(1.0f)
                            panOffset = Offset.Zero
                        } else {
                            onZoomChange(2.0f)
                        }
                    }
                )
            }
            .testTag("book_2d_viewer_container")
    ) {
        // Lienzo 2D con las tapas y el lomo (completamente limpio, sin textos ni botones superpuestos)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("book_2d_canvas")
        ) {
            val cx = size.width / 2f + panOffset.x
            val cy = size.height / 2f + panOffset.y

            drawBookSpread(
                centerX = cx,
                centerY = cy,
                zoom = animatedZoom,
                bindingType = bindingType,
                coverColor = coverColor,
                customTextureBitmap = customTextureBitmap,
                foilTitle = foilTitle,
                foilSubtitle = foilSubtitle,
                foilColorType = foilColorType,
                hasRibbon = hasRibbon,
                hasCornerGuards = hasCornerGuards,
                ribbonColor = ribbonColor,
                widthCm = widthCm,
                lengthCm = lengthCm,
                spineThicknessMm = spineThicknessMm
            )
        }
    }
}

/**
 * Renderizado de alta fidelidad 2D para el pliego completo del libro:
 * Contratapa (tapa trasera) + Lomo (con espesor milimétrico y curvatura) + Portada (tapa delantera).
 */
private fun DrawScope.drawBookSpread(
    centerX: Float,
    centerY: Float,
    zoom: Float,
    bindingType: BindingType,
    coverColor: Color,
    customTextureBitmap: Bitmap?,
    foilTitle: String,
    foilSubtitle: String,
    foilColorType: String,
    hasRibbon: Boolean,
    hasCornerGuards: Boolean,
    ribbonColor: Color,
    widthCm: Float,
    lengthCm: Float,
    spineThicknessMm: Float
) {
    val spineCm = (spineThicknessMm / 10f).coerceIn(0.5f, 6.0f)
    val hingeCm = 0.35f
    val totalSpreadCm = (widthCm * 2f) + spineCm + (hingeCm * 2f)
    val totalHeightCm = lengthCm.coerceAtLeast(10f)
    val spreadAspect = totalSpreadCm / totalHeightCm

    val availableW = size.width * 0.94f
    val availableH = size.height * 0.90f

    val (fitW, fitH) = if (availableW / availableH > spreadAspect) {
        Pair(availableH * spreadAspect, availableH)
    } else {
        Pair(availableW, availableW / spreadAspect)
    }

    val spreadW = fitW * zoom
    val spreadH = fitH * zoom

    val startX = centerX - spreadW / 2f
    val topY = centerY - spreadH / 2f
    val bottomY = topY + spreadH

    val rawHingeW = (hingeCm / totalSpreadCm) * spreadW
    val hingeW = rawHingeW.coerceAtLeast(6f * zoom)
    val rawSpineW = (spineCm / totalSpreadCm) * spreadW
    val spineW = rawSpineW.coerceAtLeast(24f * zoom)
    val coverW = ((spreadW - spineW - (hingeW * 2f)) / 2f).coerceAtLeast(30f * zoom)

    // Coordenadas horizontales de cada elemento: Contratapa (tapa trasera) + Lomo + Portada (tapa delantera)
    val backLeft = startX
    val backRight = backLeft + coverW

    val leftHingeLeft = backRight
    val leftHingeRight = leftHingeLeft + hingeW

    val spineLeft = leftHingeRight
    val spineRight = spineLeft + spineW

    val rightHingeLeft = spineRight
    val rightHingeRight = rightHingeLeft + hingeW

    val frontLeft = rightHingeRight
    val frontRight = frontLeft + coverW

    val cornerRad = 8f * zoom

    // 1. Sombras suaves de elevación general del libro abierto
    drawRoundRect(
        color = Color(0x16000000),
        topLeft = Offset(startX + 6f * zoom, topY + 10f * zoom),
        size = Size(spreadW, spreadH),
        cornerRadius = CornerRadius(12f * zoom, 12f * zoom)
    )
    drawRoundRect(
        color = Color(0x22000000),
        topLeft = Offset(startX + 3f * zoom, topY + 5f * zoom),
        size = Size(spreadW, spreadH),
        cornerRadius = CornerRadius(8f * zoom, 8f * zoom)
    )

    // 2. Ceja de cartón y cantos de hojas interiores (asomando en los bordes exteriores)
    val pageMargin = 5f * zoom
    // Canto izquierdo (trasera)
    drawRoundRect(
        color = Color(0xFFF7F4EB),
        topLeft = Offset(backLeft - 2f * zoom, topY + pageMargin),
        size = Size(6f * zoom, spreadH - pageMargin * 2),
        cornerRadius = CornerRadius(3f * zoom, 3f * zoom)
    )
    // Canto derecho (delantera)
    drawRoundRect(
        color = Color(0xFFF7F4EB),
        topLeft = Offset(frontRight - 4f * zoom, topY + pageMargin),
        size = Size(6f * zoom, spreadH - pageMargin * 2),
        cornerRadius = CornerRadius(3f * zoom, 3f * zoom)
    )

    // 3. Fondo de material artesanal (Cuero, tela o textura personalizada)
    if (customTextureBitmap != null && !customTextureBitmap.isRecycled) {
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                val shader = BitmapShader(customTextureBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                val matrix = Matrix()
                val scale = (spreadW / customTextureBitmap.width.toFloat()).coerceAtLeast(spreadH / customTextureBitmap.height.toFloat())
                matrix.setScale(scale, scale)
                matrix.postTranslate(startX, topY)
                shader.setLocalMatrix(matrix)
                this.shader = shader
            }
            val nRect = android.graphics.RectF(startX, topY, frontRight, bottomY)
            canvas.nativeCanvas.drawRoundRect(nRect, cornerRad, cornerRad, paint)
        }
    } else {
        // Tapa Trasera (Contratapa)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    coverColor.copy(
                        red = (coverColor.red * 0.90f).coerceIn(0f, 1f),
                        green = (coverColor.green * 0.90f).coerceIn(0f, 1f),
                        blue = (coverColor.blue * 0.90f).coerceIn(0f, 1f)
                    ),
                    coverColor
                ),
                start = Offset(backLeft, topY),
                end = Offset(backRight, bottomY)
            ),
            topLeft = Offset(backLeft, topY),
            size = Size(coverW, spreadH),
            cornerRadius = CornerRadius(cornerRad, cornerRad)
        )

        // Lomo Central Base
        drawRect(
            color = coverColor.copy(
                red = (coverColor.red * 0.95f).coerceIn(0f, 1f),
                green = (coverColor.green * 0.95f).coerceIn(0f, 1f),
                blue = (coverColor.blue * 0.95f).coerceIn(0f, 1f)
            ),
            topLeft = Offset(spineLeft, topY),
            size = Size(spineW, spreadH)
        )

        // Tapa Delantera (Portada)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    coverColor,
                    coverColor.copy(
                        red = (coverColor.red * 0.88f).coerceIn(0f, 1f),
                        green = (coverColor.green * 0.88f).coerceIn(0f, 1f),
                        blue = (coverColor.blue * 0.88f).coerceIn(0f, 1f)
                    )
                ),
                start = Offset(frontLeft, topY),
                end = Offset(frontRight, bottomY)
            ),
            topLeft = Offset(frontLeft, topY),
            size = Size(coverW, spreadH),
            cornerRadius = CornerRadius(cornerRad, cornerRad)
        )
    }

    // 4. Hendiduras / Cajetines de articulación (French Grooves)
    // Hendidura Izquierda
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.35f),
                Color.Black.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.22f)
            ),
            startX = leftHingeLeft,
            endX = leftHingeRight
        ),
        topLeft = Offset(leftHingeLeft, topY),
        size = Size(hingeW, spreadH)
    )
    // Hendidura Derecha
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f),
                Color.Black.copy(alpha = 0.55f),
                Color.Black.copy(alpha = 0.35f)
            ),
            startX = rightHingeLeft,
            endX = rightHingeRight
        ),
        topLeft = Offset(rightHingeLeft, topY),
        size = Size(hingeW, spreadH)
    )

    // 5. Lomo: Curvatura 3D, Cabezadas y Nervios/Costuras
    // Curvatura cilíndrica del lomo (sombra lateral y resalte central)
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.22f),
                Color.Black.copy(alpha = 0.32f)
            ),
            startX = spineLeft,
            endX = spineRight
        ),
        topLeft = Offset(spineLeft, topY),
        size = Size(spineW, spreadH)
    )

    // Cabezadas artesanales (Headbands) asomando en cabeza y pie del lomo
    val headbandH = 3.5f * zoom
    // Cabezada superior
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF8B0000), Color(0xFFD4AF37), Color(0xFF8B0000), Color(0xFFD4AF37)),
            startX = spineLeft,
            endX = spineRight
        ),
        topLeft = Offset(spineLeft + 1f * zoom, topY - headbandH),
        size = Size(spineW - 2f * zoom, headbandH)
    )
    // Cabezada inferior
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF8B0000), Color(0xFFD4AF37), Color(0xFF8B0000), Color(0xFFD4AF37)),
            startX = spineLeft,
            endX = spineRight
        ),
        topLeft = Offset(spineLeft + 1f * zoom, bottomY),
        size = Size(spineW - 2f * zoom, headbandH)
    )

    // Nervios en relieve o Costuras según SpineType
    val isExposedStitch = bindingType.spineType == SpineType.EXPOSED_COPTIC ||
        bindingType.spineType == SpineType.EXPOSED_BELGIAN ||
        bindingType.spineType == SpineType.OPEN_SPINE ||
        bindingType.spineType == SpineType.JAPANESE_EXTERNAL ||
        bindingType.spineType == SpineType.JAPANESE_INTERNAL ||
        bindingType.name.contains("Copta", ignoreCase = true) ||
        bindingType.name.contains("Japon", ignoreCase = true)

    if (isExposedStitch) {
        // Costura artesanal a la vista en el lomo
        val stitchCount = 6
        val stitchGap = spreadH / (stitchCount + 1)
        val threadColor = Color(0xFFF9E7BA)
        val spineMidX = spineLeft + spineW / 2f

        for (i in 1..stitchCount) {
            val sy = topY + i * stitchGap
            // Agujeros de punzón
            drawCircle(
                color = Color.Black.copy(alpha = 0.70f),
                radius = 2.8f * zoom,
                center = Offset(spineLeft + 3f * zoom, sy)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.70f),
                radius = 2.8f * zoom,
                center = Offset(spineRight - 3f * zoom, sy)
            )
            // Lazada de costura
            drawLine(
                color = threadColor,
                start = Offset(spineLeft + 3f * zoom, sy),
                end = Offset(spineRight - 3f * zoom, sy),
                strokeWidth = 2.2f * zoom,
                cap = StrokeCap.Round
            )
            // Detalle de nudo central
            drawCircle(
                color = Color(0xFFD8C078),
                radius = 2f * zoom,
                center = Offset(spineMidX, sy)
            )
        }
    } else {
        // Nervios horizontales en relieve clásicos (Raised Bands)
        val ribCount = 5
        val ribGap = spreadH / (ribCount + 1)
        for (i in 1..ribCount) {
            val ry = topY + i * ribGap
            // Sombra bajo el nervio
            drawLine(
                color = Color.Black.copy(alpha = 0.45f),
                start = Offset(spineLeft, ry + 1.2f * zoom),
                end = Offset(spineRight, ry + 1.2f * zoom),
                strokeWidth = 2.2f * zoom
            )
            // Resalte de luz sobre el nervio
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(spineLeft, ry - 1.2f * zoom),
                end = Offset(spineRight, ry - 1.2f * zoom),
                strokeWidth = 1.4f * zoom
            )
        }

        // Título o detalle ornamental en el lomo si tiene ancho suficiente
        if (spineW >= 22f * zoom) {
            drawIntoCanvas { canvas ->
                val spinePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = getFoilColors(foilColorType).first().toArgb()
                    textSize = (spineW * 0.45f).coerceIn(8f * zoom, 14f * zoom)
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val spineMidX = spineLeft + spineW / 2f
                val spineMidY = topY + spreadH * 0.48f

                canvas.nativeCanvas.save()
                canvas.nativeCanvas.rotate(90f, spineMidX, spineMidY)
                val spineText = if (foilTitle.isNotBlank()) foilTitle else bindingType.name
                canvas.nativeCanvas.drawText(spineText, spineMidX, spineMidY + spinePaint.textSize / 3f, spinePaint)
                canvas.nativeCanvas.restore()
            }
        }
    }

    // 6. Contratapa (Tapa Trasera): Sello artesanal de taller ("Ex Libris")
    val foilColors = getFoilColors(foilColorType)
    val backCenterX = backLeft + coverW / 2f
    val backCenterY = topY + spreadH * 0.52f

    // Sello ornamental circular grabado
    drawCircle(
        brush = Brush.linearGradient(foilColors),
        radius = 16f * zoom,
        center = Offset(backCenterX, backCenterY),
        style = Stroke(width = 1.2f * zoom)
    )
    drawCircle(
        brush = Brush.linearGradient(foilColors.map { it.copy(alpha = 0.5f) }),
        radius = 12f * zoom,
        center = Offset(backCenterX, backCenterY),
        style = Stroke(width = 0.8f * zoom)
    )
    drawIntoCanvas { canvas ->
        val sealPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = foilColors.first().toArgb()
            textSize = (7.5f * zoom).coerceIn(6f, 13f)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.nativeCanvas.drawText("TALLER", backCenterX, backCenterY - 1f * zoom, sealPaint)
        sealPaint.textSize = (6.5f * zoom).coerceIn(5f, 11f)
        canvas.nativeCanvas.drawText("ARTESANAL", backCenterX, backCenterY + 7f * zoom, sealPaint)
    }

    // 7. Tapa Delantera (Portada): Marco ornamental perimetral y título
    val borderInset = 12f * zoom
    val borderLeft = frontLeft + 10f * zoom
    val borderTop = topY + borderInset
    val borderRight = frontRight - borderInset
    val borderBottom = bottomY - borderInset

    if (borderRight > borderLeft + 20f && borderBottom > borderTop + 20f) {
        // Filete perimetral exterior
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = foilColors,
                start = Offset(borderLeft, borderTop),
                end = Offset(borderRight, borderBottom)
            ),
            topLeft = Offset(borderLeft, borderTop),
            size = Size(borderRight - borderLeft, borderBottom - borderTop),
            cornerRadius = CornerRadius(3f * zoom, 3f * zoom),
            style = Stroke(width = 1.5f * zoom)
        )

        // Filete interior fino
        val innerMargin = 5f * zoom
        if (borderRight - innerMargin > borderLeft + innerMargin + 10f) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = foilColors.map { it.copy(alpha = 0.65f) },
                    start = Offset(borderLeft, borderTop),
                    end = Offset(borderRight, borderBottom)
                ),
                topLeft = Offset(borderLeft + innerMargin, borderTop + innerMargin),
                size = Size(
                    borderRight - borderLeft - innerMargin * 2,
                    borderBottom - borderTop - innerMargin * 2
                ),
                cornerRadius = CornerRadius(2f * zoom, 2f * zoom),
                style = Stroke(width = 0.8f * zoom)
            )
        }

        // Florones ornamentales en esquinas del marco
        val floronSize = 4f * zoom
        drawCircle(brush = Brush.radialGradient(foilColors), radius = floronSize, center = Offset(borderLeft + 6f * zoom, borderTop + 6f * zoom))
        drawCircle(brush = Brush.radialGradient(foilColors), radius = floronSize, center = Offset(borderRight - 6f * zoom, borderTop + 6f * zoom))
        drawCircle(brush = Brush.radialGradient(foilColors), radius = floronSize, center = Offset(borderLeft + 6f * zoom, borderBottom - 6f * zoom))
        drawCircle(brush = Brush.radialGradient(foilColors), radius = floronSize, center = Offset(borderRight - 6f * zoom, borderBottom - 6f * zoom))

        // Título y subtítulo en Hot Stamping sobre la portada
        val frontCenterX = frontLeft + coverW / 2f
        val displayTitle = if (foilTitle.isNotBlank()) foilTitle else bindingType.name
        val displaySub = if (foilSubtitle.isNotBlank()) foilSubtitle else bindingType.category

        drawIntoCanvas { canvas ->
            val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = foilColors.first().toArgb()
                textSize = (13f * zoom).coerceIn(9f, 26f)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                letterSpacing = 0.06f
                if (foilColorType != "Golpe Seco") {
                    setShadowLayer(2f * zoom, 1f, 1f, Color.Black.copy(alpha = 0.35f).toArgb())
                }
            }
            val textCenterY = topY + spreadH * 0.44f
            canvas.nativeCanvas.drawText(displayTitle, frontCenterX, textCenterY, titlePaint)

            val subPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = foilColors.last().toArgb()
                textSize = (8.5f * zoom).coerceIn(6f, 16f)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
                textAlign = android.graphics.Paint.Align.CENTER
                letterSpacing = 0.10f
            }
            canvas.nativeCanvas.drawText(displaySub.uppercase(), frontCenterX, textCenterY + 16f * zoom, subPaint)
        }
    }

    // 8. Cinta Señaladora (Ribbon) saliendo desde la cabeza del lomo
    if (hasRibbon) {
        val ribbonX = spineLeft + spineW * 0.4f
        val ribbonW = 9f * zoom
        val ribbonDrop = 32f * zoom

        val ribbonPath = Path().apply {
            moveTo(ribbonX, topY)
            lineTo(ribbonX + 8f * zoom, bottomY + ribbonDrop)
            lineTo(ribbonX + 8f * zoom + ribbonW / 2f, bottomY + ribbonDrop - 6f * zoom)
            lineTo(ribbonX + 8f * zoom + ribbonW, bottomY + ribbonDrop)
            lineTo(ribbonX + ribbonW, topY)
            close()
        }
        drawPath(
            path = ribbonPath,
            color = ribbonColor
        )
        // Brillo satinado
        drawLine(
            color = Color.White.copy(alpha = 0.40f),
            start = Offset(ribbonX + 2f * zoom, topY),
            end = Offset(ribbonX + 8f * zoom + 3f * zoom, bottomY + ribbonDrop - 6f * zoom),
            strokeWidth = 1.6f * zoom
        )
    }

    // 9. Cantoneras Metálicas de Protección (Corner guards en esquinas exteriores)
    if (hasCornerGuards) {
        val cornerSize = 18f * zoom
        val brassGold = Color(0xFFD4AF37)
        val brassDark = Color(0xFF8C6D1F)

        // Cantoneras en Contratapa (esquinas exteriores izquierdas)
        drawCornerGuard(backLeft, topY, cornerSize, isTop = true, isLeft = true, brassGold, brassDark)
        drawCornerGuard(backLeft, bottomY, cornerSize, isTop = false, isLeft = true, brassGold, brassDark)

        // Cantoneras en Portada (esquinas exteriores derechas)
        drawCornerGuard(frontRight, topY, cornerSize, isTop = true, isLeft = false, brassGold, brassDark)
        drawCornerGuard(frontRight, bottomY, cornerSize, isTop = false, isLeft = false, brassGold, brassDark)
    }
}

/**
 * Dibuja una cantonera metálica decorativa en una de las esquinas.
 */
private fun DrawScope.drawCornerGuard(
    cornerX: Float,
    cornerY: Float,
    size: Float,
    isTop: Boolean,
    isLeft: Boolean,
    gold: Color,
    darkGold: Color
) {
    val dirX = if (isLeft) 1f else -1f
    val dirY = if (isTop) 1f else -1f

    val path = Path().apply {
        moveTo(cornerX, cornerY)
        lineTo(cornerX + size * dirX, cornerY)
        lineTo(cornerX, cornerY + size * dirY)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(gold, darkGold, gold),
            start = Offset(cornerX, cornerY),
            end = Offset(cornerX + size * dirX, cornerY + size * dirY)
        )
    )

    // Remache metálico en la cantonera
    drawCircle(
        color = Color(0xFF5C4710),
        radius = 2.2f,
        center = Offset(cornerX + (size * 0.35f) * dirX, cornerY + (size * 0.35f) * dirY)
    )
}

/**
 * Paleta de colores para Hot Stamping metálico.
 */
private fun getFoilColors(foilColorType: String): List<Color> {
    return when (foilColorType) {
        "Dorado" -> listOf(Color(0xFFFFE57F), FoilGold, Color(0xFF996515))
        "Plateado" -> listOf(Color(0xFFFFFFFF), FoilSilver, Color(0xFF7A808C))
        "Cobre" -> listOf(Color(0xFFFFC5A8), FoilCopper, Color(0xFF703215))
        "Golpe Seco" -> listOf(Color.Black.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.3f))
        else -> listOf(Color(0xFFFFE57F), FoilGold, Color(0xFF996515))
    }
}
