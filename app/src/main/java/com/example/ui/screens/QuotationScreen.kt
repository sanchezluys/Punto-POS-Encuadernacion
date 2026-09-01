package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.SaddleBrown
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.AppNavScreen
import com.example.ui.viewmodel.BookbindingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuotationScreen(
    viewModel: BookbindingViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val quoteBinding by viewModel.quoteBinding.collectAsState()
    val formatSize by viewModel.quoteFormatSize.collectAsState()
    val pageCount by viewModel.quotePageCount.collectAsState()
    val paperType by viewModel.quotePaperType.collectAsState()
    val coverMaterial by viewModel.quoteCoverMaterial.collectAsState()
    val hasRibbon by viewModel.quoteHasRibbon.collectAsState()
    val hasCorners by viewModel.quoteHasCorners.collectAsState()
    val hasElastic by viewModel.quoteHasElastic.collectAsState()
    val hasMarbled by viewModel.quoteHasMarbledEndpapers.collectAsState()
    val hasSlipcase by viewModel.quoteHasSlipcase.collectAsState()
    val hasFoil by viewModel.quoteHasFoil.collectAsState()
    val quantity by viewModel.quoteQuantity.collectAsState()
    val customDiscount by viewModel.quoteCustomDiscount.collectAsState()

    val customerName by viewModel.quoteCustomerName.collectAsState()
    val customerPhone by viewModel.quoteCustomerPhone.collectAsState()
    val customerEmail by viewModel.quoteCustomerEmail.collectAsState()
    val customerNotes by viewModel.quoteCustomerNotes.collectAsState()
    val depositPaid by viewModel.quoteDepositPaid.collectAsState()

    val quoteResult = viewModel.getCalculatedQuote()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quotation_screen_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Cotizador & Presupuestos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Cálculo automático de costos de materiales, mano de obra y descuentos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 1. SELECT BINDING TYPE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Estilo de Encuadernación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(viewModel.bindingTypes, key = { it.id }) { binding ->
                            val isSelected = binding.id == quoteBinding.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setQuoteBinding(binding) },
                                label = { Text(binding.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaddleBrown,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. FORMAT & PAGE COUNT
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Dimensiones y Hojas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Formato del Libro:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    val formats = listOf(
                        "A5 (14.8 x 21 cm)",
                        "A4 (21 x 29.7 cm)",
                        "A6 (10.5 x 14.8 cm)",
                        "Cuadrado 20x20 cm"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        formats.forEach { fmt ->
                            val isFmtSelected = formatSize == fmt
                            FilterChip(
                                selected = isFmtSelected,
                                onClick = { viewModel.setQuoteFormatSize(fmt) },
                                label = { Text(fmt, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cantidad de Páginas:", style = MaterialTheme.typography.bodyMedium)
                        Surface(
                            color = SaddleBrown.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$pageCount págs (${pageCount / 2} hojas)",
                                fontWeight = FontWeight.Bold,
                                color = SaddleBrown,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = pageCount.toFloat(),
                        onValueChange = { viewModel.setQuotePageCount(it.toInt()) },
                        valueRange = 20f..500f,
                        steps = 23,
                        colors = SliderDefaults.colors(
                            thumbColor = SaddleBrown,
                            activeTrackColor = SaddleBrown
                        ),
                        modifier = Modifier.testTag("slider_page_count")
                    )
                }
            }
        }

        // 3. PAPER & COVER MATERIALS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Materiales de Construcción",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tipo de Papel Interior:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    val papers = listOf(
                        "Ahuesado 90g Book Cream",
                        "Kraft Verjurado 120g",
                        "Papel Acuarela 300g",
                        "Bond Ahuesado 80g"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        papers.forEach { p ->
                            FilterChip(
                                selected = paperType == p,
                                onClick = { viewModel.setQuotePaperType(p) },
                                label = { Text(p, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Material de la Cubierta:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    val covers = listOf(
                        "Tela de Lino",
                        "Cuero Vacuno Envejecido",
                        "Papel Marmoleado Florentino",
                        "Cartulina Kraft 300g",
                        "Seda Japonesa Washi"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        covers.forEach { c ->
                            FilterChip(
                                selected = coverMaterial == c,
                                onClick = { viewModel.setQuoteCoverMaterial(c) },
                                label = { Text(c, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 4. EXTRAS & FINISHES
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Acabados y Accesorios",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExtraCheckboxRow("Cinta señaladora de raso (+$1.50)", hasRibbon) { viewModel.setQuoteHasRibbon(it) }
                    ExtraCheckboxRow("Esquineros metálicos de bronce (+$3.20)", hasCorners) { viewModel.setQuoteHasCorners(it) }
                    ExtraCheckboxRow("Cierre con elástico plano (+$2.00)", hasElastic) { viewModel.setQuoteHasElastic(it) }
                    ExtraCheckboxRow("Guardas marmoleadas al agua (+$4.50)", hasMarbled) { viewModel.setQuoteHasMarbledEndpapers(it) }
                    ExtraCheckboxRow("Grabado en Foil Dorado/Plata (+$5.00)", hasFoil) { viewModel.setQuoteHasFoil(it) }
                    ExtraCheckboxRow("Caja contenedora Slipcase a medida (+$12.00)", hasSlipcase) { viewModel.setQuoteHasSlipcase(it) }
                }
            }
        }

        // 5. QUANTITY & DISCOUNTS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5. Cantidad & Descuentos Automáticos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cantidad de Ejemplares:", style = MaterialTheme.typography.bodyMedium)
                            if (quoteResult.volumeDiscountPercent > 0) {
                                Surface(
                                    color = ForestGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = " Descuento por volumen: -${quoteResult.volumeDiscountPercent.toInt()}%",
                                        color = ForestGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Stepper buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.setQuoteQuantity(quantity - 1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Menos")
                            }
                            Text(
                                text = "$quantity",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = { viewModel.setQuoteQuantity(quantity + 1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Más")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom extra discount slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Descuento Especial Adicional:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "${customDiscount.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = customDiscount.toFloat(),
                        onValueChange = { viewModel.setQuoteCustomDiscount(it.toDouble()) },
                        valueRange = 0f..30f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // 6. DETAILED COST BREAKDOWN (COTIZACIÓN AUTOMÁTICA)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Desglose de Presupuesto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${quantity}x $${String.format(java.util.Locale.US, "%.2f", quoteResult.unitPrice)}/ud",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CostLine("Base Encuadernación (${quoteBinding.name})", quoteResult.basePrice)
                    CostLine("Papel interior ($paperType)", quoteResult.paperCost)
                    CostLine("Cubierta ($coverMaterial)", quoteResult.coverMaterialCost)
                    CostLine("Acabados y Herrajes Extras", quoteResult.extrasCost)
                    CostLine("Mano de obra especializada", quoteResult.laborCost)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)

                    CostLine("Subtotal (${quantity} unidades)", quoteResult.subtotal, isBold = false)

                    if (quoteResult.totalDiscountAmount > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Descuentos aplicados (${(quoteResult.volumeDiscountPercent + quoteResult.customDiscountPercent).toInt()}%)",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "-$${String.format(java.util.Locale.US, "%.2f", quoteResult.totalDiscountAmount)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL PRESUPUESTO:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$${String.format(java.util.Locale.US, "%.2f", quoteResult.total)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Material requirements list for workshop
                    Text(
                        text = "Materiales estimados a descontar del stock:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LeatherDark.copy(alpha = 0.8f)
                    )
                    quoteResult.requiredMaterialsList.forEach { mat ->
                        Text(
                            text = "• ${mat.materialName}: ${String.format(java.util.Locale.US, "%.1f", mat.quantityNeeded)} ${mat.unit}",
                            fontSize = 11.sp,
                            color = LeatherDark.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 7. CUSTOMER CONTACT DATA
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "6. Datos del Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { viewModel.setQuoteCustomerName(it) },
                        label = { Text("Nombre Completo / Empresa *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { viewModel.setQuoteCustomerPhone(it) },
                            label = { Text("Teléfono / WhatsApp") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f).testTag("input_customer_phone"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = depositPaid,
                            onValueChange = { viewModel.setQuoteDepositPaid(it) },
                            label = { Text("Anticipo / Seña $") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("input_deposit_paid"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerNotes,
                        onValueChange = { viewModel.setQuoteCustomerNotes(it) },
                        label = { Text("Notas de fabricación o especificaciones especiales") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_notes"),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 8. ACTIONS: SAVE QUOTE OR CONFIRM ORDER
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Confirm Order and send to Workshop
                Button(
                    onClick = {
                        viewModel.saveQuotationOrOrder(asConfirmed = true) { orderId ->
                            scope.launch {
                                snackbarHostState.showSnackbar("¡Pedido confirmado con éxito! Enviado al Taller.")
                                viewModel.navigateTo(AppNavScreen.PEDIDOS)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_confirm_order"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaddleBrown)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar Pedido (Pasa al Taller)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Save as draft Quote
                OutlinedButton(
                    onClick = {
                        viewModel.saveQuotationOrOrder(asConfirmed = false) { orderId ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Cotización guardada exitosamente.")
                                viewModel.navigateTo(AppNavScreen.PEDIDOS)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_quote"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar como Presupuesto / Cotización", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ExtraCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = SaddleBrown)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CostLine(label: String, amount: Double, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$${String.format(java.util.Locale.US, "%.2f", amount)}",
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
