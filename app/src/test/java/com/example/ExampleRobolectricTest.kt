package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.BookbindingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Encuadernación", appName)
  }

  @Test
  fun `3D simulation fullscreen toggle`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = BookbindingViewModel(context)
    assertFalse(vm.is3DFullscreenActive.value)

    vm.open3DFullscreen()
    assertTrue(vm.is3DFullscreenActive.value)

    vm.close3DFullscreen()
    assertFalse(vm.is3DFullscreenActive.value)
  }

  @Test
  fun `3D camera transform persists when changing color or texture`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = BookbindingViewModel(context)

    // Set custom 3D view angles and zoom
    vm.update3DTransform(yaw = 45f, pitch = 20f, zoom = 1.4f, openAngle = 60f)
    assertEquals(45f, vm.book3DYawDeg.value, 0.01f)
    assertEquals(20f, vm.book3DPitchDeg.value, 0.01f)
    assertEquals(1.4f, vm.book3DZoomScale.value, 0.01f)
    assertEquals(60f, vm.book3DOpenAngleDeg.value, 0.01f)

    // User changes simulator color
    vm.setSimulatorColor(0xFF8C6422)
    assertEquals(0xFF8C6422, vm.simulatorColorHex.value)

    // Camera angles must remain intact
    assertEquals(45f, vm.book3DYawDeg.value, 0.01f)
    assertEquals(20f, vm.book3DPitchDeg.value, 0.01f)
    assertEquals(1.4f, vm.book3DZoomScale.value, 0.01f)
    assertEquals(60f, vm.book3DOpenAngleDeg.value, 0.01f)
  }

  @Test
  fun `sheets per signature dynamic calculation`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = BookbindingViewModel(context)

    vm.setBookSheetCount(80)
    vm.setSheetsPerSignature(4)
    assertEquals(4, vm.sheetsPerSignature.value)
    assertEquals(20, vm.estimatedSignatures.value) // 80 / 4 = 20

    vm.setSheetsPerSignature(8)
    assertEquals(8, vm.sheetsPerSignature.value)
    assertEquals(10, vm.estimatedSignatures.value) // 80 / 8 = 10

    vm.setSheetsPerSignature(3)
    assertEquals(3, vm.sheetsPerSignature.value)
    assertEquals(27, vm.estimatedSignatures.value) // ceil(80 / 3) = 27
  }
}
