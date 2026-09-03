package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.WorkshopStep
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.SaddleBrown

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvanceStepDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onConfirmStepChange: (newStep: WorkshopStep, note: String, newStatus: OrderStatus?) -> Unit
) {
    val allSteps = WorkshopStep.values()
    val currentIndex = allSteps.indexOf(order.currentWorkshopStep)
    val defaultNextStep = if (currentIndex < allSteps.size - 1) allSteps[currentIndex + 1] else allSteps.last()

    var selectedStep by remember { mutableStateOf(defaultNextStep) }
    var changeNote by remember { mutableStateOf("") }
    var isStepDropdownExpanded by remember { mutableStateOf(false) }

    val quickNotes = remember(selectedStep) {
        when (selectedStep) {
            WorkshopStep.PREPARACION -> listOf("Plegado a escuadra finalizado", "Cuadernillos prensados 12h", "Corte limpio")
            WorkshopStep.COSTURA -> listOf("Costura artesanal con lino encerado", "Montaje en cintas de refuerzo", "Puntadas firmes")
            WorkshopStep.ENCOLADO_PRENSADO -> listOf("Cola de encuadernar pH neutro aplicada", "Secado en prensa de madera 10kg", "Lomo recto")
            WorkshopStep.CABEZADAS_LOMO -> listOf("Cabezadas bicolor tejidas a mano colocadas", "Tarlatana de refuerzo fijada")
            WorkshopStep.TAPAS_FORRADO -> listOf("Cartón de 2.5mm cortado a medida", "Forrado en tela/piel sin arrugas", "Secando bajo peso")
            WorkshopStep.ESTAMPADO_FOIL -> listOf("Foil térmico pan de oro aplicado", "Grabado de título nítido y centrado")
            WorkshopStep.ENCARTE_FINAL -> listOf("Encarte de guardas marmoleadas", "Encolado de charnelas", "Prensado final 24h")
            WorkshopStep.CONTROL_CALIDAD -> listOf("Inspección de escuadra y apertura 180°", "Empaque protector listo", "Trabajo finalizado")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Handyman,
                    contentDescription = null,
                    tint = SaddleBrown,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registrar Avance en Taller",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pedido: ${order.orderNumber} • ${order.customerName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current step info
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paso actual: ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = order.currentWorkshopStep.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // New Step Selector
                Text(
                    text = "Seleccionar Nuevo Paso del Proceso:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                ExposedDropdownMenuBox(
                    expanded = isStepDropdownExpanded,
                    onExpandedChange = { isStepDropdownExpanded = !isStepDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedStep.stepOrder}. ${selectedStep.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStepDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isStepDropdownExpanded,
                        onDismissRequest = { isStepDropdownExpanded = false }
                    ) {
                        allSteps.forEach { step ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${step.stepOrder}. ${step.displayName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(step.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedStep = step
                                    isStepDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Optional note field
                OutlinedTextField(
                    value = changeNote,
                    onValueChange = { changeNote = it },
                    label = { Text("Nota de este cambio (opcional)") },
                    placeholder = { Text("Ej: Prensado 12h con cola pH neutro...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_step_change_note"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4,
                    leadingIcon = {
                        Icon(Icons.Default.NoteAdd, contentDescription = null, tint = GoldenOchre)
                    }
                )

                // Quick note suggestions
                Text(
                    text = "Sugerencias rápidas de nota:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickNotes.forEach { suggestion ->
                        FilterChip(
                            selected = changeNote == suggestion,
                            onClick = { changeNote = suggestion },
                            label = { Text(suggestion, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newStatus = if (selectedStep == WorkshopStep.CONTROL_CALIDAD) {
                        OrderStatus.TERMINADO
                    } else if (order.status == OrderStatus.COTIZACION || order.status == OrderStatus.CONFIRMADO) {
                        OrderStatus.EN_TALLER
                    } else {
                        null
                    }
                    onConfirmStepChange(selectedStep, changeNote, newStatus)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaddleBrown,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_step_change")
            ) {
                Text("Confirmar Cambio", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
