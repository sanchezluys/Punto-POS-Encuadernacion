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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaterialCategory
import com.example.data.model.MaterialItem
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.SaddleBrown
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.BookbindingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryScreen(
    viewModel: BookbindingViewModel,
    modifier: Modifier = Modifier
) {
    val materials by viewModel.allMaterials.collectAsState()
    val lowStockItems by viewModel.lowStockMaterials.collectAsState()
    val selectedCategory by viewModel.selectedMaterialCategory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var materialToEdit by remember { mutableStateOf<MaterialItem?>(null) }
    var materialToDelete by remember { mutableStateOf<MaterialItem?>(null) }

    val filteredMaterials = if (selectedCategory == null) {
        materials
    } else {
        materials.filter { it.category == selectedCategory }
    }

    val totalValuation = materials.sumOf { it.currentStock * it.unitCost }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SaddleBrown,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 75.dp)
                    .testTag("fab_add_material")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Material")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("inventory_screen_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Inventario de Materiales",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Control de stock de papeles, pieles, telas, herrajes e hilos del taller.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Summary Metrics Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Total Insumos",
                        value = "${materials.size}",
                        subtitle = "artículos registrados",
                        modifier = Modifier.weight(1f),
                        badgeColor = SaddleBrown
                    )
                    MetricBox(
                        title = "Alertas Stock",
                        value = "${lowStockItems.size}",
                        subtitle = "por debajo del mín.",
                        modifier = Modifier.weight(1f),
                        badgeColor = if (lowStockItems.isNotEmpty()) Terracotta else ForestGreen
                    )
                    MetricBox(
                        title = "Valor Stock",
                        value = "$${String.format(java.util.Locale.US, "%.0f", totalValuation)}",
                        subtitle = "costo de reposición",
                        modifier = Modifier.weight(1f),
                        badgeColor = GoldenOchre
                    )
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setMaterialCategoryFilter(null) },
                            label = { Text("Todos (${materials.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaddleBrown,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    items(MaterialCategory.values()) { cat ->
                        val count = materials.count { it.category == cat }
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setMaterialCategoryFilter(if (isSelected) null else cat) },
                            label = { Text("${cat.displayName} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaddleBrown,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Materials List
            items(filteredMaterials, key = { it.id }) { item ->
                MaterialCard(
                    material = item,
                    onAdjustStock = { delta -> viewModel.adjustMaterialStock(item.id, delta) },
                    onEdit = { materialToEdit = item },
                    onDelete = { materialToDelete = item }
                )
            }
        }
    }

    // Add / Edit Material Dialog
    if (showAddDialog || materialToEdit != null) {
        MaterialEditDialog(
            initialMaterial = materialToEdit,
            onDismiss = {
                showAddDialog = false
                materialToEdit = null
            },
            onSave = { savedMaterial ->
                viewModel.saveMaterial(savedMaterial)
                showAddDialog = false
                materialToEdit = null
            }
        )
    }

    // Delete confirmation
    materialToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Eliminar Material") },
            text = { Text("¿Deseas eliminar '${item.name}' del inventario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMaterial(item)
                        materialToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = badgeColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
    }
}

@Composable
fun MaterialCard(
    material: MaterialItem,
    onAdjustStock: (Double) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLow = material.isLowStock
    val maxRatio = (material.currentStock / (material.minStockAlert * 3.0)).toFloat().coerceIn(0f, 1f)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("material_card_${material.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = material.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${material.category.displayName} • Costo: $${String.format(java.util.Locale.US, "%.2f", material.unitCost)} / ${material.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                if (isLow) {
                    Surface(
                        color = Terracotta.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stock Bajo", fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (material.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = material.notes,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Bar
            LinearProgressIndicator(
                progress = { maxRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isLow) Terracotta else ForestGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stock Adjusters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stock Actual: ${String.format(java.util.Locale.US, "%.1f", material.currentStock)} ${material.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isLow) Terracotta else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Alerta mín: ${String.format(java.util.Locale.US, "%.1f", material.minStockAlert)} ${material.unit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Minus 1
                    IconButton(
                        onClick = { onAdjustStock(-1.0) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-1", modifier = Modifier.size(16.dp))
                    }

                    // Plus 1
                    IconButton(
                        onClick = { onAdjustStock(1.0) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+1", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialEditDialog(
    initialMaterial: MaterialItem?,
    onDismiss: () -> Unit,
    onSave: (MaterialItem) -> Unit
) {
    var name by remember { mutableStateOf(initialMaterial?.name ?: "") }
    var category by remember { mutableStateOf(initialMaterial?.category ?: MaterialCategory.CUBIERTAS) }
    var unit by remember { mutableStateOf(initialMaterial?.unit ?: "unidades") }
    var unitCostStr by remember { mutableStateOf(initialMaterial?.unitCost?.toString() ?: "1.00") }
    var currentStockStr by remember { mutableStateOf(initialMaterial?.currentStock?.toString() ?: "10.0") }
    var minStockStr by remember { mutableStateOf(initialMaterial?.minStockAlert?.toString() ?: "5.0") }
    var notes by remember { mutableStateOf(initialMaterial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialMaterial == null) "Nuevo Material" else "Editar Material") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Insumo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MaterialCategory.values()) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad (m², pliegos, etc)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitCostStr,
                        onValueChange = { unitCostStr = it },
                        label = { Text("Costo Unitario $") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentStockStr,
                        onValueChange = { currentStockStr = it },
                        label = { Text("Stock Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = { Text("Alerta Mín.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas técnicas / Proveedor") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val item = MaterialItem(
                            id = initialMaterial?.id ?: 0L,
                            name = name,
                            category = category,
                            unit = unit,
                            unitCost = unitCostStr.toDoubleOrNull() ?: 0.0,
                            currentStock = currentStockStr.toDoubleOrNull() ?: 0.0,
                            minStockAlert = minStockStr.toDoubleOrNull() ?: 0.0,
                            notes = notes
                        )
                        onSave(item)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaddleBrown)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
