package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.QuoteCalculator
import com.example.data.model.PredefinedBookFormats
import com.example.data.model.PredefinedPapers
import com.example.ui.components.Book3DViewer
import com.example.ui.components.BookProposalShareDialog
import com.example.ui.theme.FoilGold
import com.example.ui.theme.FoilSilver
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.SaddleBrown
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.BookbindingViewModel
import com.example.util.ProposalExportSpec

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulatorScreen(
    viewModel: BookbindingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val simulatorBinding by viewModel.simulatorBinding.collectAsState()
    val coverColorHex by viewModel.simulatorColorHex.collectAsState()
    val customBitmap by viewModel.simulatorCustomBitmap.collectAsState()
    val foilTitle by viewModel.simulatorFoilTitle.collectAsState()
    val foilSubtitle by viewModel.simulatorFoilSubtitle.collectAsState()
    val foilColor by viewModel.simulatorFoilColor.collectAsState()
    val hasRibbon by viewModel.simulatorHasRibbon.collectAsState()
    val hasCorners by viewModel.simulatorHasCorners.collectAsState()

    // Unified Physical Dimensions & Paper States
    val bookWidthCm by viewModel.bookWidthCm.collectAsState()
    val bookLengthCm by viewModel.bookLengthCm.collectAsState()
    val bookSheetCount by viewModel.bookSheetCount.collectAsState()
    val bookPageCount by viewModel.bookPageCount.collectAsState()
    val bookGrammageGsm by viewModel.bookGrammageGsm.collectAsState()
    val bookPaperType by viewModel.bookPaperType.collectAsState()
    val bookFormatSize by viewModel.bookFormatSize.collectAsState()
    val spineThicknessMm by viewModel.calculatedSpineThicknessMm.collectAsState()
    val estimatedSignatures by viewModel.estimatedSignatures.collectAsState()
    val coverMaterial by viewModel.quoteCoverMaterial.collectAsState()
    val quoteResult = viewModel.getCalculatedQuote()

    var showShareDialog by remember { mutableStateOf(false) }

    val exportSpec = remember(
        simulatorBinding, bookWidthCm, bookLengthCm, spineThicknessMm,
        bookSheetCount, bookPageCount, bookPaperType, coverMaterial,
        coverColorHex, foilTitle, foilSubtitle, foilColor,
        hasRibbon, hasCorners, quoteResult
    ) {
        ProposalExportSpec(
            bindingType = simulatorBinding,
            widthCm = bookWidthCm,
            lengthCm = bookLengthCm,
            spineThicknessMm = spineThicknessMm,
            sheetCount = bookSheetCount,
            pageCount = bookPageCount,
            paperType = bookPaperType,
            coverMaterial = coverMaterial,
            coverColorHex = coverColorHex,
            foilTitle = foilTitle,
            foilSubtitle = foilSubtitle,
            foilColorType = foilColor,
            hasRibbon = hasRibbon,
            hasCorners = hasCorners,
            hasSlipcase = false,
            hasEndpapers = true,
            clientName = "",
            clientNotes = "",
            quoteResult = quoteResult
        )
    }

    // Camera Capture Launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setSimulatorCustomBitmap(bitmap)
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhotoLauncher.launch(null)
        }
    }

    // Gallery Picker Launcher
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.loadBitmapFromUri(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("simulator_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simulator Title & 3D Realtime Stage
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Simulador de Acabados & Texturas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Prueba materiales, texturas con la cámara y grabado en pan de oro en 3D.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // 3D Canvas Viewer Card with Title & Texture Info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulator_3d_card"),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = simulatorBinding.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (customBitmap != null) "Textura personalizada aplicada" else "Visualización en vivo 360°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (customBitmap != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = simulatorBinding.spineType.name.replace("_", " "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Book3DViewer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .testTag("simulator_3d_viewer"),
                            bindingType = simulatorBinding,
                            coverColor = Color(coverColorHex),
                            customTextureBitmap = customBitmap,
                            foilTitle = foilTitle,
                            foilSubtitle = foilSubtitle,
                            foilColorType = foilColor,
                            hasRibbon = hasRibbon,
                            hasCornerGuards = hasCorners,
                            showControls = true,
                            widthCm = bookWidthCm,
                            lengthCm = bookLengthCm,
                            spineThicknessMm = spineThicknessMm,
                            sheetCount = bookSheetCount,
                            grammageGsm = bookGrammageGsm
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Share & Quote Bar below 3D Viewer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showShareDialog = true },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .testTag("btn_share_3d_proposal_top"),
                                colors = ButtonDefaults.buttonColors(containerColor = SaddleBrown),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Compartir Ficha 3D", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.prepareQuotationFromSimulator() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_quote_from_viewer_top"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cotizar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // 1. SELECT BINDING TYPE
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "1. Estructura de Encuadernación",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${simulatorBinding.category} • ${simulatorBinding.subtitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(viewModel.bindingTypes, key = { it.id }) { binding ->
                            val isSelected = binding.id == simulatorBinding.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectGlobalBinding(binding) },
                                label = {
                                    Text(
                                        text = binding.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. PHYSICAL DIMENSIONS, SHEETS & GRAMMAGE (CALCULADORA DE LOMO & CORTE)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "2. Dimensiones, Hojas & Gramaje",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "El modelo 3D y la cotización se recalculan en tiempo real",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Format Presets
                    Text("Formato / Tamaño Estándar:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(QuoteCalculator.standardFormats) { fmt ->
                            val isFmtSelected = bookFormatSize == fmt.name
                            FilterChip(
                                selected = isFmtSelected,
                                onClick = { viewModel.setBookFormatOption(fmt) },
                                label = { Text("${fmt.name} (${fmt.widthCm}x${fmt.lengthCm}cm)", fontSize = 12.sp) },
                                leadingIcon = if (isFmtSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ancho & Largo Sliders / Values
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Ancho
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ancho (cm):", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format(java.util.Locale.US, "%.1f", bookWidthCm)} cm", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = bookWidthCm,
                                onValueChange = { viewModel.setBookWidthCm(it) },
                                valueRange = 8f..32f,
                                steps = 24
                            )
                        }

                        // Largo
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Largo (cm):", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format(java.util.Locale.US, "%.1f", bookLengthCm)} cm", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = bookLengthCm,
                                onValueChange = { viewModel.setBookLengthCm(it) },
                                valueRange = 10f..40f,
                                steps = 30
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Paper Type & Grammage Chips
                    Text("Tipo de Papel & Gramaje:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(QuoteCalculator.standardPapers) { paper ->
                            val isPaperSelected = bookPaperType == paper.name
                            FilterChip(
                                selected = isPaperSelected,
                                onClick = { viewModel.setBookPaperOption(paper) },
                                label = { Text("${paper.name} (${paper.grammageGsm} g/m²)", fontSize = 12.sp) },
                                leadingIcon = if (isPaperSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sheet Count Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cantidad de Hojas:", style = MaterialTheme.typography.bodyMedium)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$bookSheetCount hojas ($bookPageCount páginas)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Slider(
                        value = bookSheetCount.toFloat(),
                        onValueChange = { viewModel.setBookSheetCount(it.toInt()) },
                        valueRange = 10f..350f,
                        steps = 34
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calculation Summary Banner
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "📐 Grosor de Lomo:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", spineThicknessMm)} mm",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "📚 Cuadernillos sugeridos:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "$estimatedSignatures cuadernillos (de 4 hojas c/u)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. CAMERA TEXTURE CAPTURE & TEXTURE PRESETS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "2. Texturas Reales & Cámara",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Aplica fotos reales de telas o papeles a la tapa 3D",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (customBitmap != null) {
                            IconButton(
                                onClick = { viewModel.setSimulatorCustomBitmap(null) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Quitar textura", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Camera & Gallery Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val hasCamPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasCamPermission) {
                                    takePhotoLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_capture_texture_camera"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Usar Cámara", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { galleryPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_pick_texture_gallery"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galería / Foto", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Muestrario de Materiales Típicos:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Texture Presets List
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(viewModel.texturePresets, key = { it.id }) { preset ->
                            val isSelected = (coverColorHex == preset.colorHex && customBitmap == null && preset.drawableResId == null)

                            Surface(
                                modifier = Modifier
                                    .width(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.applyPresetTexture(preset) }
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(preset.colorHex))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = preset.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = preset.description,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. COLOR PALETTE SWATCHES
        item {
            val artisanColors = listOf(
                0xFF4A2A18 to "Saddle Brown",
                0xFF2C1810 to "Marrón Ébano",
                0xFF2D5A43 to "Verde Esmeralda",
                0xFF1B3B2B to "Verde Botella",
                0xFF6D213C to "Vino Borgoña",
                0xFF1C2D42 to "Azul Marino",
                0xFF3E4E59 to "Gris Pizarra",
                0xFFB85D38 to "Terracota",
                0xFFD4A017 to "Ocre Dorado",
                0xFF8C6422 to "Cuero Mostaza",
                0xFF1C1B1F to "Negro Carbón",
                0xFFFAF6EE to "Pergamino Claro"
            )
            val selectedColorName = artisanColors.find { it.first == coverColorHex }?.second ?: "Color Personalizado"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Color de Cubierta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = selectedColorName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        artisanColors.forEach { (colorLong, name) ->
                            val isColorSelected = (coverColorHex == colorLong)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorLong))
                                    .border(
                                        width = if (isColorSelected) 3.dp else 1.5.dp,
                                        color = if (isColorSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.setSimulatorColor(colorLong)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = name,
                                        tint = if (colorLong == 0xFFFAF6EE) Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. FOIL STAMPING & TITLES
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "4. Estampado & Grabado en Tapa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Opcional: escribe para estampar en la tapa 3D",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (foilTitle.isNotEmpty() || foilSubtitle.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.setSimulatorFoilTitle("")
                                    viewModel.setSimulatorFoilSubtitle("")
                                }
                            ) {
                                Text("Limpiar", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Foil Color Selector Chips with High Contrast
                    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    val onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
                    val foilOptions = listOf(
                        "Dorado" to (Color(0xFFE5B83B) to Color(0xFF261D00)),
                        "Plateado" to (Color(0xFFCFD5DC) to Color(0xFF191C20)),
                        "Cobre" to (Color(0xFFD9724C) to Color(0xFF2E0E05)),
                        "Golpe Seco" to (secondaryContainerColor to onSecondaryContainerColor)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        foilOptions.forEach { (type, colorPair) ->
                            val isFoilSelected = foilColor == type
                            FilterChip(
                                selected = isFoilSelected,
                                onClick = { viewModel.setSimulatorFoilColor(type) },
                                label = {
                                    Text(
                                        text = type,
                                        fontSize = 12.sp,
                                        fontWeight = if (isFoilSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isFoilSelected) colorPair.second else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorPair.first,
                                    selectedLabelColor = colorPair.second,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = foilTitle,
                        onValueChange = { viewModel.setSimulatorFoilTitle(it) },
                        label = { Text("Título grabado en la tapa") },
                        placeholder = { Text("Ej: Mi Diario Artesanal") },
                        modifier = Modifier.fillMaxWidth().testTag("input_foil_title"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = foilSubtitle,
                        onValueChange = { viewModel.setSimulatorFoilSubtitle(it) },
                        label = { Text("Subtítulo / Autor / Fecha") },
                        placeholder = { Text("Ej: 2026 • Edición Limitada") },
                        modifier = Modifier.fillMaxWidth().testTag("input_foil_subtitle"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 5. HARDWARE & ACCESSORIES
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5. Herrajes & Accesorios",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cinta señaladora de raso",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = hasRibbon,
                            onCheckedChange = { viewModel.setSimulatorHasRibbon(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Esquineros metálicos de protección",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = hasCorners,
                            onCheckedChange = { viewModel.setSimulatorHasCorners(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // BOTTOM ACTIONS: SHARE 3D PROPOSAL OR PASS TO QUOTATION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showShareDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_share_3d_proposal_bottom"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaddleBrown,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir Ficha 3D para Aprobación", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.prepareQuotationFromSimulator() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_proceed_to_quote_from_simulator"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Cotización con este Diseño", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showShareDialog) {
        BookProposalShareDialog(
            spec = exportSpec,
            onDismiss = { showShareDialog = false }
        )
    }
}
