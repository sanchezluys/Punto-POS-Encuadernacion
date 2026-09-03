package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BindingType
import com.example.data.model.SpineType
import com.example.ui.theme.FoilGold
import com.example.ui.theme.FoilSilver
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.SaddleBrown
import com.example.ui.theme.Terracotta

val SpineType.isExposed: Boolean
    get() = when (this) {
        SpineType.EXPOSED_COPTIC,
        SpineType.EXPOSED_BELGIAN,
        SpineType.OPEN_SPINE,
        SpineType.FRENCH_EXTERNAL,
        SpineType.JAPANESE_EXTERNAL -> true
        else -> false
    }

@Composable
fun Book2DDetailViewer(
    bindingType: BindingType,
    coverColor: Color,
    customTextureBitmap: Bitmap? = null,
    foilTitle: String = "",
    foilSubtitle: String = "",
    foilColorType: String = "Dorado",
    hasRibbon: Boolean = true,
    hasCornerGuards: Boolean = true,
    widthCm: Float = 14.8f,
    lengthCm: Float = 21.0f,
    spineThicknessMm: Float = 14.0f,
    sheetCount: Int = 80,
    grammageGsm: Int = 90,
    estimatedSignatures: Int = 15,
    sheetsPerSignature: Int = 4,
    onOpen3DSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Despiece Desplegado, 1: Sección Transversal
    val isExposedSpine = bindingType.spineType.isExposed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("book_2d_detail_card"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Bar: Binding info & 3D Simulation Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bindingType.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${bindingType.category} • Vista Técnica 2D",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 3D Simulation Launcher Button (Prominent)
                Button(
                    onClick = onOpen3DSimulation,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenOchre,
                        contentColor = Color.Black
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_launch_3d_fullscreen")
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = "Simulación 3D",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simular 3D", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spine Exposure Status Chip & Explanation Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isExposedSpine) GoldenOchre.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isExposedSpine) GoldenOchre.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isExposedSpine) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isExposedSpine) SaddleBrown else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isExposedSpine) "LOMO DESCUBIERTO (Costura Expuesta)" else "LOMO CUBIERTO (Forrado y Protegido)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isExposedSpine) SaddleBrown else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isExposedSpine) {
                                "Los cuadernillos e hilos quedan a la vista. Apertura plana de 180° total sin lomera rígida."
                            } else {
                                "Lomera entelada o en piel con cabezadas tejidas a mano y refuerzo estructural."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // View Selector Tabs (Despiece Desplegado vs Sección de Lomo)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .height(38.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Despiece Exterior (Tapas & Lomo)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Corte Transversal (Sección)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2D Drawing Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2124)) // Cutting mat / dark blueprint background
                    .border(1.dp, Color(0xFF33383F), RoundedCornerShape(12.dp))
            ) {
                val textMeasurer = rememberTextMeasurer()

                if (selectedTab == 0) {
                    // DESPIECE EXTERIOR (Contraportada + Lomo + Portada)
                    UnfoldedCoverBlueprintCanvas(
                        bindingType = bindingType,
                        coverColor = coverColor,
                        customBitmap = customTextureBitmap,
                        foilTitle = foilTitle,
                        foilSubtitle = foilSubtitle,
                        foilColorType = foilColorType,
                        hasRibbon = hasRibbon,
                        hasCorners = hasCornerGuards,
                        widthCm = widthCm,
                        lengthCm = lengthCm,
                        spineThicknessMm = spineThicknessMm,
                        textMeasurer = textMeasurer
                    )
                } else {
                    // CORTE TRANSVERSAL 2D (Perfil del lomo y bloque de hojas)
                    CrossSectionSpineCanvas(
                        bindingType = bindingType,
                        coverColor = coverColor,
                        widthCm = widthCm,
                        spineThicknessMm = spineThicknessMm,
                        sheetCount = sheetCount,
                        estimatedSignatures = estimatedSignatures,
                        sheetsPerSignature = sheetsPerSignature,
                        textMeasurer = textMeasurer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dimensions and Specs Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecItem(
                    label = "Tapas (C/U)",
                    value = "${widthCm} × ${lengthCm} cm"
                )
                SpecItem(
                    label = "Lomo",
                    value = "${String.format(java.util.Locale.US, "%.1f", spineThicknessMm)} mm ${if (isExposedSpine) "(Abierto)" else "(Cerrado)"}"
                )
                SpecItem(
                    label = "Cuadernillos",
                    value = "$estimatedSignatures (${sheetsPerSignature}h c/u)"
                )
                SpecItem(
                    label = "Cartón / Ceja",
                    value = "2.5 mm / +3 mm"
                )
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun UnfoldedCoverBlueprintCanvas(
    bindingType: BindingType,
    coverColor: Color,
    customBitmap: Bitmap?,
    foilTitle: String,
    foilSubtitle: String,
    foilColorType: String,
    hasRibbon: Boolean,
    hasCorners: Boolean,
    widthCm: Float,
    lengthCm: Float,
    spineThicknessMm: Float,
    textMeasurer: TextMeasurer
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val isExposed = bindingType.spineType.isExposed

        // Draw cutting mat grid pattern (subtle 20px grid)
        val gridStep = 24f
        val gridPaintColor = Color(0xFF282D34)
        var xGrid = 0f
        while (xGrid < w) {
            drawLine(gridPaintColor, Offset(xGrid, 0f), Offset(xGrid, h), strokeWidth = 1f)
            xGrid += gridStep
        }
        var yGrid = 0f
        while (yGrid < h) {
            drawLine(gridPaintColor, Offset(0f, yGrid), Offset(w, yGrid), strokeWidth = 1f)
            yGrid += gridStep
        }

        // Available area for the unfolded book
        val padX = 24f
        val padY = 32f
        val bookAreaW = w - (padX * 2)
        val bookAreaH = h - (padY * 2)

        // Realistic proportion calculation
        // Total spread width = coverW + hinge + spineW + hinge + coverW
        val totalUnits = (widthCm * 2f) + (spineThicknessMm / 10f) + 1.0f // 1.0cm for hinges
        val pxPerCm = (bookAreaW / totalUnits).coerceIn(4f, 22f)

        val coverW = widthCm * pxPerCm
        val spineW = ((spineThicknessMm / 10f) * pxPerCm).coerceAtLeast(18f)
        val hingeW = 0.5f * pxPerCm
        val bookH = (lengthCm * pxPerCm).coerceAtMost(bookAreaH)

        val startX = (w - (coverW * 2 + spineW + hingeW * 2)) / 2f
        val startY = (h - bookH) / 2f

        val backCoverLeft = startX
        val spineLeft = backCoverLeft + coverW + hingeW
        val frontCoverLeft = spineLeft + spineW + hingeW

        // 1. TAPA TRASERA (Contraportada - Left)
        drawRect(
            color = coverColor,
            topLeft = Offset(backCoverLeft, startY),
            size = Size(coverW, bookH)
        )
        // Texture overlay or border
        drawRect(
            color = Color.Black.copy(alpha = 0.2f),
            topLeft = Offset(backCoverLeft, startY),
            size = Size(coverW, bookH),
            style = Stroke(width = 1.5f)
        )

        // Corner guards on back cover (top-left & bottom-left)
        if (hasCorners) {
            drawCornerTriangle(Offset(backCoverLeft, startY), isTop = true, isLeft = true, size = 14f)
            drawCornerTriangle(Offset(backCoverLeft, startY + bookH), isTop = false, isLeft = true, size = 14f)
        }

        // Label on back cover
        val backLabel = "Contraportada\n${widthCm} x ${lengthCm} cm"
        drawText(
            textMeasurer = textMeasurer,
            text = backLabel,
            topLeft = Offset(backCoverLeft + 10f, startY + (bookH / 2f) - 15f),
            style = TextStyle(color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, textAlign = TextAlign.Center)
        )

        // 2. HINGE CHANNELS (Hendidos de 5mm)
        drawHingeChannel(Offset(backCoverLeft + coverW, startY), hingeW, bookH)
        drawHingeChannel(Offset(spineLeft + spineW, startY), hingeW, bookH)

        // 3. EL LOMO CENTRAL (Spine)
        if (isExposed) {
            // === LOMO DESCUBIERTO / EXPUESTO ===
            // Paper block background (Book Cream / Ahuesado)
            drawRect(
                color = Color(0xFFF4EEDC),
                topLeft = Offset(spineLeft, startY),
                size = Size(spineW, bookH)
            )

            // Draw stacked signatures (cuadernillos) layered lines
            val numSignatures = 10
            val sigH = bookH / numSignatures
            for (i in 0..numSignatures) {
                val y = startY + (i * sigH)
                drawLine(
                    color = Color(0xFFC7BBA2),
                    start = Offset(spineLeft, y),
                    end = Offset(spineLeft + spineW, y),
                    strokeWidth = 1f
                )
            }

            // Draw exposed stitching threads (cadenetas coptas o pasadas de hilo encerado)
            val stitchPositions = listOf(0.18f, 0.38f, 0.62f, 0.82f)
            val threadColor = Color(0xFFC89B3C) // Waxed linen thread color
            val threadDark = Color(0xFF78561C)

            stitchPositions.forEach { fraction ->
                val stitchY = startY + (bookH * fraction)
                // Stitch holes
                drawCircle(
                    color = Color(0xFF3B2E1E),
                    radius = 2.5f,
                    center = Offset(spineLeft + (spineW * 0.25f), stitchY)
                )
                drawCircle(
                    color = Color(0xFF3B2E1E),
                    radius = 2.5f,
                    center = Offset(spineLeft + (spineW * 0.75f), stitchY)
                )
                // Interlocking chain stitches
                drawLine(
                    color = threadColor,
                    start = Offset(spineLeft + (spineW * 0.25f), stitchY - 8f),
                    end = Offset(spineLeft + (spineW * 0.75f), stitchY + 8f),
                    strokeWidth = 2.2f
                )
                drawLine(
                    color = threadColor,
                    start = Offset(spineLeft + (spineW * 0.75f), stitchY - 8f),
                    end = Offset(spineLeft + (spineW * 0.25f), stitchY + 8f),
                    strokeWidth = 2.2f
                )
            }

            // Outline of exposed spine
            drawRect(
                color = GoldenOchre,
                topLeft = Offset(spineLeft, startY),
                size = Size(spineW, bookH),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 3f)))
            )

            // "EXPUESTO" watermark label
            drawText(
                textMeasurer = textMeasurer,
                text = "EXPUESTO\n${String.format(java.util.Locale.US, "%.1f", spineThicknessMm)} mm",
                topLeft = Offset(spineLeft + (spineW / 2f) - 18f, startY + (bookH / 2f) - 16f),
                style = TextStyle(color = SaddleBrown, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            )

        } else {
            // === LOMO CUBIERTO / FORRADO ===
            // Covered spine in matching or contrasting cloth/leather
            val spineColor = if (bindingType.spineType == SpineType.HALF_LEATHER_DUTCH) {
                Color(0xFF3E2723) // Leather spine
            } else {
                coverColor.copy(alpha = 0.95f)
            }

            drawRect(
                color = spineColor,
                topLeft = Offset(spineLeft, startY),
                size = Size(spineW, bookH)
            )

            // Raised bands (nervios en relieve horizontales)
            val nerviosCount = 4
            for (i in 1..nerviosCount) {
                val nervioY = startY + (bookH * (i / (nerviosCount + 1f)))
                // Shadow underneath
                drawLine(
                    color = Color.Black.copy(alpha = 0.5f),
                    start = Offset(spineLeft, nervioY + 2f),
                    end = Offset(spineLeft + spineW, nervioY + 2f),
                    strokeWidth = 2.5f
                )
                // Highlight on top
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(spineLeft, nervioY - 1f),
                    end = Offset(spineLeft + spineW, nervioY - 1f),
                    strokeWidth = 2f
                )
            }

            // Woven Headbands (Cabezadas artesanales bicolor en los extremos)
            drawHeadband(Offset(spineLeft, startY), spineW, isTop = true)
            drawHeadband(Offset(spineLeft, startY + bookH - 5f), spineW, isTop = false)

            // Outline of covered spine
            drawRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(spineLeft, startY),
                size = Size(spineW, bookH),
                style = Stroke(width = 1.5f)
            )

            // "CUBIERTO" label
            drawText(
                textMeasurer = textMeasurer,
                text = "${String.format(java.util.Locale.US, "%.1f", spineThicknessMm)} mm",
                topLeft = Offset(spineLeft + (spineW / 2f) - 14f, startY + (bookH / 2f) - 8f),
                style = TextStyle(color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            )
        }

        // 4. TAPA DELANTERA (Portada - Right)
        drawRect(
            color = coverColor,
            topLeft = Offset(frontCoverLeft, startY),
            size = Size(coverW, bookH)
        )
        // Outer border
        drawRect(
            color = Color.Black.copy(alpha = 0.2f),
            topLeft = Offset(frontCoverLeft, startY),
            size = Size(coverW, bookH),
            style = Stroke(width = 1.5f)
        )

        // Corner guards on front cover (top-right & bottom-right)
        if (hasCorners) {
            drawCornerTriangle(Offset(frontCoverLeft + coverW, startY), isTop = true, isLeft = false, size = 14f)
            drawCornerTriangle(Offset(frontCoverLeft + coverW, startY + bookH), isTop = false, isLeft = false, size = 14f)
        }

        // Silk ribbon bookmark dangling out
        if (hasRibbon) {
            val ribbonX = frontCoverLeft + (coverW * 0.45f)
            val ribbonW = 7f
            val ribbonColor = Color(0xFF9E2A2B) // Burgundy red silk
            drawLine(
                color = ribbonColor,
                start = Offset(ribbonX, startY - 8f),
                end = Offset(ribbonX + 12f, startY + bookH + 18f),
                strokeWidth = ribbonW
            )
        }

        // Stamped Foil Title & Subtitle on Front Cover
        val foilTextColor = when (foilColorType.lowercase()) {
            "plata", "plateado" -> FoilSilver
            "cobre" -> Color(0xFFB87333)
            else -> FoilGold
        }

        val displayTitle = if (foilTitle.isNotBlank()) foilTitle else "PORTADA"
        val titleLayout = textMeasurer.measure(
            text = displayTitle,
            style = TextStyle(
                color = foilTextColor,
                fontSize = if (coverW > 80f) 11.sp else 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )
        )

        drawText(
            textLayoutResult = titleLayout,
            topLeft = Offset(
                frontCoverLeft + ((coverW - titleLayout.size.width) / 2f).coerceAtLeast(4f),
                startY + (bookH * 0.35f)
            )
        )

        if (foilSubtitle.isNotBlank()) {
            val subLayout = textMeasurer.measure(
                text = foilSubtitle,
                style = TextStyle(
                    color = foilTextColor.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )
            )
            drawText(
                textLayoutResult = subLayout,
                topLeft = Offset(
                    frontCoverLeft + ((coverW - subLayout.size.width) / 2f).coerceAtLeast(4f),
                    startY + (bookH * 0.35f) + titleLayout.size.height + 4f
                )
            )
        }

        // 5. DIMENSION ARROWS & ANNOTATIONS
        // Upper dimension line (Width of entire layout)
        val dimY = startY - 14f
        drawLine(
            color = Color(0xFF6B7280),
            start = Offset(backCoverLeft, dimY),
            end = Offset(frontCoverLeft + coverW, dimY),
            strokeWidth = 1f
        )
        // Arrows
        drawLine(Color(0xFF6B7280), Offset(backCoverLeft, dimY - 3f), Offset(backCoverLeft, dimY + 3f), 1f)
        drawLine(Color(0xFF6B7280), Offset(frontCoverLeft + coverW, dimY - 3f), Offset(frontCoverLeft + coverW, dimY + 3f), 1f)

        val totalSpreadCm = String.format(java.util.Locale.US, "%.1f", (widthCm * 2f) + (spineThicknessMm / 10f) + 1.0f)
        drawText(
            textMeasurer = textMeasurer,
            text = "Total desplegado: $totalSpreadCm cm",
            topLeft = Offset((w / 2f) - 60f, dimY - 14f),
            style = TextStyle(color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun CrossSectionSpineCanvas(
    bindingType: BindingType,
    coverColor: Color,
    widthCm: Float,
    spineThicknessMm: Float,
    sheetCount: Int,
    estimatedSignatures: Int = 15,
    sheetsPerSignature: Int = 4,
    textMeasurer: TextMeasurer
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val isExposed = bindingType.spineType.isExposed

        // Background subtle grid
        val gridStep = 24f
        var xGrid = 0f
        while (xGrid < w) {
            drawLine(Color(0xFF282D34), Offset(xGrid, 0f), Offset(xGrid, h), strokeWidth = 1f)
            xGrid += gridStep
        }

        val centerX = w / 2f
        val centerY = h / 2f

        // Draw cross-section diagram:
        // Spine is in center, leaves expand outward to left and right
        val spineH = (spineThicknessMm * 4f).coerceIn(40f, 100f)
        val leafLength = 130f
        val boardThickness = 7f

        val spineX = centerX - 40f
        val topCoverY = centerY - (spineH / 2f)
        val bottomCoverY = centerY + (spineH / 2f)

        // 1. Boards (Cartón de tapas 2.5mm)
        // Top board
        drawRoundRect(
            color = coverColor,
            topLeft = Offset(spineX, topCoverY - boardThickness),
            size = Size(leafLength + 15f, boardThickness),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Bottom board
        drawRoundRect(
            color = coverColor,
            topLeft = Offset(spineX, bottomCoverY),
            size = Size(leafLength + 15f, boardThickness),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // 2. Book Block Signatures (Cuadernillos plegados)
        val numSignatures = estimatedSignatures.coerceIn(3, 14)
        val step = spineH / numSignatures
        for (i in 0 until numSignatures) {
            val sigY = topCoverY + (i * step) + (step / 2f)
            // Draw folded signature loop (U-curve at spine, extending out)
            val path = Path().apply {
                moveTo(spineX + leafLength, sigY - 2f)
                lineTo(spineX + 6f, sigY - 2f)
                cubicTo(
                    spineX, sigY - 2f,
                    spineX, sigY + 2f,
                    spineX + 6f, sigY + 2f
                )
                lineTo(spineX + leafLength, sigY + 2f)
            }
            drawPath(path, color = Color(0xFFEADBCE), style = Stroke(width = 2f))
        }

        // 3. Spine Construction: EXPOSED vs COVERED
        if (isExposed) {
            // === LOMO DESCUBIERTO ===
            // Exposed thread stitches piercing signatures
            for (i in 0 until numSignatures) {
                val sigY = topCoverY + (i * step) + (step / 2f)
                drawCircle(
                    color = Color(0xFFC89B3C),
                    radius = 3f,
                    center = Offset(spineX + 3f, sigY)
                )
            }
            // Vertical connecting thread loops
            drawLine(
                color = Color(0xFFC89B3C),
                start = Offset(spineX + 3f, topCoverY),
                end = Offset(spineX + 3f, bottomCoverY),
                strokeWidth = 2.5f
            )

            // Explanatory callout
            drawText(
                textMeasurer = textMeasurer,
                text = "← LOMO DESCUBIERTO\nCostura directa al canto\nSin lomera rígida ni adhesivo\nApertura plana de 180° total",
                topLeft = Offset(spineX - 180f, centerY - 25f),
                style = TextStyle(color = GoldenOchre, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )

        } else {
            // === LOMO CUBIERTO ===
            // Hollow spine curve / cloth wrapping around the back
            val spineCoverPath = Path().apply {
                moveTo(spineX + 12f, topCoverY - boardThickness)
                cubicTo(
                    spineX - 25f, topCoverY,
                    spineX - 25f, bottomCoverY,
                    spineX + 12f, bottomCoverY + boardThickness
                )
            }
            drawPath(
                spineCoverPath,
                color = coverColor,
                style = Stroke(width = 4.5f)
            )

            // Headband indicator (cabezada)
            drawCircle(Color(0xFF9E2A2B), radius = 4f, center = Offset(spineX + 4f, topCoverY - 2f))
            drawCircle(Color(0xFF9E2A2B), radius = 4f, center = Offset(spineX + 4f, bottomCoverY + 2f))

            // Explanatory callout
            drawText(
                textMeasurer = textMeasurer,
                text = "← LOMO CUBIERTO\nLomera forrada con fuelle\nTarlatana de refuerzo\nCabezadas artesanales",
                topLeft = Offset(spineX - 170f, centerY - 25f),
                style = TextStyle(color = Color(0xFF60A5FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
        }

        // Callout on the right: Ceja & Hojas & Cuadernillos
        drawText(
            textMeasurer = textMeasurer,
            text = "Bloque: $sheetCount hojas ($estimatedSignatures cuad. de ${sheetsPerSignature}h) →\nCeja de cartón (+3 mm de protección)",
            topLeft = Offset(spineX + leafLength + 10f, centerY - 15f),
            style = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
        )
    }
}

private fun DrawScope.drawHingeChannel(topLeft: Offset, width: Float, height: Float) {
    drawRect(
        color = Color(0xFF15171A),
        topLeft = topLeft,
        size = Size(width, height)
    )
    // Crease line
    drawLine(
        color = Color(0xFF2A2E35),
        start = Offset(topLeft.x + (width / 2f), topLeft.y),
        end = Offset(topLeft.x + (width / 2f), topLeft.y + height),
        strokeWidth = 1f
    )
}

private fun DrawScope.drawCornerTriangle(origin: Offset, isTop: Boolean, isLeft: Boolean, size: Float) {
    val path = Path().apply {
        moveTo(origin.x, origin.y)
        val x2 = if (isLeft) origin.x + size else origin.x - size
        val y2 = origin.y
        val x3 = origin.x
        val y3 = if (isTop) origin.y + size else origin.y - size
        lineTo(x2, y2)
        lineTo(x3, y3)
        close()
    }
    // Brass / Gold metallic corner
    drawPath(path, color = Color(0xFFD4AF37))
    drawPath(path, color = Color(0xFF78561C), style = Stroke(width = 1f))
}

private fun DrawScope.drawHeadband(origin: Offset, spineW: Float, isTop: Boolean) {
    val h = 5f
    val segments = 6
    val segW = spineW / segments
    for (i in 0 until segments) {
        val color = if (i % 2 == 0) Color(0xFF9E2A2B) else Color(0xFFD4AF37)
        drawRect(
            color = color,
            topLeft = Offset(origin.x + (i * segW), origin.y),
            size = Size(segW, h)
        )
    }
}
