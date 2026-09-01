package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.BindingType
import com.example.data.model.MaterialCategory
import com.example.data.model.MaterialItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PredefinedBindingTypes
import com.example.data.model.QuoteCalculator
import com.example.data.model.QuoteResult
import com.example.data.model.WorkshopStep
import com.example.data.repository.BookbindingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppNavScreen(val title: String, val iconName: String) {
    CATALOGO("Catálogo 3D", "MenuBook"),
    SIMULADOR("Simulador", "Palette"),
    COTIZADOR("Cotizador", "Calculate"),
    PEDIDOS("Taller / Pedidos", "Inventory2"),
    ENTREGAS("Entregas", "LocalShipping"),
    INVENTARIO("Inventario", "Warehouse")
}

data class TexturePreset(
    val id: String,
    val name: String,
    val description: String,
    val colorHex: Long,
    val drawableResId: Int? = null
)

class BookbindingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookbindingRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = BookbindingRepository(database.orderDao(), database.materialDao())
        
        viewModelScope.launch {
            repository.ensureDefaultMaterials()
        }
    }

    // Navigation
    private val _currentScreen = MutableStateFlow(AppNavScreen.CATALOGO)
    val currentScreen: StateFlow<AppNavScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppNavScreen) {
        _currentScreen.value = screen
    }

    // ==========================================
    // 1. CATALOG & BINDING TYPES
    // ==========================================
    val bindingTypes: List<BindingType> = PredefinedBindingTypes.list

    private val _selectedCatalogBinding = MutableStateFlow(bindingTypes.first())
    val selectedCatalogBinding: StateFlow<BindingType> = _selectedCatalogBinding.asStateFlow()

    fun selectCatalogBinding(binding: BindingType) {
        _selectedCatalogBinding.value = binding
    }

    // ==========================================
    // 2. SIMULATOR STATE (Color, Textures, Camera)
    // ==========================================
    private val _simulatorBinding = MutableStateFlow(bindingTypes.first())
    val simulatorBinding: StateFlow<BindingType> = _simulatorBinding.asStateFlow()

    private val _simulatorColorHex = MutableStateFlow(bindingTypes.first().defaultColorHex)
    val simulatorColorHex: StateFlow<Long> = _simulatorColorHex.asStateFlow()

    private val _simulatorCustomBitmap = MutableStateFlow<Bitmap?>(null)
    val simulatorCustomBitmap: StateFlow<Bitmap?> = _simulatorCustomBitmap.asStateFlow()

    private val _simulatorFoilTitle = MutableStateFlow("OBRA MAESTRA")
    val simulatorFoilTitle: StateFlow<String> = _simulatorFoilTitle.asStateFlow()

    private val _simulatorFoilSubtitle = MutableStateFlow("TALLER ARTESANAL")
    val simulatorFoilSubtitle: StateFlow<String> = _simulatorFoilSubtitle.asStateFlow()

    private val _simulatorFoilColor = MutableStateFlow("Dorado") // Dorado, Plateado, Cobre, Golpe Seco
    val simulatorFoilColor: StateFlow<String> = _simulatorFoilColor.asStateFlow()

    private val _simulatorHasRibbon = MutableStateFlow(true)
    val simulatorHasRibbon: StateFlow<Boolean> = _simulatorHasRibbon.asStateFlow()

    private val _simulatorHasCorners = MutableStateFlow(true)
    val simulatorHasCorners: StateFlow<Boolean> = _simulatorHasCorners.asStateFlow()

    val texturePresets = listOf(
        TexturePreset("cuero_marron", "Cuero Vintage Marrón", "Piel natural curtida", 0xFF4A2A18, R.drawable.tex_cuero_marron),
        TexturePreset("papel_marmol", "Papel Marmoleado", "Florentino artesanal", 0xFF2B4C6F, R.drawable.tex_papel_marmol),
        TexturePreset("lino_esmeralda", "Tela Lino Esmeralda", "Tejido holandés de alta gama", 0xFF2D5A43),
        TexturePreset("cuero_borgona", "Cuero Noble Borgoña", "Piel teñida al alcohol", 0xFF6D213C),
        TexturePreset("algodon_noche", "Algodón Azul Noche", "Lona gruesa encerada", 0xFF1C2D42),
        TexturePreset("cuero_mostaza", "Cuero Ocre Antiguo", "Acabado rústico encerado", 0xFF8C6422),
        TexturePreset("kraft_reciclado", "Cartulina Kraft Cruda", "Textura orgánica 100% fibra", 0xFF7A6B5D)
    )

    fun setSimulatorBinding(binding: BindingType) {
        _simulatorBinding.value = binding
        _simulatorColorHex.value = binding.defaultColorHex
        _simulatorHasRibbon.value = binding.hasRibbon
        _simulatorHasCorners.value = binding.hasCornerGuards
    }

    fun setSimulatorColor(colorHex: Long) {
        _simulatorColorHex.value = colorHex
    }

    fun setSimulatorCustomBitmap(bitmap: Bitmap?) {
        _simulatorCustomBitmap.value = bitmap
    }

    fun applyPresetTexture(preset: TexturePreset) {
        _simulatorColorHex.value = preset.colorHex
        if (preset.drawableResId != null) {
            try {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                val bmp = BitmapFactory.decodeResource(getApplication<Application>().resources, preset.drawableResId, opts)
                _simulatorCustomBitmap.value = bmp
            } catch (e: Exception) {
                _simulatorCustomBitmap.value = null
            }
        } else {
            _simulatorCustomBitmap.value = null
        }
    }

    fun loadBitmapFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                _simulatorCustomBitmap.value = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSimulatorFoilTitle(title: String) {
        _simulatorFoilTitle.value = title
    }

    fun setSimulatorFoilSubtitle(subtitle: String) {
        _simulatorFoilSubtitle.value = subtitle
    }

    fun setSimulatorFoilColor(color: String) {
        _simulatorFoilColor.value = color
    }

    fun setSimulatorHasRibbon(has: Boolean) {
        _simulatorHasRibbon.value = has
    }

    fun setSimulatorHasCorners(has: Boolean) {
        _simulatorHasCorners.value = has
    }

    // ==========================================
    // 3. QUOTATION / PRESUPUESTOS
    // ==========================================
    private val _quoteBinding = MutableStateFlow(bindingTypes.first())
    val quoteBinding: StateFlow<BindingType> = _quoteBinding.asStateFlow()

    private val _quoteFormatSize = MutableStateFlow("A5 (14.8 x 21 cm)")
    val quoteFormatSize: StateFlow<String> = _quoteFormatSize.asStateFlow()

    private val _quotePageCount = MutableStateFlow(120)
    val quotePageCount: StateFlow<Int> = _quotePageCount.asStateFlow()

    private val _quotePaperType = MutableStateFlow("Ahuesado 90g Book Cream")
    val quotePaperType: StateFlow<String> = _quotePaperType.asStateFlow()

    private val _quoteCoverMaterial = MutableStateFlow("Tela de Lino")
    val quoteCoverMaterial: StateFlow<String> = _quoteCoverMaterial.asStateFlow()

    private val _quoteHasRibbon = MutableStateFlow(true)
    val quoteHasRibbon: StateFlow<Boolean> = _quoteHasRibbon.asStateFlow()

    private val _quoteHasCorners = MutableStateFlow(true)
    val quoteHasCorners: StateFlow<Boolean> = _quoteHasCorners.asStateFlow()

    private val _quoteHasElastic = MutableStateFlow(false)
    val quoteHasElastic: StateFlow<Boolean> = _quoteHasElastic.asStateFlow()

    private val _quoteHasMarbledEndpapers = MutableStateFlow(true)
    val quoteHasMarbledEndpapers: StateFlow<Boolean> = _quoteHasMarbledEndpapers.asStateFlow()

    private val _quoteHasSlipcase = MutableStateFlow(false)
    val quoteHasSlipcase: StateFlow<Boolean> = _quoteHasSlipcase.asStateFlow()

    private val _quoteHasFoil = MutableStateFlow(true)
    val quoteHasFoil: StateFlow<Boolean> = _quoteHasFoil.asStateFlow()

    private val _quoteQuantity = MutableStateFlow(1)
    val quoteQuantity: StateFlow<Int> = _quoteQuantity.asStateFlow()

    private val _quoteCustomDiscount = MutableStateFlow(0.0)
    val quoteCustomDiscount: StateFlow<Double> = _quoteCustomDiscount.asStateFlow()

    // Customer Form for Quote / Order
    private val _quoteCustomerName = MutableStateFlow("")
    val quoteCustomerName: StateFlow<String> = _quoteCustomerName.asStateFlow()

    private val _quoteCustomerPhone = MutableStateFlow("")
    val quoteCustomerPhone: StateFlow<String> = _quoteCustomerPhone.asStateFlow()

    private val _quoteCustomerEmail = MutableStateFlow("")
    val quoteCustomerEmail: StateFlow<String> = _quoteCustomerEmail.asStateFlow()

    private val _quoteCustomerNotes = MutableStateFlow("")
    val quoteCustomerNotes: StateFlow<String> = _quoteCustomerNotes.asStateFlow()

    private val _quoteDepositPaid = MutableStateFlow("")
    val quoteDepositPaid: StateFlow<String> = _quoteDepositPaid.asStateFlow()

    fun setQuoteBinding(binding: BindingType) {
        _quoteBinding.value = binding
        _quoteCoverMaterial.value = binding.defaultCoverMaterial
        _quoteHasRibbon.value = binding.hasRibbon
        _quoteHasCorners.value = binding.hasCornerGuards
    }

    fun setQuoteFormatSize(size: String) { _quoteFormatSize.value = size }
    fun setQuotePageCount(pages: Int) { _quotePageCount.value = pages.coerceIn(20, 800) }
    fun setQuotePaperType(paper: String) { _quotePaperType.value = paper }
    fun setQuoteCoverMaterial(material: String) { _quoteCoverMaterial.value = material }
    fun setQuoteHasRibbon(has: Boolean) { _quoteHasRibbon.value = has }
    fun setQuoteHasCorners(has: Boolean) { _quoteHasCorners.value = has }
    fun setQuoteHasElastic(has: Boolean) { _quoteHasElastic.value = has }
    fun setQuoteHasMarbledEndpapers(has: Boolean) { _quoteHasMarbledEndpapers.value = has }
    fun setQuoteHasSlipcase(has: Boolean) { _quoteHasSlipcase.value = has }
    fun setQuoteHasFoil(has: Boolean) { _quoteHasFoil.value = has }
    fun setQuoteQuantity(qty: Int) { _quoteQuantity.value = qty.coerceIn(1, 500) }
    fun setQuoteCustomDiscount(discount: Double) { _quoteCustomDiscount.value = discount.coerceIn(0.0, 50.0) }

    fun setQuoteCustomerName(name: String) { _quoteCustomerName.value = name }
    fun setQuoteCustomerPhone(phone: String) { _quoteCustomerPhone.value = phone }
    fun setQuoteCustomerEmail(email: String) { _quoteCustomerEmail.value = email }
    fun setQuoteCustomerNotes(notes: String) { _quoteCustomerNotes.value = notes }
    fun setQuoteDepositPaid(deposit: String) { _quoteDepositPaid.value = deposit }

    fun getCalculatedQuote(): QuoteResult {
        return QuoteCalculator.calculate(
            bindingType = _quoteBinding.value,
            pageCount = _quotePageCount.value,
            formatSize = _quoteFormatSize.value,
            paperType = _quotePaperType.value,
            coverMaterial = _quoteCoverMaterial.value,
            hasRibbon = _quoteHasRibbon.value,
            hasMetalCorners = _quoteHasCorners.value,
            hasElasticBand = _quoteHasElastic.value,
            hasMarbledEndpapers = _quoteHasMarbledEndpapers.value,
            hasSlipcase = _quoteHasSlipcase.value,
            hasFoil = _quoteHasFoil.value,
            quantity = _quoteQuantity.value,
            customDiscountPercent = _quoteCustomDiscount.value
        )
    }

    fun prepareQuotationFromCatalog(binding: BindingType) {
        setQuoteBinding(binding)
        navigateTo(AppNavScreen.COTIZADOR)
    }

    fun prepareQuotationFromSimulator() {
        setQuoteBinding(_simulatorBinding.value)
        navigateTo(AppNavScreen.COTIZADOR)
    }

    fun saveQuotationOrOrder(asConfirmed: Boolean, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val calc = getCalculatedQuote()
            val now = System.currentTimeMillis()
            val df = SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault())
            val orderNum = "ENC-${df.format(Date(now))}"

            val deposit = _quoteDepositPaid.value.toDoubleOrNull() ?: 0.0
            val balance = (calc.total - deposit).coerceAtLeast(0.0)

            val order = OrderEntity(
                orderNumber = orderNum,
                customerName = if (_quoteCustomerName.value.isNotBlank()) _quoteCustomerName.value else "Cliente Mostrador",
                customerPhone = _quoteCustomerPhone.value,
                customerEmail = _quoteCustomerEmail.value,
                customerNotes = _quoteCustomerNotes.value,
                bindingTypeId = _quoteBinding.value.id,
                bindingTypeName = _quoteBinding.value.name,
                formatSize = _quoteFormatSize.value,
                pageCount = _quotePageCount.value,
                paperType = _quotePaperType.value,
                coverMaterial = _quoteCoverMaterial.value,
                coverColorHex = _simulatorColorHex.value,
                foilTitle = _simulatorFoilTitle.value,
                foilSubtitle = _simulatorFoilSubtitle.value,
                foilColor = _simulatorFoilColor.value,
                hasRibbonBookmark = _quoteHasRibbon.value,
                hasMetalCorners = _quoteHasCorners.value,
                hasElasticBand = _quoteHasElastic.value,
                hasMarbledEndpapers = _quoteHasMarbledEndpapers.value,
                hasSlipcase = _quoteHasSlipcase.value,
                quantity = _quoteQuantity.value,
                materialCost = calc.paperCost + calc.coverMaterialCost,
                laborCost = calc.laborCost,
                extrasCost = calc.extrasCost,
                subtotal = calc.subtotal,
                discountPercent = calc.volumeDiscountPercent + calc.customDiscountPercent,
                discountAmount = calc.totalDiscountAmount,
                totalAmount = calc.total,
                depositPaid = deposit,
                balanceDue = balance,
                status = if (asConfirmed) OrderStatus.CONFIRMADO else OrderStatus.COTIZACION,
                currentWorkshopStep = WorkshopStep.PREPARACION,
                createdAt = now
            )

            val id = if (asConfirmed) {
                repository.confirmOrderAndDeductStock(order)
            } else {
                repository.saveOrder(order)
            }

            // Clear quote form
            _quoteCustomerName.value = ""
            _quoteCustomerPhone.value = ""
            _quoteCustomerEmail.value = ""
            _quoteCustomerNotes.value = ""
            _quoteDepositPaid.value = ""

            onSaved(id)
        }
    }

    // ==========================================
    // 4. ORDERS & WORKSHOP PIPELINE
    // ==========================================
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _orderStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val orderStatusFilter: StateFlow<OrderStatus?> = _orderStatusFilter.asStateFlow()

    private val _selectedOrderDetail = MutableStateFlow<OrderEntity?>(null)
    val selectedOrderDetail: StateFlow<OrderEntity?> = _selectedOrderDetail.asStateFlow()

    fun setOrderStatusFilter(status: OrderStatus?) {
        _orderStatusFilter.value = status
    }

    fun selectOrderForDetail(order: OrderEntity?) {
        _selectedOrderDetail.value = order
    }

    fun updateOrderStatus(order: OrderEntity, newStatus: OrderStatus) {
        viewModelScope.launch {
            val updated = order.copy(
                status = newStatus,
                deliveredAt = if (newStatus == OrderStatus.ENTREGADO) System.currentTimeMillis() else order.deliveredAt
            )
            repository.updateOrder(updated)
            if (_selectedOrderDetail.value?.id == order.id) {
                _selectedOrderDetail.value = updated
            }
        }
    }

    fun advanceWorkshopStep(order: OrderEntity) {
        viewModelScope.launch {
            val steps = WorkshopStep.values()
            val currentIndex = steps.indexOf(order.currentWorkshopStep)
            val nextStep = if (currentIndex < steps.size - 1) {
                steps[currentIndex + 1]
            } else {
                steps.last()
            }

            val newStatus = if (nextStep == WorkshopStep.CONTROL_CALIDAD) {
                OrderStatus.TERMINADO
            } else {
                OrderStatus.EN_TALLER
            }

            val updated = order.copy(
                currentWorkshopStep = nextStep,
                status = if (order.status == OrderStatus.COTIZACION || order.status == OrderStatus.CONFIRMADO) newStatus else order.status
            )
            repository.updateOrder(updated)
            if (_selectedOrderDetail.value?.id == order.id) {
                _selectedOrderDetail.value = updated
            }
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            if (_selectedOrderDetail.value?.id == order.id) {
                _selectedOrderDetail.value = null
            }
        }
    }

    // ==========================================
    // 5. DELIVERY MANAGEMENT (Generar Entrega)
    // ==========================================
    private val _deliveryOrder = MutableStateFlow<OrderEntity?>(null)
    val deliveryOrder: StateFlow<OrderEntity?> = _deliveryOrder.asStateFlow()

    private val _deliveryReceiverName = MutableStateFlow("")
    val deliveryReceiverName: StateFlow<String> = _deliveryReceiverName.asStateFlow()

    private val _deliveryReceiverId = MutableStateFlow("")
    val deliveryReceiverId: StateFlow<String> = _deliveryReceiverId.asStateFlow()

    private val _deliveryNotes = MutableStateFlow("")
    val deliveryNotes: StateFlow<String> = _deliveryNotes.asStateFlow()

    private val _deliverySignaturePoints = MutableStateFlow<List<androidx.compose.ui.geometry.Offset>>(emptyList())
    val deliverySignaturePoints: StateFlow<List<androidx.compose.ui.geometry.Offset>> = _deliverySignaturePoints.asStateFlow()

    fun selectOrderForDelivery(order: OrderEntity) {
        _deliveryOrder.value = order
        _deliveryReceiverName.value = order.customerName
        _deliveryReceiverId.value = order.customerPhone
        _deliveryNotes.value = "Entregado conforme en taller artesanal."
        _deliverySignaturePoints.value = emptyList()
        navigateTo(AppNavScreen.ENTREGAS)
    }

    fun setDeliveryReceiverName(name: String) { _deliveryReceiverName.value = name }
    fun setDeliveryReceiverId(id: String) { _deliveryReceiverId.value = id }
    fun setDeliveryNotes(notes: String) { _deliveryNotes.value = notes }

    fun addSignaturePoint(point: androidx.compose.ui.geometry.Offset) {
        _deliverySignaturePoints.value = _deliverySignaturePoints.value + point
    }

    fun clearSignature() {
        _deliverySignaturePoints.value = emptyList()
    }

    fun confirmDelivery(onDelivered: () -> Unit) {
        val order = _deliveryOrder.value ?: return
        viewModelScope.launch {
            val updated = order.copy(
                status = OrderStatus.ENTREGADO,
                deliveredAt = System.currentTimeMillis(),
                receiverName = _deliveryReceiverName.value,
                receiverDniOrPhone = _deliveryReceiverId.value,
                deliveryNotes = _deliveryNotes.value,
                isDeliverySigned = _deliverySignaturePoints.value.isNotEmpty(),
                balanceDue = 0.0,
                depositPaid = order.totalAmount
            )
            repository.updateOrder(updated)
            _deliveryOrder.value = updated
            onDelivered()
        }
    }

    // ==========================================
    // 6. INVENTORY / MATERIAL MANAGEMENT
    // ==========================================
    val allMaterials: StateFlow<List<MaterialItem>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockMaterials: StateFlow<List<MaterialItem>> = repository.lowStockMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMaterialCategory = MutableStateFlow<MaterialCategory?>(null)
    val selectedMaterialCategory: StateFlow<MaterialCategory?> = _selectedMaterialCategory.asStateFlow()

    fun setMaterialCategoryFilter(category: MaterialCategory?) {
        _selectedMaterialCategory.value = category
    }

    fun adjustMaterialStock(materialId: Long, amount: Double) {
        viewModelScope.launch {
            repository.adjustStock(materialId, amount)
        }
    }

    fun saveMaterial(material: MaterialItem) {
        viewModelScope.launch {
            if (material.id == 0L) {
                repository.saveMaterial(material)
            } else {
                repository.updateMaterial(material)
            }
        }
    }

    fun deleteMaterial(material: MaterialItem) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
        }
    }
}
