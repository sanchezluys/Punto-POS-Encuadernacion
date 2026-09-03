package com.example.ui.components

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BindingType
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PredefinedBindingTypes
import com.example.data.model.PredefinedBookFormats
import com.example.data.model.PredefinedPapers
import com.example.data.model.QuoteCalculator
import com.example.data.model.WorkshopStep
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.SaddleBrown
import com.example.ui.theme.Terracotta
import com.example.util.ContactPickerHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewOrderDialog(
    initialBinding: BindingType,
    bindingTypes: List<BindingType>,
    onDismiss: () -> Unit,
    onSaveOrder: (OrderEntity) -> Unit
) {
    val context = LocalContext.current

    // Customer fields
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerNotes by remember { mutableStateOf("") }

    // Binding specs
    var selectedBinding by remember { mutableStateOf(initialBinding) }
    var isBindingDropdownExpanded by remember { mutableStateOf(false) }

    var selectedFormat by remember { mutableStateOf("A5 (14.8 x 21.0 cm)") }
    var isFormatDropdownExpanded by remember { mutableStateOf(false) }

    var pageCount by remember { mutableIntStateOf(120) }
    var selectedPaper by remember { mutableStateOf("Ahuesado 90g Book Cream") }
    var isPaperDropdownExpanded by remember { mutableStateOf(false) }

    var coverMaterial by remember { mutableStateOf(selectedBinding.defaultCoverMaterial) }
    var coverColorHex by remember { mutableLongStateOf(selectedBinding.defaultColorHex) }

    // Foil
    var hasFoil by remember { mutableStateOf(true) }
    var foilTitle by remember { mutableStateOf("") }
    var foilSubtitle by remember { mutableStateOf("") }
    var foilColor by remember { mutableStateOf("Oro") }

    // Extras
    var hasRibbon by remember { mutableStateOf(selectedBinding.hasRibbon) }
    var hasCorners by remember { mutableStateOf(selectedBinding.hasCornerGuards) }
    var hasElastic by remember { mutableStateOf(false) }
    var hasEndpapers by remember { mutableStateOf(true) }
    var hasSlipcase by remember { mutableStateOf(false) }

    // Quantity & Pricing
    var quantity by remember { mutableIntStateOf(1) }
    var depositPaidText by remember { mutableStateOf("") }
    var estimatedDays by remember { mutableIntStateOf(5) }
    var initialStepNote by remember { mutableStateOf("Ingreso de pedido al taller. Iniciar fase de plegado.") }

    // Auto-calculate total price
    val calculatedQuote = remember(
        selectedBinding, selectedFormat, pageCount, selectedPaper,
        coverMaterial, hasRibbon, hasCorners, hasElastic,
        hasEndpapers, hasSlipcase, hasFoil, quantity
    ) {
        QuoteCalculator.calculate(
            bindingType = selectedBinding,
            pageCount = pageCount,
            formatSize = selectedFormat,
            paperType = selectedPaper,
            coverMaterial = coverMaterial,
            hasRibbon = hasRibbon,
            hasMetalCorners = hasCorners,
            hasElasticBand = hasElastic,
            hasMarbledEndpapers = hasEndpapers,
            hasSlipcase = hasSlipcase,
            hasFoil = hasFoil,
            quantity = quantity,
            customDiscountPercent = 0.0
        )
    }

    val depositPaid = depositPaidText.toDoubleOrNull() ?: 0.0
    val balanceDue = (calculatedQuote.total - depositPaid).coerceAtLeast(0.0)

    // Contact Picker Launcher from System Address Book
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            val contactInfo = ContactPickerHelper.extractContactInfo(context, contactUri)
            if (contactInfo.name.isNotBlank()) {
                customerName = contactInfo.name
            }
            if (contactInfo.phone.isNotBlank()) {
                customerPhone = contactInfo.phone
            }
            if (contactInfo.email.isNotBlank()) {
                customerEmail = contactInfo.email
            }
            Toast.makeText(
                context,
                "Contacto cargado: ${contactInfo.name.ifBlank { "Cliente" }}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ingreso de Nuevo Pedido",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Conexión con agenda de contactos y proceso de taller",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_new_order_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Scrollable Form
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // SECTION 1: DATOS DEL CLIENTE + AGENDA DEL TELÉFONO
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "1. Datos del Cliente",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SaddleBrown
                                    )

                                    // Contact Picker Button
                                    Button(
                                        onClick = { contactPickerLauncher.launch(null) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldenOchre,
                                            contentColor = Color.Black
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_pick_contact_agenda")
                                    ) {
                                        Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Seleccionar de Agenda", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = { customerName = it },
                                    label = { Text("Nombre y Apellido *") },
                                    placeholder = { Text("Ej: María Gómez") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_order_customer_name"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customerPhone,
                                        onValueChange = { customerPhone = it },
                                        label = { Text("Teléfono / WhatsApp") },
                                        placeholder = { Text("+54 9 11 ...") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_order_customer_phone"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = customerEmail,
                                        onValueChange = { customerEmail = it },
                                        label = { Text("Email") },
                                        placeholder = { Text("cliente@correo.com") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_order_customer_email"),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = customerNotes,
                                    onValueChange = { customerNotes = it },
                                    label = { Text("Notas o Requerimientos Especiales") },
                                    placeholder = { Text("Dedicatoria en portada, papel con hojas punteadas, etc.") },
                                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    minLines = 2
                                )
                            }
                        }
                    }

                    // SECTION 2: TIPO DE ENCUADERNACIÓN & FORMATO
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "2. Tipo de Encuadernación & Medidas",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SaddleBrown
                                )

                                // Binding Type Selector Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = isBindingDropdownExpanded,
                                    onExpandedChange = { isBindingDropdownExpanded = !isBindingDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = "${selectedBinding.name} (${if (selectedBinding.spineType.isExposed) "Lomo Expuesto" else "Lomo Cubierto"})",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Tipo de Encuadernación") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBindingDropdownExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = isBindingDropdownExpanded,
                                        onDismissRequest = { isBindingDropdownExpanded = false }
                                    ) {
                                        bindingTypes.forEach { bt ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(bt.defaultColorHex))
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(bt.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            Text(
                                                                "${bt.category} • ${if (bt.spineType.isExposed) "Lomo Descubierto" else "Lomo Cubierto"}",
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    selectedBinding = bt
                                                    coverMaterial = bt.defaultCoverMaterial
                                                    coverColorHex = bt.defaultColorHex
                                                    hasRibbon = bt.hasRibbon
                                                    hasCorners = bt.hasCornerGuards
                                                    isBindingDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Format Selector Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = isFormatDropdownExpanded,
                                    onExpandedChange = { isFormatDropdownExpanded = !isFormatDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedFormat,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Formato de Tamaño") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFormatDropdownExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = isFormatDropdownExpanded,
                                        onDismissRequest = { isFormatDropdownExpanded = false }
                                    ) {
                                        PredefinedBookFormats.list.forEach { fmt ->
                                            DropdownMenuItem(
                                                text = { Text("${fmt.name} (${fmt.widthCm} x ${fmt.lengthCm} cm)") },
                                                onClick = {
                                                    selectedFormat = "${fmt.name} (${fmt.widthCm} x ${fmt.lengthCm} cm)"
                                                    isFormatDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Page count slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Número de Páginas:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("$pageCount páginas (${pageCount / 2} hojas)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SaddleBrown)
                                }
                                Slider(
                                    value = pageCount.toFloat(),
                                    onValueChange = { pageCount = ((it / 8).toInt() * 8).coerceIn(32, 400) },
                                    valueRange = 32f..400f,
                                    steps = 45,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Paper Selector Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = isPaperDropdownExpanded,
                                    onExpandedChange = { isPaperDropdownExpanded = !isPaperDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedPaper,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Papel Interior") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPaperDropdownExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = isPaperDropdownExpanded,
                                        onDismissRequest = { isPaperDropdownExpanded = false }
                                    ) {
                                        PredefinedPapers.list.forEach { p ->
                                            DropdownMenuItem(
                                                text = { Text("${p.name} (${p.grammageGsm}g)") },
                                                onClick = {
                                                    selectedPaper = "${p.name} ${p.grammageGsm}g"
                                                    isPaperDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: ACABADOS & FOIL
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "3. Acabados, Cubierta & Estampado",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SaddleBrown
                                )

                                OutlinedTextField(
                                    value = coverMaterial,
                                    onValueChange = { coverMaterial = it },
                                    label = { Text("Material de Cubierta") },
                                    placeholder = { Text("Ej: Tela de Lino Natural, Cuero Vacuno, etc.") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                // Quick Color Palette
                                Text("Color de Tapas:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                val classicColors = listOf(
                                    0xFF2D5A43, // Emerald Green
                                    0xFF5C3317, // Saddle Brown
                                    0xFF1A2B4C, // Deep Navy
                                    0xFF8B2500, // Rust Terracotta
                                    0xFF1C1C1C, // Charcoal Black
                                    0xFF800020, // Burgundy
                                    0xFFC2B280  // Ecru / Lino Crudo
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    classicColors.forEach { colorHex ->
                                        val isSelected = coverColorHex == colorHex
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(colorHex))
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) GoldenOchre else Color.Gray.copy(alpha = 0.5f),
                                                    shape = CircleShape
                                                )
                                                .clickable { coverColorHex = colorHex }
                                        )
                                    }
                                }

                                // Foil Stamping Title
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Grabado en Hot Stamping / Pan de Oro", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Switch(checked = hasFoil, onCheckedChange = { hasFoil = it })
                                }

                                if (hasFoil) {
                                    OutlinedTextField(
                                        value = foilTitle,
                                        onValueChange = { foilTitle = it },
                                        label = { Text("Título en Portada (Foil)") },
                                        placeholder = { Text("Ej: DIARIO DE CAMPO") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("Oro", "Plata", "Cobre", "Seco").forEach { fColor ->
                                            FilterChip(
                                                selected = foilColor == fColor,
                                                onClick = { foilColor = fColor },
                                                label = { Text(fColor) }
                                            )
                                        }
                                    }
                                }

                                // Extras Toggles
                                Text("Accesorios & Extras:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = hasRibbon,
                                        onClick = { hasRibbon = !hasRibbon },
                                        label = { Text("Cinta Registro") }
                                    )
                                    FilterChip(
                                        selected = hasCorners,
                                        onClick = { hasCorners = !hasCorners },
                                        label = { Text("Esquineros Metálicos") }
                                    )
                                    FilterChip(
                                        selected = hasEndpapers,
                                        onClick = { hasEndpapers = !hasEndpapers },
                                        label = { Text("Guardas Marmoleadas") }
                                    )
                                    FilterChip(
                                        selected = hasSlipcase,
                                        onClick = { hasSlipcase = !hasSlipcase },
                                        label = { Text("Estuche a Medida") }
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 4: PRECIO, SEÑA & NOTA INICIAL DE TALLER
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "4. Valores, Seña & Planificación",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SaddleBrown
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Cantidad a Confeccionar:")
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { if (quantity > 1) quantity-- },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        Text("$quantity un.", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                        IconButton(
                                            onClick = { quantity++ },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Presupuestado:", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", calculatedQuote.total)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = SaddleBrown
                                    )
                                }

                                OutlinedTextField(
                                    value = depositPaidText,
                                    onValueChange = { depositPaidText = it },
                                    label = { Text("Seña / Anticipo Pagado ($)") },
                                    placeholder = { Text("Ej: 20.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_order_deposit"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                if (depositPaid > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Saldo a contraentrega:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            "$${String.format(Locale.US, "%.2f", balanceDue)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (balanceDue > 0) Terracotta else ForestGreen
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Plazo de Entrega en Taller:", fontSize = 12.sp)
                                    Text("$estimatedDays días hábiles", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Slider(
                                    value = estimatedDays.toFloat(),
                                    onValueChange = { estimatedDays = it.toInt() },
                                    valueRange = 2f..20f,
                                    steps = 18,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Initial step note
                                OutlinedTextField(
                                    value = initialStepNote,
                                    onValueChange = { initialStepNote = it },
                                    label = { Text("Nota inicial del proceso de encuadernación (opcional)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_initial_step_note"),
                                    shape = RoundedCornerShape(10.dp),
                                    minLines = 2
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            val df = SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault())
                            val orderNumber = "ENC-${df.format(Date(now))}"

                            val initialOrder = OrderEntity(
                                orderNumber = orderNumber,
                                customerName = if (customerName.isNotBlank()) customerName.trim() else "Cliente Taller",
                                customerPhone = customerPhone.trim(),
                                customerEmail = customerEmail.trim(),
                                customerNotes = customerNotes.trim(),
                                bindingTypeId = selectedBinding.id,
                                bindingTypeName = selectedBinding.name,
                                formatSize = selectedFormat,
                                pageCount = pageCount,
                                paperType = selectedPaper,
                                coverMaterial = coverMaterial,
                                coverColorHex = coverColorHex,
                                foilTitle = if (hasFoil) foilTitle.trim() else "",
                                foilSubtitle = if (hasFoil) foilSubtitle.trim() else "",
                                foilColor = if (hasFoil) foilColor else "Sin Foil",
                                hasRibbonBookmark = hasRibbon,
                                hasMetalCorners = hasCorners,
                                hasElasticBand = hasElastic,
                                hasMarbledEndpapers = hasEndpapers,
                                hasSlipcase = hasSlipcase,
                                quantity = quantity,
                                materialCost = calculatedQuote.paperCost + calculatedQuote.coverMaterialCost,
                                laborCost = calculatedQuote.laborCost,
                                extrasCost = calculatedQuote.extrasCost,
                                subtotal = calculatedQuote.subtotal,
                                discountPercent = 0.0,
                                discountAmount = 0.0,
                                totalAmount = calculatedQuote.total,
                                depositPaid = depositPaid,
                                balanceDue = balanceDue,
                                status = OrderStatus.CONFIRMADO,
                                currentWorkshopStep = WorkshopStep.PREPARACION,
                                createdAt = now,
                                estimatedDeliveryDate = now + (estimatedDays * 24 * 60 * 60 * 1000L)
                            ).withAddedStepLog(
                                stepName = WorkshopStep.PREPARACION.displayName,
                                statusName = OrderStatus.CONFIRMADO.displayName,
                                note = initialStepNote
                            )

                            onSaveOrder(initialOrder)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("btn_save_new_order"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear Pedido", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
