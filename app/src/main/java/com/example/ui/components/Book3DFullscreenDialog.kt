package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BindingType
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.LeatherDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Book3DFullscreenDialog(
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
    currentYaw: Float? = null,
    currentPitch: Float? = null,
    currentZoom: Float? = null,
    currentOpenAngle: Float? = null,
    onTransformChanged: ((yaw: Float, pitch: Float, zoom: Float, openAngle: Float) -> Unit)? = null,
    onColorSelected: ((Long) -> Unit)? = null,
    onQuoteClick: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dialog_3d_fullscreen")
        ) {
            Book3DViewer(
                modifier = Modifier.fillMaxSize(),
                bindingType = bindingType,
                coverColor = coverColor,
                customTextureBitmap = customTextureBitmap,
                foilTitle = foilTitle,
                foilSubtitle = foilSubtitle,
                foilColorType = foilColorType,
                hasRibbon = hasRibbon,
                hasCornerGuards = hasCornerGuards,
                showControls = true,
                isFullScreen = true,
                onCloseFullscreen = onDismiss,
                onQuoteClick = onQuoteClick,
                widthCm = widthCm,
                lengthCm = lengthCm,
                spineThicknessMm = spineThicknessMm,
                sheetCount = sheetCount,
                grammageGsm = grammageGsm,
                estimatedSignatures = estimatedSignatures,
                sheetsPerSignature = sheetsPerSignature,
                currentYaw = currentYaw,
                currentPitch = currentPitch,
                currentZoom = currentZoom,
                currentOpenAngle = currentOpenAngle,
                onTransformChanged = onTransformChanged,
                onColorSelected = onColorSelected
            )
        }
    }
}
