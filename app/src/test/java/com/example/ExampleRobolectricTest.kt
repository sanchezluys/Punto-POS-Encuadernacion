package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import coil.ImageLoaderFactory
import com.example.data.model.AppCurrency
import com.example.data.model.CurrencySettings
import com.example.ui.viewmodel.BookbindingViewModel
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
}
