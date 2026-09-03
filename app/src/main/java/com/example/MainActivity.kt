package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.ui.components.Book3DViewer
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.DeliveryScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.QuotationScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.theme.ArtisanTheme
import com.example.ui.theme.GoldenOchre
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.SaddleBrown
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.AppNavScreen
import com.example.ui.viewmodel.BookbindingViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BookbindingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtisanTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: BookbindingViewModel) {
    val is3DFullscreenActive by viewModel.is3DFullscreenActive.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val lowStockMaterials by viewModel.lowStockMaterials.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    if (is3DFullscreenActive) {
        BackHandler {
            viewModel.close3DFullscreen()
        }

        val simulatorBinding by viewModel.simulatorBinding.collectAsState()
        val coverColorHex by viewModel.simulatorColorHex.collectAsState()
        val customBitmap by viewModel.simulatorCustomBitmap.collectAsState()
        val foilTitle by viewModel.simulatorFoilTitle.collectAsState()
        val foilSubtitle by viewModel.simulatorFoilSubtitle.collectAsState()
        val foilColor by viewModel.simulatorFoilColor.collectAsState()
        val hasRibbon by viewModel.simulatorHasRibbon.collectAsState()
        val hasCorners by viewModel.simulatorHasCorners.collectAsState()
        val bookWidthCm by viewModel.bookWidthCm.collectAsState()
        val bookLengthCm by viewModel.bookLengthCm.collectAsState()
        val spineThicknessMm by viewModel.calculatedSpineThicknessMm.collectAsState()
        val bookSheetCount by viewModel.bookSheetCount.collectAsState()
        val bookGrammageGsm by viewModel.bookGrammageGsm.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("screen_3d_fullscreen_root")
        ) {
            Book3DViewer(
                modifier = Modifier.fillMaxSize(),
                bindingType = simulatorBinding,
                coverColor = Color(coverColorHex),
                customTextureBitmap = customBitmap,
                foilTitle = foilTitle,
                foilSubtitle = foilSubtitle,
                foilColorType = foilColor,
                hasRibbon = hasRibbon,
                hasCornerGuards = hasCorners,
                showControls = true,
                isFullScreen = true,
                onCloseFullscreen = { viewModel.close3DFullscreen() },
                onQuoteClick = {
                    viewModel.close3DFullscreen()
                    viewModel.navigateTo(AppNavScreen.COTIZADOR)
                },
                widthCm = bookWidthCm,
                lengthCm = bookLengthCm,
                spineThicknessMm = spineThicknessMm,
                sheetCount = bookSheetCount,
                grammageGsm = bookGrammageGsm
            )
        }
    } else {
        val pendingOrdersCount = orders.count { it.status == OrderStatus.EN_TALLER || it.status == OrderStatus.CONFIRMADO }
        val readyToDeliverCount = orders.count { it.status == OrderStatus.TERMINADO }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_app_scaffold"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "Logo",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Encuadernación",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Binding Studio Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        // Quick indicator of active workshop jobs
                        if (pendingOrdersCount > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$pendingOrdersCount en taller",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 0.dp,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    AppNavScreen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        val icon = getScreenIcon(screen)

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = {
                                when (screen) {
                                    AppNavScreen.PEDIDOS -> {
                                        if (pendingOrdersCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ) { Text("$pendingOrdersCount") }
                                                }
                                            ) {
                                                Icon(icon, contentDescription = screen.title)
                                            }
                                        } else {
                                            Icon(icon, contentDescription = screen.title)
                                        }
                                    }
                                    AppNavScreen.ENTREGAS -> {
                                        if (readyToDeliverCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ) { Text("$readyToDeliverCount") }
                                                }
                                            ) {
                                                Icon(icon, contentDescription = screen.title)
                                            }
                                        } else {
                                            Icon(icon, contentDescription = screen.title)
                                        }
                                    }
                                    AppNavScreen.INVENTARIO -> {
                                        if (lowStockMaterials.isNotEmpty()) {
                                            BadgedBox(
                                                badge = { Badge(containerColor = MaterialTheme.colorScheme.error) { Text("!") } }
                                            ) {
                                                Icon(icon, contentDescription = screen.title)
                                            }
                                        } else {
                                            Icon(icon, contentDescription = screen.title)
                                        }
                                    }
                                    else -> {
                                        Icon(icon, contentDescription = screen.title)
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = when (screen) {
                                        AppNavScreen.CATALOGO -> "Catálogo"
                                        AppNavScreen.SIMULADOR -> "Simulador"
                                        AppNavScreen.COTIZADOR -> "Cotizador"
                                        AppNavScreen.PEDIDOS -> "Taller"
                                        AppNavScreen.ENTREGAS -> "Entregas"
                                        AppNavScreen.INVENTARIO -> "Stock"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppNavScreen.CATALOGO -> CatalogScreen(viewModel = viewModel)
                    AppNavScreen.SIMULADOR -> SimulatorScreen(viewModel = viewModel)
                    AppNavScreen.COTIZADOR -> QuotationScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
                    AppNavScreen.PEDIDOS -> OrdersScreen(viewModel = viewModel)
                    AppNavScreen.ENTREGAS -> DeliveryScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
                    AppNavScreen.INVENTARIO -> InventoryScreen(viewModel = viewModel)
                }
            }
        }
    }
}

fun getScreenIcon(screen: AppNavScreen): ImageVector {
    return when (screen) {
        AppNavScreen.CATALOGO -> Icons.Default.MenuBook
        AppNavScreen.SIMULADOR -> Icons.Default.Palette
        AppNavScreen.COTIZADOR -> Icons.Default.Calculate
        AppNavScreen.PEDIDOS -> Icons.Default.Inventory2
        AppNavScreen.ENTREGAS -> Icons.Default.LocalShipping
        AppNavScreen.INVENTARIO -> Icons.Default.Warehouse
    }
}
