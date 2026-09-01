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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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

    fun project(centerX: Float, centerY: Float, fov: Float = 600f, zoomScale: Float = 1.0f): Offset {
        val distance = fov + z
        val scale = (if (distance > 10f) fov / distance else 1f) * zoomScale
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
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
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
        animationSpec = tween(durationMillis = 300),
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
                detectTransformGestures { _, pan, zoom, _ ->
                    isAutoRotating = false
                    zoomScale = (zoomScale * zoom).coerceIn(0.55f, 2.4f)
                    yawDeg += pan.x * 0.45f
                    pitchDeg = (pitchDeg - pan.y * 0.4f).coerceIn(-75f, 75f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        zoomScale = 1.0f
                        yawDeg = initialYaw
                        pitchDeg = initialPitch
                        isAutoRotating = false
                    }
                )
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
                zoomScale = zoomScale,
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
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Bar inside 3D viewer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interaction guidance pill with current Zoom display
                    Surface(
                        color = Color.White.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E2EC)),
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
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Giro 360° • Zoom ${(zoomScale * 100).toInt()}%",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Floating 3D Action Tools
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zoom Out Button
                        IconButton(
                            onClick = {
                                zoomScale = (zoomScale - 0.2f).coerceIn(0.55f, 2.4f)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.94f), CircleShape)
                                .border(1.dp, Color(0xFFE1E2EC), CircleShape)
                                .testTag("btn_zoom_out")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Alejar Zoom",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Zoom In Button
                        IconButton(
                            onClick = {
                                zoomScale = (zoomScale + 0.2f).coerceIn(0.55f, 2.4f)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.94f), CircleShape)
                                .border(1.dp, Color(0xFFE1E2EC), CircleShape)
                                .testTag("btn_zoom_in")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Acercar Zoom",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Auto-spin toggle
                        IconButton(
                            onClick = { isAutoRotating = !isAutoRotating },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isAutoRotating) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.94f),
                                    CircleShape
                                )
                                .border(1.dp, if (isAutoRotating) MaterialTheme.colorScheme.primary else Color(0xFFE1E2EC), CircleShape)
                                .testTag("btn_auto_rotate")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoMode,
                                contentDescription = "Auto Giro",
                                tint = if (isAutoRotating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Reset View & Zoom
                        IconButton(
                            onClick = {
                                yawDeg = initialYaw
                                pitchDeg = initialPitch
                                zoomScale = 1.0f
                                isAutoRotating = false
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.94f), CircleShape)
                                .border(1.dp, Color(0xFFE1E2EC), CircleShape)
                                .testTag("btn_reset_view")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Vista",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Bottom control panel for Book Opening Angle & Presets
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E2EC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Apertura: ${openAngleDeg.toInt()}°",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Quick preset tags
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    "Cerrado" to 0f,
                                    "45°" to 45f,
                                    "100°" to 100f
                                ).forEach { (label, deg) ->
                                    val isCurrent = (openAngleDeg == deg)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF3F3FA),
                                        modifier = Modifier.clickable { openAngleDeg = deg }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 9.5.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
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
                            modifier = Modifier.height(24.dp).testTag("slider_open_angle")
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
    zoomScale: Float,
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
        return tr(p).project(centerX, centerY, 580f, zoomScale)
    }

    // Shadow on surface under the book
    val shadowCenter = Point3D(0f, bookHeight / 2f + 36f, 0f)
    val shadowProj = proj(shadowCenter)
    val shadowW = 320f * zoomScale
    val shadowH = 80f * zoomScale
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
            center = shadowProj,
            radius = shadowW / 2f
        ),
        topLeft = Offset(shadowProj.x - shadowW / 2f, shadowProj.y - shadowH / 2f),
        size = Size(shadowW, shadowH)
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
                val pt0 = proj(backTL); moveTo(pt0.x, pt0.y)
                val pt1 = proj(backTR); lineTo(pt1.x, pt1.y)
                val pt2 = proj(backBR); lineTo(pt2.x, pt2.y)
                val pt3 = proj(backBL); lineTo(pt3.x, pt3.y)
                close()
            }

            if (customTextureBitmap != null && !customTextureBitmap.isRecycled) {
                drawIntoCanvas { canvas ->
                    val androidPath = android.graphics.Path().apply {
                        val pt0 = proj(backTL); moveTo(pt0.x, pt0.y)
                        val pt1 = proj(backTR); lineTo(pt1.x, pt1.y)
                        val pt2 = proj(backBR); lineTo(pt2.x, pt2.y)
                        val pt3 = proj(backBL); lineTo(pt3.x, pt3.y)
                        close()
                    }

                    canvas.nativeCanvas.save()
                    canvas.nativeCanvas.clipPath(androidPath)

                    val bmpW = customTextureBitmap.width.toFloat()
                    val bmpH = customTextureBitmap.height.toFloat()
                    val src = floatArrayOf(0f, 0f, bmpW, 0f, bmpW, bmpH, 0f, bmpH)
                    val pt0 = proj(backTL)
                    val pt1 = proj(backTR)
                    val pt2 = proj(backBR)
                    val pt3 = proj(backBL)
                    val dst = floatArrayOf(pt0.x, pt0.y, pt1.x, pt1.y, pt2.x, pt2.y, pt3.x, pt3.y)

                    val matrix = Matrix()
                    val mapped = matrix.setPolyToPoly(src, 0, dst, 0, 4)
                    if (!mapped) {
                        matrix.setPolyToPoly(src, 0, dst, 0, 3)
                    }

                    val paint = NativePaint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                        isDither = true
                    }

                    canvas.nativeCanvas.drawBitmap(customTextureBitmap, matrix, paint)
                    canvas.nativeCanvas.restore()
                }
                drawPath(p, color = coverColor.copy(alpha = 0.18f))
                drawPath(p, color = Color.Black.copy(alpha = 0.35f))
            } else {
                drawPath(p, color = coverColor.copy(alpha = 0.9f))
                drawPath(p, color = Color.Black.copy(alpha = 0.35f))
            }
            drawPath(p, color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.5f * zoomScale))
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
            // Page line stripes
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
                    strokeWidth = 1f * zoomScale
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

                val p0 = proj(pageTL)
                val p1 = proj(pageTR)
                val p2 = proj(pageBR)
                val p3 = proj(pageBL)

                // Simulated text lines on open book page
                for (j in 3..14) {
                    val lineFrac = j / 18f
                    val lx1 = p0.x + (p3.x - p0.x) * lineFrac + (12f * zoomScale)
                    val ly1 = p0.y + (p3.y - p0.y) * lineFrac
                    val lx2 = p1.x + (p2.x - p1.x) * lineFrac - (12f * zoomScale)
                    val ly2 = p1.y + (p2.y - p1.y) * lineFrac
                    drawLine(
                        color = Color(0xFF8B7D6B).copy(alpha = 0.35f),
                        start = Offset(lx1, ly1),
                        end = Offset(lx2, ly2),
                        strokeWidth = 1.5f * zoomScale
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

            if (customTextureBitmap != null && !customTextureBitmap.isRecycled) {
                drawIntoCanvas { canvas ->
                    val androidPath = android.graphics.Path().apply {
                        val pt0 = proj(spineTL); moveTo(pt0.x, pt0.y)
                        val pt1 = proj(spineTR); lineTo(pt1.x, pt1.y)
                        val pt2 = proj(spineBR); lineTo(pt2.x, pt2.y)
                        val pt3 = proj(spineBL); lineTo(pt3.x, pt3.y)
                        close()
                    }

                    canvas.nativeCanvas.save()
                    canvas.nativeCanvas.clipPath(androidPath)

                    val bmpW = customTextureBitmap.width.toFloat()
                    val bmpH = customTextureBitmap.height.toFloat()
                    val src = floatArrayOf(0f, 0f, bmpW, 0f, bmpW, bmpH, 0f, bmpH)
                    val pt0 = proj(spineTL)
                    val pt1 = proj(spineTR)
                    val pt2 = proj(spineBR)
                    val pt3 = proj(spineBL)
                    val dst = floatArrayOf(pt0.x, pt0.y, pt1.x, pt1.y, pt2.x, pt2.y, pt3.x, pt3.y)

                    val matrix = Matrix()
                    val mapped = matrix.setPolyToPoly(src, 0, dst, 0, 4)
                    if (!mapped) {
                        matrix.setPolyToPoly(src, 0, dst, 0, 3)
                    }

                    val paint = NativePaint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                        isDither = true
                    }

                    canvas.nativeCanvas.drawBitmap(customTextureBitmap, matrix, paint)
                    canvas.nativeCanvas.restore()
                }
                drawPath(p, color = coverColor.copy(alpha = 0.18f))
            } else {
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
            }

            // Specialized Spine details based on Binding Style
            when (bindingType.spineType) {
                SpineType.EXPOSED_COPTIC -> {
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

                        drawLine(
                            color = Color(0xFFFAF0DD),
                            start = Offset(sx1, sy1 - 4f * zoomScale),
                            end = Offset(sx2, sy2 + 4f * zoomScale),
                            strokeWidth = 3f * zoomScale,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFD4A017),
                            start = Offset(sx1, sy1 + 4f * zoomScale),
                            end = Offset(sx2, sy2 - 4f * zoomScale),
                            strokeWidth = 2.5f * zoomScale,
                            cap = StrokeCap.Round
                        )
                    }
                }
                SpineType.JAPANESE_STAB -> {
                    val spTop = proj(spineTL)
                    val spBottom = proj(spineBL)
                    for (h in 1..4) {
                        val frac = h / 5f
                        val hx = spTop.x + (spBottom.x - spTop.x) * frac
                        val hy = spTop.y + (spBottom.y - spTop.y) * frac
                        drawCircle(
                            color = Color(0xFF1E1510),
                            radius = 4f * zoomScale,
                            center = Offset(hx + 12f * zoomScale, hy)
                        )
                        drawLine(
                            color = Color(0xFFF3E5AB),
                            start = Offset(hx, hy),
                            end = Offset(hx + 12f * zoomScale, hy),
                            strokeWidth = 2.5f * zoomScale
                        )
                    }
                }
                SpineType.SPIRAL_WIRE -> {
                    val spTop = proj(spineTL)
                    val spBottom = proj(spineBL)
                    for (w in 1..14) {
                        val frac = w / 15f
                        val wx = spTop.x + (spBottom.x - spTop.x) * frac
                        val wy = spTop.y + (spBottom.y - spTop.y) * frac
                        drawCircle(
                            color = FoilGold,
                            radius = 5.5f * zoomScale,
                            center = Offset(wx + 4f * zoomScale, wy),
                            style = Stroke(width = 2.5f * zoomScale)
                        )
                    }
                }
                SpineType.ROUNDED, SpineType.FLAT -> {
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

                        drawLine(
                            color = Color.Black.copy(alpha = 0.5f),
                            start = Offset(rx1, ry1 + 2f * zoomScale),
                            end = Offset(rx2, ry2 + 2f * zoomScale),
                            strokeWidth = 3.5f * zoomScale
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(rx1, ry1 - 1f * zoomScale),
                            end = Offset(rx2, ry2 - 1f * zoomScale),
                            strokeWidth = 2f * zoomScale
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
                    val androidPath = android.graphics.Path().apply {
                        val pt0 = proj(frontTL); moveTo(pt0.x, pt0.y)
                        val pt1 = proj(frontTR); lineTo(pt1.x, pt1.y)
                        val pt2 = proj(frontBR); lineTo(pt2.x, pt2.y)
                        val pt3 = proj(frontBL); lineTo(pt3.x, pt3.y)
                        close()
                    }

                    canvas.nativeCanvas.save()
                    canvas.nativeCanvas.clipPath(androidPath)

                    val bmpW = customTextureBitmap.width.toFloat()
                    val bmpH = customTextureBitmap.height.toFloat()
                    val src = floatArrayOf(0f, 0f, bmpW, 0f, bmpW, bmpH, 0f, bmpH)
                    val pt0 = proj(frontTL)
                    val pt1 = proj(frontTR)
                    val pt2 = proj(frontBR)
                    val pt3 = proj(frontBL)
                    val dst = floatArrayOf(pt0.x, pt0.y, pt1.x, pt1.y, pt2.x, pt2.y, pt3.x, pt3.y)

                    val matrix = Matrix()
                    val mapped = matrix.setPolyToPoly(src, 0, dst, 0, 4)
                    if (!mapped) {
                        matrix.setPolyToPoly(src, 0, dst, 0, 3)
                    }

                    val paint = NativePaint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                        isDither = true
                    }

                    canvas.nativeCanvas.drawBitmap(customTextureBitmap, matrix, paint)
                    canvas.nativeCanvas.restore()
                }
                // Tint overlay with selected color for rich texture blending
                drawPath(p, color = coverColor.copy(alpha = 0.18f))
            } else {
                // Base material color with rich artisan shading
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
                style = Stroke(width = 2f * zoomScale, join = StrokeJoin.Round)
            )

            // ONLY draw Foil stamped Title & Subtitle when explicitly provided (clean book otherwise)
            val titleToRender = foilTitle.trim()
            val subtitleToRender = foilSubtitle.trim()

            if (titleToRender.isNotEmpty() || subtitleToRender.isNotEmpty()) {
                val foilPaintColor = when (foilColorType) {
                    "Plateado" -> android.graphics.Color.rgb(230, 235, 240)
                    "Golpe Seco" -> android.graphics.Color.argb(120, 20, 10, 5)
                    "Cobre" -> android.graphics.Color.rgb(205, 127, 50)
                    else -> android.graphics.Color.rgb(255, 215, 0) // Gold
                }

                drawIntoCanvas { canvas ->
                    val centerCover = Offset(
                        (inTL.x + inTR.x + inBR.x + inBL.x) / 4f,
                        (inTL.y + inTR.y + inBR.y + inBL.y) / 4f
                    )

                    val dx = inTR.x - inTL.x
                    val dy = inTR.y - inTL.y
                    val rotationAngle = (Math.atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()

                    canvas.nativeCanvas.save()
                    canvas.nativeCanvas.translate(centerCover.x, centerCover.y)
                    canvas.nativeCanvas.rotate(rotationAngle)

                    if (titleToRender.isNotEmpty()) {
                        val textPaint = NativePaint().apply {
                            color = foilPaintColor
                            textSize = 30f * zoomScale
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            isAntiAlias = true
                            setShadowLayer(3f * zoomScale, 1f, 1f, android.graphics.Color.argb(160, 0, 0, 0))
                        }
                        val yOffset = if (subtitleToRender.isNotEmpty()) -8f * zoomScale else 6f * zoomScale
                        canvas.nativeCanvas.drawText(titleToRender, 0f, yOffset, textPaint)
                    }

                    if (subtitleToRender.isNotEmpty()) {
                        val subTextPaint = NativePaint().apply {
                            color = foilPaintColor
                            textSize = 16f * zoomScale
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            letterSpacing = 0.15f
                            setShadowLayer(2f * zoomScale, 1f, 1f, android.graphics.Color.argb(140, 0, 0, 0))
                        }
                        canvas.nativeCanvas.drawText(subtitleToRender, 0f, 22f * zoomScale, subTextPaint)
                    }

                    canvas.nativeCanvas.restore()
                }
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
                    drawPath(cPath, color = Color.Black.copy(alpha = 0.4f), style = Stroke(width = 1f * zoomScale))
                    drawCircle(
                        color = Color(0xFF4A2A18),
                        radius = 2f * zoomScale,
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
                        midRibbon.x - (20f * zoomScale), midRibbon.y - (40f * zoomScale),
                        midRibbon.x + (30f * zoomScale), midRibbon.y + (40f * zoomScale),
                        bottomRibbon.x, bottomRibbon.y
                    )
                }
                drawPath(
                    rPath,
                    color = Color.Black.copy(alpha = 0.4f),
                    style = Stroke(width = 9f * zoomScale, cap = StrokeCap.Round)
                )
                drawPath(
                    rPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ribbonColor,
                            ribbonColor.copy(alpha = 0.8f),
                            ribbonColor
                        )
                    ),
                    style = Stroke(width = 7f * zoomScale, cap = StrokeCap.Round)
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
