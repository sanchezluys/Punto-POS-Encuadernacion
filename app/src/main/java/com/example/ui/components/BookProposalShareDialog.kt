package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.SaddleBrown
import com.example.util.BookProposalImageGenerator
import com.example.util.ProposalExportSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BookProposalShareDialog(
    spec: ProposalExportSpec,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var clientName by remember { mutableStateOf(spec.clientName) }
    var isGenerating by remember { mutableStateOf(true) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isEditingClient by remember { mutableStateOf(false) }

    fun generateImage() {
        scope.launch {
            isGenerating = true
            val updatedSpec = spec.copy(clientName = clientName)
            val bmp = withContext(Dispatchers.Default) {
                BookProposalImageGenerator.generateProposalBitmap(context, updatedSpec)
            }
            generatedBitmap = bmp
            isGenerating = false
        }
    }

    LaunchedEffect(Unit) {
        generateImage()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp)
                .testTag("dialog_share_proposal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ficha Comercial & Modelo 3D",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Imagen de alto impacto optimizada para aprobación del cliente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_share_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Client customization chip / field
                if (isEditingClient) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            label = { Text("Nombre del Cliente") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldenOchre,
                                focusedLabelColor = GoldenOchre
                            )
                        )
                        Button(
                            onClick = {
                                isEditingClient = false
                                generateImage()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaddleBrown)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Aplicar")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (clientName.isNotBlank()) "👤 Cliente: $clientName" else "👤 Cliente General",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = { isEditingClient = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Personalizar Cliente",
                                    modifier = Modifier.size(16.dp),
                                    tint = GoldenOchre
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Image Preview Area with Scroll
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14181C))
                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = GoldenOchre,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Renderizando propuesta comercial 3D en alta definición...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    } else {
                        generatedBitmap?.let { bmp ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Ficha Comercial de la Encuadernación",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Share via System Intent (WhatsApp, Email, Telegram, Drive)
                    Button(
                        onClick = {
                            generatedBitmap?.let { bmp ->
                                val uri = BookProposalImageGenerator.saveProposalImageToCache(context, bmp)
                                if (uri != null) {
                                    val updatedSpec = spec.copy(clientName = clientName)
                                    BookProposalImageGenerator.shareProposal(context, uri, updatedSpec)
                                } else {
                                    Toast.makeText(context, "No se pudo generar el enlace para compartir", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("btn_share_proposal_action"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaddleBrown),
                        enabled = !isGenerating && generatedBitmap != null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", fontWeight = FontWeight.Bold)
                    }

                    // 2. Save to Gallery
                    OutlinedButton(
                        onClick = {
                            generatedBitmap?.let { bmp ->
                                BookProposalImageGenerator.saveToGallery(context, bmp)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_save_proposal_gallery"),
                        enabled = !isGenerating && generatedBitmap != null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar", fontSize = 13.sp)
                    }

                    // 3. Copy Text Summary
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val summary = buildString {
                                append("PROPUESTA DE ENCUADERNACIÓN ARTESANAL\n")
                                append("Modelo: ${spec.bindingType.name} (${spec.bindingType.category})\n")
                                append("Formato: ${spec.widthCm} x ${spec.lengthCm} cm (Lomo: ${String.format(java.util.Locale.US, "%.1f", spec.spineThicknessMm)} mm)\n")
                                append("Hojas: ${spec.sheetCount} (${spec.pageCount} páginas) - ${spec.paperType}\n")
                                append("Cubiertas: ${spec.coverMaterial} con Foil ${spec.foilColorType}\n")
                                append("Presupuesto: $${String.format(java.util.Locale.US, "%.2f", spec.quoteResult.total)} (${spec.quoteResult.quantity} ud.)\n")
                            }
                            clipboard.setPrimaryClip(ClipData.newPlainText("Cotización", summary))
                            Toast.makeText(context, "Resumen copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .testTag("btn_copy_proposal_text"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
