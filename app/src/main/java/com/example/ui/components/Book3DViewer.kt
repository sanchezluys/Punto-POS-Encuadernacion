package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import com.example.ui.theme.FoilGold
import com.example.ui.theme.FoilSilver
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.SaddleBrown
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Float, val y: Float, val z: Float) {
    fun rotateY(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(
            x * cosA + z * sinA,
            y,
            -x * sinA + z * cosA
        )
    }

    fun rotateX(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(
            x,
            y * cosA - z * sinA,
            y * sinA + z * cosA
        )
    }

    fun rotateZ(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(
            x * cosA - y * sinA,
            x * sinA + y * cosA,
            z
        )
    }

    fun project(centerX: Float, centerY: Float, fov: Float = 600f): Offset {
        val distance = fov + z
        val scale = if (distance > 10f) fov / distance else 1f
        return Offset(centerX + x * scale, centerY + y * scale)
    }
}

@Composable
fun Book3DViewer(
    modifier: Modifier = Modifier,
    bindingType: BindingType,
    coverColor: Color = Color(bindingType.defaultColorHex),
    customTextureBitmap: Bitmap? = null,
    foilTitle: String = "",
    foilSubtitle: String = "",
    foilColorType: String = "Dorado", // Dorado, Plateado, Cobre, Golpe Seco
    hasRibbon: Boolean = bindingType.hasRibbon,
    hasCornerGuards: Boolean = bindingType.hasCornerGuards,
    ribbonColor: Color = Color(0xFFC41E3A), // Wine/Ruby ribbon
    showControls: Boolean = true,
    initialYaw: Float = -25f,
    initialPitch: Float = 15f
) {
    var yawDeg by remember { mutableFloatStateOf(initialYaw) }
    var pitchDeg by remember { mutableFloatStateOf(initialPitch) }
    var openAngleDeg by remember { mutableFloatStateOf(0f) }
    var isAutoRotating by remember { mutableStateOf(false) }

    // Auto-rotation effect
    LaunchedEffect(isAutoRotating) {
        while (isAutoRotating) {
            yawDeg = (yawDeg + 1.2f) % 360f
            delay(16)
        }
    }

    val animatedOpenAngle by animateFloatAsState(
        targetValue = openAngleDeg,
        animationSpec = tween(durationMillis = 350),
        label = "openAngle"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF3F3FA),
                        Color(0xFFE5E7F0)
                    )
                )
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    isAutoRotating = false
                    yawDeg += dragAmount.x * 0.5f
                    pitchDeg = (pitchDeg - dragAmount.y * 0.4f).coerceIn(-65f, 65f)
                }
            }
            .testTag("book_3d_canvas_container")
    ) {
        Canvas(modifier = Modifier.fillMaxSize().testTag("book_3d_canvas")) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            draw3DBook(
                centerX = cx,
                centerY = cy,
                yawDeg = yawDeg,
                pitchDeg = pitchDeg,
                openAngleDeg = animatedOpenAngle,
                bindingType = bindingType,
                coverColor = coverColor,
                customTextureBitmap = customTextureBitmap,
                foilTitle = foilTitle,
                foilSubtitle = foilSubtitle,
                foilColorType = foilColorType,
                hasRibbon = hasRibbon,
                hasCornerGuards = hasCornerGuards,
                ribbonColor = ribbonColor
            )
        }

        // Overlay controls
        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Bar inside 3D viewer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "3D Realtime",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = " 3D Interactivo • Arrastra para girar",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Auto-spin toggle
                        IconButton(
                            onClick = { isAutoRotating = !isAutoRotating },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isAutoRotating) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.92f),
                                    CircleShape
                                )
                                .testTag("btn_auto_rotate")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoMode,
                                contentDescription = "Auto Giro",
                                tint = if (isAutoRotating) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reset View
                        IconButton(
                            onClick = {
                                yawDeg = -25f
                                pitchDeg = 15f
                                isAutoRotating = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.92f), CircleShape)
                                .testTag("btn_reset_view")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Vista",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom control panel for Book Opening Angle
                Surface(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = " Apertura del Libro: ${openAngleDeg.toInt()}°",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = if (openAngleDeg == 0f) "Cerrado" else if (openAngleDeg < 90f) "Semi-abierto" else "Abierto",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = openAngleDeg,
                            onValueChange = { openAngleDeg = it },
                            valueRange = 0f..140f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.height(28.dp).testTag("slider_open_angle")
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.draw3DBook(
    centerX: Float,
    centerY: Float,
    yawDeg: Float,
    pitchDeg: Float,
    openAngleDeg: Float,
    bindingType: BindingType,
    coverColor: Color,
    customTextureBitmap: Bitmap?,
    foilTitle: String,
    foilSubtitle: String,
    foilColorType: String,
    hasRibbon: Boolean,
    hasCornerGuards: Boolean,
    ribbonColor: Color
) {
    val yawRad = (yawDeg * PI / 180f).toFloat()
    val pitchRad = (pitchDeg * PI / 180f).toFloat()

    // Dimensions of standard book in 3D coordinate space
    val bookWidth = 145f
    val bookHeight = 210f
    val bookThickness = 32f
    val coverOverhang = 5f

    // Transform function
    fun tr(p: Point3D): Point3D {
        return p.rotateY(yawRad).rotateX(pitchRad)
    }

    fun proj(p: Point3D): Offset {
        return tr(p).project(centerX, centerY, 550f)
    }

    // Shadow on surface
    val shadowCenter = Point3D(0f, bookHeight / 2f + 40f, 0f)
    val shadowProj = proj(shadowCenter)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
            center = shadowProj,
            radius = 180f
        ),
        topLeft = Offset(shadowProj.x - 170f, shadowProj.y - 45f),
        size = Size(340f, 90f)
    )

    // Lighting vector (directional light from top-left-front)
    val lightDir = Point3D(-0.5f, -0.8f, -1.0f)

    // Calculate normals and depths for polygon ordering (Painter's Algorithm)
    // 1. Back Cover Coordinates
    val backTL = Point3D(-bookWidth / 2f, -bookHeight / 2f - coverOverhang, bookThickness / 2f)
    val backTR = Point3D(bookWidth / 2f, -bookHeight / 2f - coverOverhang, bookThickness / 2f)
    val backBR = Point3D(bookWidth / 2f, bookHeight / 2f + coverOverhang, bookThickness / 2f)
    val backBL = Point3D(-bookWidth / 2f, bookHeight / 2f + coverOverhang, bookThickness / 2f)

    // 2. Page Block Coordinates
    val pageTL = Point3D(-bookWidth / 2f + 4f, -bookHeight / 2f, -bookThickness / 2f + 4f)
    val pageTR = Point3D(bookWidth / 2f - 4f, -bookHeight / 2f, -bookThickness / 2f + 4f)
    val pageBR = Point3D(bookWidth / 2f - 4f, bookHeight / 2f, -bookThickness / 2f + 4f)
    val pageBL = Point3D(-bookWidth / 2f + 4f, bookHeight / 2f, -bookThickness / 2f + 4f)

    val pageBackTL = Point3D(-bookWidth / 2f + 4f, -bookHeight / 2f, bookThickness / 2f - 4f)
    val pageBackTR = Point3D(bookWidth / 2f - 4f, -bookHeight / 2f, bookThickness / 2f - 4f)
    val pageBackBR = Point3D(bookWidth / 2f - 4f, bookHeight / 2f, bookThickness / 2f - 4f)
    val pageBackBL = Point3D(-bookWidth / 2f + 4f, bookHeight / 2f, bookThickness / 2f - 4f)

    // 3. Front Cover Coordinates (with open angle rotation around spine hinge at x = -bookWidth/2)
    val openRad = (-openAngleDeg * PI / 180f).toFloat()
    fun rotateCoverPoint(localX: Float, y: Float, z: Float): Point3D {
        // Spine hinge is at (-bookWidth/2, z)
        val hingeX = -bookWidth / 2f
        val relX = localX - hingeX
        val rotatedRelX = relX * cos(openRad)
        val rotatedRelZ = relX * sin(openRad)
        return Point3D(hingeX + rotatedRelX, y, z + rotatedRelZ)
    }

    val frontTL = rotateCoverPoint(-bookWidth / 2f, -bookHeight / 2f - coverOverhang, -bookThickness / 2f)
    val frontTR = rotateCoverPoint(bookWidth / 2f, -bookHeight / 2f - coverOverhang, -bookThickness / 2f)
    val frontBR = rotateCoverPoint(bookWidth / 2f, bookHeight / 2f + coverOverhang, -bookThickness / 2f)
    val frontBL = rotateCoverPoint(-bookWidth / 2f, bookHeight / 2f + coverOverhang, -bookThickness / 2f)

    // Spine Coordinates
    val spineTL = Point3D(-bookWidth / 2f, -bookHeight / 2f - coverOverhang, -bookThickness / 2f)
    val spineTR = Point3D(-bookWidth / 2f, -bookHeight / 2f - coverOverhang, bookThickness / 2f)
    val spineBR = Point3D(-bookWidth / 2f, bookHeight / 2f + coverOverhang, bookThickness / 2f)
    val spineBL = Point3D(-bookWidth / 2f, bookHeight / 2f + coverOverhang, -bookThickness / 2f)

    // Draw polygons sorted by average transformed Z
    data class Poly3D(
        val name: String,
        val points: List<Point3D>,
        val drawAction: () -> Unit
    ) {
        val avgZ: Float
            get() = points.map { tr(it).z }.average().toFloat()
    }

    val polygons = mutableListOf<Poly3D>()

    // Back cover polygon
    polygons.add(
        Poly3D("back_cover", listOf(backTL, backTR, backBR, backBL)) {
            val p = Path().apply {
                val pt0 = proj(backTL)
                moveTo(pt0.x, pt0.y)
                val pt1 = proj(backTR); lineTo(pt1.x, pt1.y)
                val pt2 = proj(backBR); lineTo(pt2.x, pt2.y)
                val pt3 = proj(backBL); lineTo(pt3.x, pt3.y)
                close()
            }
            drawPath(p, color = coverColor.copy(alpha = 0.9f))
            drawPath(p, color = Color.Black.copy(alpha = 0.35f))
            drawPath(p, color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.5f))
        }
    )

    // Page Block: Right Fore-edge (Edge of pages)
    polygons.add(
        Poly3D("page_fore_edge", listOf(pageTR, pageBackTR, pageBackBR, pageBR)) {
            val p = Path().apply {
                val pt0 = proj(pageTR); moveTo(pt0.x, pt0.y)
                val pt1 = proj(pageBackTR); lineTo(pt1.x, pt1.y)
                val pt2 = proj(pageBackBR); lineTo(pt2.x, pt2.y)
                val pt3 = proj(pageBR); lineTo(pt3.x, pt3.y)
                close()
            }
            // Cream / ahuesado paper page edge shading
            drawPath(
                p,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE8DECA),
                        Color(0xFFD3C5AB),
                        Color(0xFFF7F2E7),
                        Color(0xFFC7B799)
                    )
                )
            )
            // Draw subtle page line stripes
            val pTop = proj(pageTR)
            val pBottom = proj(pageBR)
            val pBackTop = proj(pageBackTR)
            val pBackBottom = proj(pageBackBR)
            for (i in 1..8) {
                val fraction = i / 9f
                val start = Offset(
                    pTop.x + (pBackTop.x - pTop.x) * fraction,
                    pTop.y + (pBackTop.y - pTop.y) * fraction
                )
                val end = Offset(
                    pBottom.x + (pBackBottom.x - pBottom.x) * fraction,
                    pBottom.y + (pBackBottom.y - pBottom.y) * fraction
                )
                drawLine(
                    color = Color(0xFFB5A486).copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = 1f
                )
            }
        }
    )

    // Page Block: Top Edge (Head)
    polygons.add(
        Poly3D("page_top_edge", listOf(pageTL, pageTR, pageBackTR, pageBackTL)) {
            val p = Path().apply {
                val pt0 = proj(pageTL); moveTo(pt0.x, pt0.y)
                val pt1 = proj(pageTR); lineTo(pt1.x, pt1.y)
                val pt2 = proj(pageBackTR); lineTo(pt2.x, pt2.y)
                val pt3 = proj(pageBackTL); lineTo(pt3.x, pt3.y)
                close()
            }
            drawPath(
                p,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFDCCFBA),
                        Color(0xFFEFE6D6),
                        Color(0xFFC9B99E)
                    )
                )
            )
        }
    )

    // Page Block: Bottom Edge (Tail)
    polygons.add(
        Poly3D("page_bottom_edge", listOf(pageBL, pageBR, pageBackBR, pageBackBL)) {
            val p = Path().apply {
                val pt0 = proj(pageBL); moveTo(pt0.x, pt0.y)
                val pt1 = proj(pageBR); lineTo(pt1.x, pt1.y)
                val pt2 = proj(pageBackBR); lineTo(pt2.x, pt2.y)
                val pt3 = proj(pageBackBL); lineTo(pt3.x, pt3.y)
                close()
            }
            drawPath(
                p,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB5A48B),
                        Color(0xFFD6C8B1),
                        Color(0xFF9E8E75)
                    )
                )
            )
        }
    )

    // Inside Pages View when Book is Open
    if (openAngleDeg > 5f) {
        polygons.add(
            Poly3D("open_inner_page", listOf(pageTL, pageTR, pageBR, pageBL)) {
                val p = Path().apply {
                    val pt0 = proj(pageTL); moveTo(pt0.x, pt0.y)
                    val pt1 = proj(pageTR); lineTo(pt1.x, pt1.y)
                    val pt2 = proj(pageBR); lineTo(pt2.x, pt2.y)
                    val pt3 = proj(pageBL); lineTo(pt3.x, pt3.y)
                    close()
                }
                // First interior page / flyleaf
                drawPath(p, color = Color(0xFFF9F5EB))

                // Endpaper Marbled or artisan illustration texture
                val p0 = proj(pageTL)
                val p1 = proj(pageTR)
                val p2 = proj(pageBR)
                val p3 = proj(pageBL)

                // Simulated text/grid lines on open book page
                for (j in 3..14) {
                    val lineFrac = j / 18f
                    val lx1 = p0.x + (p3.x - p0.x) * lineFrac + 15f
                    val ly1 = p0.y + (p3.y - p0.y) * lineFrac
                    val lx2 = p1.x + (p2.x - p1.x) * lineFrac - 15f
                    val ly2 = p1.y + (p2.y - p1.y) * lineFrac
                    drawLine(
                        color = Color(0xFF8B7D6B).copy(alpha = 0.35f),
                        start = Offset(lx1, ly1),
                        end = Offset(lx2, ly2),
                        strokeWidth = 1.5f
                    )
                }
            }
        )
    }

    // Spine Polygon
    polygons.add(
        Poly3D("spine", listOf(spineTL, spineTR, spineBR, spineBL)) {
            val p = Path().apply {
                val pt0 = proj(spineTL); moveTo(pt0.x, pt0.y)
                val pt1 = proj(spineTR); lineTo(pt1.x, pt1.y)
                val pt2 = proj(spineBR); lineTo(pt2.x, pt2.y)
                val pt3 = proj(spineBL); lineTo(pt3.x, pt3.y)
                close()
            }

            // Spine color with realistic cylinder lighting
            drawPath(
                p,
                brush = Brush.linearGradient(
                    colors = listOf(
                        coverColor.copy(alpha = 0.85f),
                        coverColor,
                        coverColor.copy(alpha = 0.6f)
                    ),
                    start = proj(spineTL),
                    end = proj(spineTR)
                )
            )

            // Specialized Spine details based on Binding Style
            when (bindingType.spineType) {
                SpineType.EXPOSED_COPTIC -> {
                    // Draw exposed multi-needle coptic stitches
                    val spTop = proj(spineTL)
                    val spBottom = proj(spineBL)
                    val spRightTop = proj(spineTR)
                    val spRightBottom = proj(spineBR)

                    for (k in 1..7) {
                        val frac = k / 8f
                        val sx1 = spTop.x + (spBottom.x - spTop.x) * frac
                        val sy1 = spTop.y + (spBottom.y - spTop.y) * frac
                        val sx2 = spRightTop.x + (spRightBottom.x - spRightTop.x) * frac
                        val sy2 = spRightTop.y + (spRightBottom.y - spRightTop.y) * frac

                        // Criss-cross braided stitch
                        drawLine(
                            color = Color(0xFFFAF0DD),
                            start = Offset(sx1, sy1 - 4f),
                            end = Offset(sx2, sy2 + 4f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFD4A017),
                            start = Offset(sx1, sy1 + 4f),
                            end = Offset(sx2, sy2 - 4f),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                SpineType.JAPANESE_STAB -> {
                    // Draw 4-hole oriental stab binding cord
                    val spTop = proj(spineTL)
                    val spBottom = proj(spineBL)
                    for (h in 1..4) {
                        val frac = h / 5f
                        val hx = spTop.x + (spBottom.x - spTop.x) * frac
                        val hy = spTop.y + (spBottom.y - spTop.y) * frac
                        drawCircle(
                            color = Color(0xFF1E1510),
                            radius = 4f,
                            center = Offset(hx + 12f, hy)
                        )
                        drawLine(
                            color = Color(0xFFF3E5AB),
                            start = Offset(hx, hy),
                            end = Offset(hx + 12f, hy),
                            strokeWidth = 2.5f
                        )
                    }
                }
                SpineType.SPIRAL_WIRE -> {
                    // Draw double metallic wire loops
                    val spTop = proj(spineTL)
                    val spBottom = proj(spineBL)
                    for (w in 1..14) {
                        val frac = w / 15f
                        val wx = spTop.x + (spBottom.x - spTop.x) * frac
                        val wy = spTop.y + (spBottom.y - spTop.y) * frac
                        drawCircle(
                            color = FoilGold,
                            radius = 5.5f,
                            center = Offset(wx + 4f, wy),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
                SpineType.ROUNDED, SpineType.FLAT -> {
                    // Raised ribs (nervios en relieve) on leather/linen spine
                    val spTop = proj(spineTL)
                    val spBottom = proj(spineBL)
                    val spRightTop = proj(spineTR)
                    val spRightBottom = proj(spineBR)

                    for (rib in 1..4) {
                        val frac = rib / 5f
                        val rx1 = spTop.x + (spBottom.x - spTop.x) * frac
                        val ry1 = spTop.y + (spBottom.y - spTop.y) * frac
                        val rx2 = spRightTop.x + (spRightBottom.x - spRightTop.x) * frac
                        val ry2 = spRightTop.y + (spRightBottom.y - spRightTop.y) * frac

                        // Raised rib highlight & shadow
                        drawLine(
                            color = Color.Black.copy(alpha = 0.5f),
                            start = Offset(rx1, ry1 + 2f),
                            end = Offset(rx2, ry2 + 2f),
                            strokeWidth = 3.5f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(rx1, ry1 - 1f),
                            end = Offset(rx2, ry2 - 1f),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }
    )

    // Front Cover Polygon (Interactive, with Texture, Foil text, Corner guards)
    polygons.add(
        Poly3D("front_cover", listOf(frontTL, frontTR, frontBR, frontBL)) {
            val p = Path().apply {
                val pt0 = proj(frontTL); moveTo(pt0.x, pt0.y)
                val pt1 = proj(frontTR); lineTo(pt1.x, pt1.y)
                val pt2 = proj(frontBR); lineTo(pt2.x, pt2.y)
                val pt3 = proj(frontBL); lineTo(pt3.x, pt3.y)
                close()
            }

            // If user took camera texture or selected custom bitmap
            if (customTextureBitmap != null && !customTextureBitmap.isRecycled) {
                drawIntoCanvas { canvas ->
                    val paint = NativePaint().apply {
                        isAntiAlias = true
                        val shader = BitmapShader(customTextureBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                        // Apply scale matrix to fit nicely
                        val matrix = Matrix()
                        val scale = 0.5f
                        matrix.setScale(scale, scale)
                        shader.setLocalMatrix(matrix)
                        this.shader = shader
                    }
                    val androidPath = android.graphics.Path()
                    val pt0 = proj(frontTL); androidPath.moveTo(pt0.x, pt0.y)
                    val pt1 = proj(frontTR); androidPath.lineTo(pt1.x, pt1.y)
                    val pt2 = proj(frontBR); androidPath.lineTo(pt2.x, pt2.y)
                    val pt3 = proj(frontBL); androidPath.lineTo(pt3.x, pt3.y)
                    androidPath.close()

                    canvas.nativeCanvas.drawPath(androidPath, paint)
                }
                // Tint overlay with selected color
                drawPath(p, color = coverColor.copy(alpha = 0.35f))
            } else {
                // Base material color with subtle leather grain gradient
                drawPath(
                    p,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            coverColor,
                            coverColor.copy(alpha = 0.92f),
                            coverColor.copy(alpha = 0.78f)
                        ),
                        start = proj(frontTL),
                        end = proj(frontBR)
                    )
                )
            }

            // Highlight glare / reflection when rotating in light
            val angleRatio = (sin(yawRad) + cos(pitchRad)) / 2f
            if (angleRatio > 0.1f) {
                drawPath(
                    p,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f * angleRatio),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.15f)
                        ),
                        start = proj(frontTL),
                        end = proj(frontBR)
                    )
                )
            }

            // Decorative Blind Embossing border (Marco perimetral hendido)
            val insetFactor = 0.12f
            val ptTL = proj(frontTL)
            val ptTR = proj(frontTR)
            val ptBR = proj(frontBR)
            val ptBL = proj(frontBL)

            val inTL = Offset(ptTL.x + (ptBR.x - ptTL.x) * insetFactor, ptTL.y + (ptBR.y - ptTL.y) * insetFactor)
            val inTR = Offset(ptTR.x + (ptBL.x - ptTR.x) * insetFactor, ptTR.y + (ptBL.y - ptTR.y) * insetFactor)
            val inBR = Offset(ptBR.x + (ptTL.x - ptBR.x) * insetFactor, ptBR.y + (ptTL.y - ptBR.y) * insetFactor)
            val inBL = Offset(ptBL.x + (ptTR.x - ptBL.x) * insetFactor, ptBL.y + (ptTR.y - ptBL.y) * insetFactor)

            val innerFrame = Path().apply {
                moveTo(inTL.x, inTL.y)
                lineTo(inTR.x, inTR.y)
                lineTo(inBR.x, inBR.y)
                lineTo(inBL.x, inBL.y)
                close()
            }
            drawPath(
                innerFrame,
                color = when (foilColorType) {
                    "Plateado" -> FoilSilver.copy(alpha = 0.7f)
                    "Golpe Seco" -> Color.Black.copy(alpha = 0.4f)
                    "Cobre" -> Color(0xFFCB6D51)
                    else -> FoilGold.copy(alpha = 0.8f) // Dorado
                },
                style = Stroke(width = 2f, join = StrokeJoin.Round)
            )

            // Foil stamped Title & Subtitle in Real-Time 3D Projection!
            val foilPaintColor = when (foilColorType) {
                "Plateado" -> android.graphics.Color.rgb(230, 235, 240)
                "Golpe Seco" -> android.graphics.Color.argb(120, 20, 10, 5)
                "Cobre" -> android.graphics.Color.rgb(205, 127, 50)
                else -> android.graphics.Color.rgb(255, 215, 0) // Gold
            }

            val titleToRender = if (foilTitle.isNotBlank()) foilTitle else bindingType.name
            val subtitleToRender = if (foilSubtitle.isNotBlank()) foilSubtitle else "EDICIÓN ARTESANAL"

            drawIntoCanvas { canvas ->
                val centerCover = Offset(
                    (inTL.x + inTR.x + inBR.x + inBL.x) / 4f,
                    (inTL.y + inTR.y + inBR.y + inBL.y) / 4f
                )

                // Calculate rotation angle of the front cover on 2D screen
                val dx = inTR.x - inTL.x
                val dy = inTR.y - inTL.y
                val rotationAngle = (Math.atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()

                canvas.nativeCanvas.save()
                canvas.nativeCanvas.translate(centerCover.x, centerCover.y)
                canvas.nativeCanvas.rotate(rotationAngle)

                // Draw title
                val textPaint = NativePaint().apply {
                    color = foilPaintColor
                    textSize = 34f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                    setShadowLayer(3f, 1f, 1f, android.graphics.Color.argb(160, 0, 0, 0))
                }
                canvas.nativeCanvas.drawText(titleToRender, 0f, -10f, textPaint)

                // Draw subtitle
                val subTextPaint = NativePaint().apply {
                    color = foilPaintColor
                    textSize = 18f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    letterSpacing = 0.2f
                    setShadowLayer(2f, 1f, 1f, android.graphics.Color.argb(140, 0, 0, 0))
                }
                canvas.nativeCanvas.drawText(subtitleToRender, 0f, 22f, subTextPaint)

                canvas.nativeCanvas.restore()
            }

            // Metal Corner Guards (Esquineros de Bronce/Oro en las 4 esquinas)
            if (hasCornerGuards) {
                val cornerColor = when (foilColorType) {
                    "Plateado" -> FoilSilver
                    else -> GoldenOchre
                }

                fun drawCorner(pCorner: Offset, p1: Offset, p2: Offset) {
                    val cPath = Path().apply {
                        moveTo(pCorner.x, pCorner.y)
                        val v1 = Offset((p1.x - pCorner.x) * 0.18f, (p1.y - pCorner.y) * 0.18f)
                        val v2 = Offset((p2.x - pCorner.x) * 0.18f, (p2.y - pCorner.y) * 0.18f)
                        lineTo(pCorner.x + v1.x, pCorner.y + v1.y)
                        lineTo(pCorner.x + v1.x + v2.x, pCorner.y + v1.y + v2.y)
                        lineTo(pCorner.x + v2.x, pCorner.y + v2.y)
                        close()
                    }
                    drawPath(cPath, color = cornerColor)
                    drawPath(cPath, color = Color.Black.copy(alpha = 0.4f), style = Stroke(width = 1f))
                    // Rivet dot
                    drawCircle(
                        color = Color(0xFF4A2A18),
                        radius = 2f,
                        center = Offset(
                            pCorner.x + (p1.x - pCorner.x) * 0.09f + (p2.x - pCorner.x) * 0.09f,
                            pCorner.y + (p1.y - pCorner.y) * 0.09f + (p2.y - pCorner.y) * 0.09f
                        )
                    )
                }

                drawCorner(ptTL, ptTR, ptBL)
                drawCorner(ptTR, ptTL, ptBR)
                drawCorner(ptBR, ptTR, ptBL)
                drawCorner(ptBL, ptTL, ptBR)
            }
        }
    )

    // Ribbon Bookmark flowing from top spine to bottom
    if (hasRibbon) {
        polygons.add(
            Poly3D("ribbon", listOf(spineTL, pageBL)) {
                val topRibbon = proj(spineTL)
                val bottomRibbon = proj(Point3D(0f, bookHeight / 2f + 40f, -bookThickness / 4f))
                val midRibbon = proj(Point3D(bookWidth / 4f, 0f, -bookThickness / 2f - 10f))

                val rPath = Path().apply {
                    moveTo(topRibbon.x, topRibbon.y)
                    cubicTo(
                        midRibbon.x - 20f, midRibbon.y - 40f,
                        midRibbon.x + 30f, midRibbon.y + 40f,
                        bottomRibbon.x, bottomRibbon.y
                    )
                }
                // Ribbon shadow
                drawPath(
                    rPath,
                    color = Color.Black.copy(alpha = 0.4f),
                    style = Stroke(width = 9f, cap = StrokeCap.Round)
                )
                // Ribbon body
                drawPath(
                    rPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ribbonColor,
                            ribbonColor.copy(alpha = 0.8f),
                            ribbonColor
                        )
                    ),
                    style = Stroke(width = 7f, cap = StrokeCap.Round)
                )
            }
        )
    }

    // Sort polygons from back to front by distance to camera
    val sortedPolygons = polygons.sortedBy { it.avgZ }

    // Execute drawing for each polygon
    for (poly in sortedPolygons) {
        poly.drawAction()
    }
}
