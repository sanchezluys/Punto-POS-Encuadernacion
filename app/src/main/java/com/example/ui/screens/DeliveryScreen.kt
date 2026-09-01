package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.SaddleBrown
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.AppNavScreen
import com.example.ui.viewmodel.BookbindingViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeliveryScreen(
    viewModel: BookbindingViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val allOrders by viewModel.allOrders.collectAsState()
    val deliveryOrder by viewModel.deliveryOrder.collectAsState()
    val receiverName by viewModel.deliveryReceiverName.collectAsState()
    val receiverId by viewModel.deliveryReceiverId.collectAsState()
    val deliveryNotes by viewModel.deliveryNotes.collectAsState()
    val signaturePoints by viewModel.deliverySignaturePoints.collectAsState()

    var checkVisualInspection by remember { mutableStateOf(true) }
    var checkOpeningAngles by remember { mutableStateOf(true) }
    var checkProtectivePackaging by remember { mutableStateOf(true) }
    var checkBalanceSettled by remember { mutableStateOf(true) }

    val readyOrders = allOrders.filter { it.status == OrderStatus.TERMINADO || it.status == OrderStatus.EN_TALLER || it.status == OrderStatus.CONFIRMADO }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("delivery_screen_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Generador de Entregas & Remitos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Emisión de comprobante de entrega, firma digital conforme y cierre de pedido.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // If no order selected, show ready orders to select
        if (deliveryOrder == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Selecciona un Pedido para Entregar:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (readyOrders.isEmpty()) {
                            Text(
                                text = "No hay pedidos pendientes de entrega actualmente.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            readyOrders.forEach { ord ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.selectOrderForDelivery(ord) },
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${ord.orderNumber} • ${ord.customerName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "${ord.quantity}x ${ord.bindingTypeName} • $${String.format(java.util.Locale.US, "%.2f", ord.totalAmount)}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SaddleBrown)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val order = deliveryOrder!!
            val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            // OFFICIAL DELIVERY RECEIPT / REMITO CARD
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_receipt_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Header of the receipt
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "REMITO DE ENTREGA",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaddleBrown
                                )
                                Text(
                                    text = "Taller de Encuadernación Artesanal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = "QR Código",
                                modifier = Modifier.size(44.dp),
                                tint = SaddleBrown
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SaddleBrown.copy(alpha = 0.3f))

                        // Key-Value specifications
                        ReceiptRow("Comprobante N°:", order.orderNumber)
                        ReceiptRow("Fecha Emisión:", df.format(Date()))
                        ReceiptRow("Cliente:", order.customerName)
                        if (order.customerPhone.isNotBlank()) ReceiptRow("Contacto:", order.customerPhone)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detalle del Trabajo Realizado:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SaddleBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ReceiptRow("Modelo:", "${order.quantity}x ${order.bindingTypeName}")
                        ReceiptRow("Formato:", order.formatSize)
                        ReceiptRow("Páginas:", "${order.pageCount} páginas")
                        ReceiptRow("Papel & Cubierta:", "${order.paperType} / ${order.coverMaterial}")
                        if (order.foilTitle.isNotBlank()) ReceiptRow("Grabado en Tapa:", "\"${order.foilTitle}\" (${order.foilColor})")

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SaddleBrown.copy(alpha = 0.2f))

                        // Financial balance
                        ReceiptRow("Importe Total:", "$${String.format(java.util.Locale.US, "%.2f", order.totalAmount)}")
                        ReceiptRow("Anticipo abonado:", "$${String.format(java.util.Locale.US, "%.2f", order.depositPaid)}")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Saldo a Cancelar:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "$${String.format(java.util.Locale.US, "%.2f", order.balanceDue)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (order.balanceDue > 0) Terracotta else ForestGreen
                            )
                        }
                    }
                }
            }

            // RECEIVER DETAILS FORM
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Datos de Quien Recibe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = receiverName,
                            onValueChange = { viewModel.setDeliveryReceiverName(it) },
                            label = { Text("Nombre y Apellido *") },
                            modifier = Modifier.fillMaxWidth().testTag("input_receiver_name"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = receiverId,
                            onValueChange = { viewModel.setDeliveryReceiverId(it) },
                            label = { Text("DNI / Cédula / Teléfono") },
                            modifier = Modifier.fillMaxWidth().testTag("input_receiver_id"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = deliveryNotes,
                            onValueChange = { viewModel.setDeliveryNotes(it) },
                            label = { Text("Observaciones de entrega") },
                            modifier = Modifier.fillMaxWidth().testTag("input_delivery_notes"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // DELIVERY CHECKLIST
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Control de Calidad & Entrega",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ChecklistRow("Inspección visual y encuadre conforme", checkVisualInspection) { checkVisualInspection = it }
                        ChecklistRow("Apertura de hojas y costura verificada", checkOpeningAngles) { checkOpeningAngles = it }
                        ChecklistRow("Empaque protector / estuche entregado", checkProtectivePackaging) { checkProtectivePackaging = it }
                        ChecklistRow("Saldo financiero cancelado en su totalidad", checkBalanceSettled) { checkBalanceSettled = it }
                    }
                }
            }

            // DIGITAL SIGNATURE CANVAS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Draw, contentDescription = null, tint = SaddleBrown)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Firma Digital de Conformidad",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (signaturePoints.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSignature() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Borrar firma", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Text(
                            text = "Firma en el recuadro blanco con el dedo:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Touch Canvas for Signature
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            viewModel.addSignaturePoint(offset)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            viewModel.addSignaturePoint(change.position)
                                        }
                                    )
                                }
                                .testTag("signature_canvas")
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (signaturePoints.size > 1) {
                                    val path = Path()
                                    path.moveTo(signaturePoints.first().x, signaturePoints.first().y)
                                    for (pt in signaturePoints) {
                                        path.lineTo(pt.x, pt.y)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color(0xFF1E1510),
                                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }

                            if (signaturePoints.isEmpty()) {
                                Text(
                                    text = "Firmar aquí ✍",
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }

            // CONFIRM DELIVERY ACTION
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.confirmDelivery {
                                scope.launch {
                                    snackbarHostState.showSnackbar("¡Entrega registrada y pedido finalizado con éxito!")
                                    viewModel.navigateTo(AppNavScreen.PEDIDOS)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_confirm_delivery"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Entrega y Cerrar Pedido", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            // Switch or change selected delivery order
                            viewModel.selectOrderForDetail(order)
                            viewModel.navigateTo(AppNavScreen.PEDIDOS)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Volver al Listado de Pedidos")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ChecklistRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
        )
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
