package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import coil.ImageLoaderFactory
import com.example.data.model.AppCurrency
import com.example.data.model.CurrencySettings
import com.example.ui.viewmodel.BookbindingViewModel
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = ArtisanApplication::class)
class ExampleRobolectricTest {

  @Test
  fun `read string from context and verify app identity`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Encuadernación", appName)
    assertTrue("Nombre de app cumple con límite de 30 caracteres para Google Play", appName.length <= 30)
  }

  @Test
  fun `verify ArtisanApplication initializes Coil ImageLoader with caches`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    assertTrue("La aplicación debe ser instancia de ArtisanApplication", app is ArtisanApplication)
    val imageLoaderFactory = app as ImageLoaderFactory
    val imageLoader = imageLoaderFactory.newImageLoader()
    assertNotNull("ImageLoader debe instanciarse correctamente", imageLoader)
    assertNotNull("Debe contar con caché en memoria configurada", imageLoader.memoryCache)
    assertNotNull("Debe contar con caché en disco configurada", imageLoader.diskCache)
  }

  @Test
  fun `verify ViewModel currency formatting with COP, USD and SOL`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BookbindingViewModel(app)

    // Default: COP, con separador de miles, sin decimales
    val formattedCop = viewModel.formatPrice(25000.0)
    assertTrue("Formato COP debe contener el símbolo $ o COP", formattedCop.contains("$") || formattedCop.contains("COP"))
    assertTrue("Formato COP debe usar separador de miles", formattedCop.contains(".") || formattedCop.contains(","))

    // Configuración USD: con decimales
    viewModel.updateCurrency(AppCurrency.USD)
    viewModel.setUseThousandsSeparator(true)
    viewModel.setUseDecimals(true)
    val formattedUsd = viewModel.formatPrice(45.50)
    assertTrue("Formato USD debe contener USD o $", formattedUsd.contains("USD") || formattedUsd.contains("$"))
    assertTrue("Formato USD debe reflejar decimales", formattedUsd.contains("45.50") || formattedUsd.contains("45,50"))

    // Configuración SOL: sin separador de miles, con decimales
    viewModel.updateCurrency(AppCurrency.SOL)
    viewModel.setUseThousandsSeparator(false)
    viewModel.setUseDecimals(true)
    val formattedSol = viewModel.formatPrice(120.00)
    assertTrue("Formato SOL debe contener S/ o SOL", formattedSol.contains("S/") || formattedSol.contains("SOL"))
  }

  @Test
  fun `verify custom bitmap memory downsampling guard`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BookbindingViewModel(app)

    // Large bitmap (e.g. 2000x3000 from high-res camera)
    val largeBitmap = Bitmap.createBitmap(2000, 3000, Bitmap.Config.ARGB_8888)
    viewModel.setSimulatorCustomBitmap(largeBitmap)

    val currentBitmap = viewModel.simulatorCustomBitmap.value
    assertNotNull("El bitmap debe estar presente", currentBitmap)
    assertTrue("El ancho máximo no debe superar 1024px", currentBitmap!!.width <= 1024)
    assertTrue("El alto máximo no debe superar 1024px", currentBitmap.height <= 1024)

    // Clear bitmap
    viewModel.setSimulatorCustomBitmap(null)
    assertNull("El bitmap debe ser null al removerse", viewModel.simulatorCustomBitmap.value)
  }

  @Test
  fun `qa verify spine thickness calculator logic`() {
    // Thin book: 20 sheets of 80g paper
    val thinThickness = com.example.data.model.SpineThicknessCalculator.calculateThicknessMm(
      sheetCount = 20,
      paperGrammageGsm = 80,
      isHardcover = true
    )
    assertTrue("Lomo delgado debe ser mayor a 6mm con tapas duras", thinThickness >= 6.0)

    // Heavy watercolor book: 100 sheets of 300g paper
    val thickWatercolor = com.example.data.model.SpineThicknessCalculator.calculateThicknessMm(
      sheetCount = 100,
      paperGrammageGsm = 300,
      isHardcover = true
    )
    assertTrue("Lomo acuarela grueso debe superar los 40mm", thickWatercolor > 40.0)

    // Bound check: extreme sheets should coerce inside limits
    val extremeThin = com.example.data.model.SpineThicknessCalculator.calculateThicknessMm(1, 80, false)
    assertTrue("Lomo no debe ser inferior a 3mm", extremeThin >= 3.0)
  }

  @Test
  fun `qa verify quote calculator volume discounts and pricing`() {
    val binding = com.example.data.model.PredefinedBindingTypes.list.first()

    // 1 unit: 0% volume discount
    val quote1 = com.example.data.model.QuoteCalculator.calculate(
      bindingType = binding,
      pageCount = 120,
      formatSize = "A5 (14.8 x 21 cm)",
      quantity = 1
    )
    assertEquals(0.0, quote1.volumeDiscountPercent, 0.01)
    assertEquals(quote1.unitPrice, quote1.total, 0.1)

    // 5 units: 10% volume discount
    val quote5 = com.example.data.model.QuoteCalculator.calculate(
      bindingType = binding,
      pageCount = 120,
      formatSize = "A5 (14.8 x 21 cm)",
      quantity = 5
    )
    assertEquals(10.0, quote5.volumeDiscountPercent, 0.01)
    assertTrue("El total con descuento debe ser menor al subtotal", quote5.total < quote5.subtotal)

    // 25 units: 20% volume discount
    val quote25 = com.example.data.model.QuoteCalculator.calculate(
      bindingType = binding,
      pageCount = 120,
      formatSize = "A5 (14.8 x 21 cm)",
      quantity = 25
    )
    assertEquals(20.0, quote25.volumeDiscountPercent, 0.01)
    assertTrue("Debe generar lista de materiales requeridos", quote25.requiredMaterialsList.isNotEmpty())
  }

  @Test
  fun `qa verify navigation stack flow and back navigation`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BookbindingViewModel(app)

    // Start at CATALOGO
    assertEquals(com.example.ui.viewmodel.AppNavScreen.CATALOGO, viewModel.currentScreen.value)

    // Navigate to SIMULADOR, then COTIZADOR, then AJUSTES
    viewModel.navigateTo(com.example.ui.viewmodel.AppNavScreen.SIMULADOR)
    assertEquals(com.example.ui.viewmodel.AppNavScreen.SIMULADOR, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.ui.viewmodel.AppNavScreen.COTIZADOR)
    assertEquals(com.example.ui.viewmodel.AppNavScreen.COTIZADOR, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.ui.viewmodel.AppNavScreen.AJUSTES)
    assertEquals(com.example.ui.viewmodel.AppNavScreen.AJUSTES, viewModel.currentScreen.value)

    // Navigate Back: AJUSTES -> COTIZADOR
    viewModel.navigateBack()
    assertEquals(com.example.ui.viewmodel.AppNavScreen.COTIZADOR, viewModel.currentScreen.value)
  }

  @Test
  fun `qa verify in memory Room database operations`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.db.AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val materialDao = db.materialDao()
    val orderDao = db.orderDao()

    // 1. Insert Material and adjust stock
    val testMaterial = com.example.data.model.MaterialItem(
      id = 1L,
      name = "Piel Cabra Marroquinería",
      category = com.example.data.model.MaterialCategory.CUBIERTAS,
      currentStock = 10.0,
      unit = "dm²",
      unitCost = 25.0,
      minStockAlert = 3.0
    )
    materialDao.insertMaterial(testMaterial)
    assertEquals(1, materialDao.getCount())

    // Stock consumption
    materialDao.adjustStock(1L, -8.0) // 10 - 8 = 2 <= minStockAlert (3.0)
    val lowStockList = materialDao.getLowStockMaterials().first()
    assertEquals(1, lowStockList.size)
    assertEquals("Piel Cabra Marroquinería", lowStockList[0].name)
    assertEquals(2.0, lowStockList[0].currentStock, 0.01)

    // 2. Insert Order and update status
    val testOrder = com.example.data.model.OrderEntity(
      id = 101L,
      orderNumber = "ENC-2026-001",
      customerName = "Biblioteca Nacional",
      bindingTypeId = "tapa_dura",
      bindingTypeName = "Tapa Dura Clásica",
      formatSize = "A4",
      paperType = "Ahuesado 90g",
      pageCount = 240,
      quantity = 2,
      totalAmount = 300.0,
      status = com.example.data.model.OrderStatus.COTIZACION
    )
    val orderId = orderDao.insertOrder(testOrder)
    assertEquals(101L, orderId)
    assertEquals(1, orderDao.getOrderCount())

    // Transition status to EN_TALLER
    val updatedOrder = testOrder.copy(status = com.example.data.model.OrderStatus.EN_TALLER)
    orderDao.updateOrder(updatedOrder)

    val inWorkshopOrders = orderDao.getOrdersByStatus(com.example.data.model.OrderStatus.EN_TALLER).first()
    assertEquals(1, inWorkshopOrders.size)
    assertEquals("Biblioteca Nacional", inWorkshopOrders[0].customerName)

    db.close()
  }

  @Test
  fun `verify MainActivity edge to edge launch and theme initialization`() {
    val activityController = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
    val activity = activityController.setup().get()
    assertNotNull("MainActivity debe inicializarse con enableEdgeToEdge correctamente", activity)
    assertNotNull("Window debe existir", activity.window)
    assertEquals(false, activity.window.isNavigationBarContrastEnforced)
    activityController.destroy()
  }

  @Test
  fun `qa verify PlayInAppUpdateManager flexible and immediate update states`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val fakeManager = com.google.android.play.core.appupdate.testing.FakeAppUpdateManager(context)
    val updateHelper = com.example.update.PlayInAppUpdateManager(fakeManager)

    // 1. No update available -> user-initiated check yields UpToDate
    fakeManager.setUpdateNotAvailable()
    updateHelper.checkForUpdates(userInitiated = true)
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    assertTrue(
      "Cuando no hay actualización debe indicar UpToDate",
      updateHelper.uiState.value is com.example.update.InAppUpdateUiState.UpToDate
    )

    // 2. Standard update available (low priority) -> Flexible non-blocking notification
    fakeManager.setUpdateAvailable(99)
    fakeManager.setUpdatePriority(1)
    updateHelper.checkForUpdates(userInitiated = false)
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    val flexibleState = updateHelper.uiState.value
    assertTrue(
      "Actualización estándar debe mostrar banner flexible",
      flexibleState is com.example.update.InAppUpdateUiState.Available &&
        flexibleState.updateType == com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE &&
        !flexibleState.shouldLaunchImmediately
    )

    // 3. Dismiss banner suppresses auto-prompt for same version, manual check restores it
    updateHelper.dismissBanner(99)
    assertEquals(com.example.update.InAppUpdateUiState.Idle, updateHelper.uiState.value)
    updateHelper.checkForUpdates(userInitiated = false)
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    assertEquals(com.example.update.InAppUpdateUiState.Idle, updateHelper.uiState.value)

    // 4. High priority update (>= 4) -> Immediate update flow
    fakeManager.setUpdateAvailable(100)
    fakeManager.setUpdatePriority(5)
    updateHelper.checkForUpdates(userInitiated = false)
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    val immediateState = updateHelper.uiState.value
    assertTrue(
      "Actualización crítica debe activar flujo inmediato",
      immediateState is com.example.update.InAppUpdateUiState.Available &&
        immediateState.updateType == com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE &&
        immediateState.shouldLaunchImmediately
    )

    updateHelper.dispose()
  }
}
