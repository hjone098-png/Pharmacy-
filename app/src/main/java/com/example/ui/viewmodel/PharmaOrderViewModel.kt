package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.Medication
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItem
import com.example.data.local.PharmacyProfile
import com.example.data.repository.MedicationRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.PharmacyRepository
import com.example.ui.navigation.Screen
import com.example.ui.util.formatIraqiDinar
import com.example.ui.util.formatOrderSlip
import com.example.ui.util.formatPrice
import com.example.ui.util.toArabicDigits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class InventoryFilter {
    ALL,
    LOW_STOCK,
    OUT_OF_STOCK
}

class PharmaOrderViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val pharmacyRepository = PharmacyRepository(database.pharmacyDao())
    private val medicationRepository = MedicationRepository(database.medicationDao())
    private val orderRepository = OrderRepository(database.orderDao())

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.OrderCreation)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Pharmacy Profile
    val pharmacyProfile: StateFlow<PharmacyProfile?> = pharmacyRepository.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Search and Category for Order Selection
    private val _orderSearchQuery = MutableStateFlow("")
    val orderSearchQuery: StateFlow<String> = _orderSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Inventory Search & Filter
    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private val _inventoryFilter = MutableStateFlow(InventoryFilter.ALL)
    val inventoryFilter: StateFlow<InventoryFilter> = _inventoryFilter.asStateFlow()

    // All Medications
    val allMedications: StateFlow<List<Medication>> = medicationRepository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Medication Sort Options (وقت الإضافة / ترتيب الإضافة / حسب الأحرف الأبجدية)
    private val _medicationSortOrder = MutableStateFlow(MedicationSortOrder.TIME_ADDED)
    val medicationSortOrder: StateFlow<MedicationSortOrder> = _medicationSortOrder.asStateFlow()

    private val _sortAscending = MutableStateFlow(false)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    fun setSortOrder(order: MedicationSortOrder) {
        if (_medicationSortOrder.value == order) {
            _sortAscending.value = !_sortAscending.value
        } else {
            _medicationSortOrder.value = order
            // When switching to alphabetical, default to A-Z (ascending)
            // When switching to time or addition order, default to newest first (descending)
            _sortAscending.value = (order == MedicationSortOrder.ALPHABETICAL)
        }
    }

    fun toggleSortDirection() {
        _sortAscending.value = !_sortAscending.value
    }

    private fun sortMedications(
        list: List<Medication>,
        sortOrder: MedicationSortOrder,
        ascending: Boolean
    ): List<Medication> {
        val collator = java.text.Collator.getInstance(java.util.Locale("ar"))
        return when (sortOrder) {
            MedicationSortOrder.TIME_ADDED -> {
                if (ascending) list.sortedBy { it.updatedAt }
                else list.sortedByDescending { it.updatedAt }
            }
            MedicationSortOrder.ADDITION_ORDER -> {
                if (ascending) list.sortedBy { it.id }
                else list.sortedByDescending { it.id }
            }
            MedicationSortOrder.ALPHABETICAL -> {
                if (ascending) list.sortedWith { a, b -> collator.compare(a.tradeName, b.tradeName) }
                else list.sortedWith { a, b -> collator.compare(b.tradeName, a.tradeName) }
            }
        }
    }

    // Filtered Medications for Order Catalog with Sorting
    val catalogMedications: StateFlow<List<Medication>> = combine(
        allMedications,
        _orderSearchQuery,
        _selectedCategory,
        _medicationSortOrder,
        _sortAscending
    ) { list, query, category, sortOrder, ascending ->
        val filtered = list.filter { med ->
            val matchesCategory = (category == "الكل" || med.category == category)
            val matchesQuery = query.isBlank() ||
                    med.tradeName.contains(query, ignoreCase = true) ||
                    med.scientificName.contains(query, ignoreCase = true) ||
                    med.category.contains(query, ignoreCase = true) ||
                    med.dosageForm.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
        sortMedications(filtered, sortOrder, ascending)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Medications for Inventory Management with Sorting
    val inventoryMedications: StateFlow<List<Medication>> = combine(
        allMedications,
        _inventorySearchQuery,
        _inventoryFilter,
        _medicationSortOrder,
        _sortAscending
    ) { list, query, filter, sortOrder, ascending ->
        val filtered = list.filter { med ->
            val matchesQuery = query.isBlank() ||
                    med.tradeName.contains(query, ignoreCase = true) ||
                    med.scientificName.contains(query, ignoreCase = true) ||
                    med.category.contains(query, ignoreCase = true) ||
                    med.manufacturer.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                InventoryFilter.ALL -> true
                InventoryFilter.LOW_STOCK -> med.isLowStock
                InventoryFilter.OUT_OF_STOCK -> med.isOutOfStock
            }
            matchesQuery && matchesFilter
        }
        sortMedications(filtered, sortOrder, ascending)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Order Cart: Map of MedicationId -> OrderItem
    private val _cartItems = MutableStateFlow<Map<Long, OrderItem>>(emptyMap())
    val cartItems: StateFlow<Map<Long, OrderItem>> = _cartItems.asStateFlow()

    // Pharmacy Name and Location for Current Order
    private val _currentOrderPharmacyName = MutableStateFlow("")
    val currentOrderPharmacyName: StateFlow<String> = _currentOrderPharmacyName.asStateFlow()

    private val _currentOrderPharmacyLocation = MutableStateFlow("")
    val currentOrderPharmacyLocation: StateFlow<String> = _currentOrderPharmacyLocation.asStateFlow()

    // Drug Depot / Warehouse (المذخر الموجه إليه الطلب)
    private val _orderDepotName = MutableStateFlow("مذخر سبأ")
    val orderDepotName: StateFlow<String> = _orderDepotName.asStateFlow()

    // Order Meta State
    private val _orderUrgency = MutableStateFlow("عادي")
    val orderUrgency: StateFlow<String> = _orderUrgency.asStateFlow()

    private val _orderSupplier = MutableStateFlow("مذخر سبأ")
    val orderSupplier: StateFlow<String> = _orderSupplier.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    // Last Generated Order (for summary view)
    private val _lastGeneratedOrder = MutableStateFlow<OrderEntity?>(null)
    val lastGeneratedOrder: StateFlow<OrderEntity?> = _lastGeneratedOrder.asStateFlow()

    // Order History
    val orderHistory: StateFlow<List<OrderEntity>> = orderRepository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // App initializes with the medications specified by the user
        viewModelScope.launch {
            if (medicationRepository.getCount() == 0) {
                val defaultList = listOf(
                    Medication(
                        tradeName = "جنى شراب",
                        scientificName = "Herbal Cough & Throat Syrup",
                        category = "شراب وأدوية سائلة",
                        dosageForm = "شراب",
                        packageUnit = "علبة",
                        unitPrice = 1500.0,
                        currentStock = 50,
                        minStockAlert = 10
                    ),
                    Medication(
                        tradeName = "كوفستوب شراب",
                        scientificName = "Dextromethorphan + Chlorpheniramine",
                        category = "شراب وأدوية سائلة",
                        dosageForm = "شراب",
                        packageUnit = "علبة",
                        unitPrice = 2000.0,
                        currentStock = 40,
                        minStockAlert = 10
                    ),
                    Medication(
                        tradeName = "اوترينوز قطرة صغار ٠.٠٥٪",
                        scientificName = "Oxymetazoline HCl 0.05% Paediatric",
                        category = "قطرات ومحاليل",
                        dosageForm = "قطرة أنفية",
                        packageUnit = "علبة",
                        unitPrice = 1250.0,
                        currentStock = 30,
                        minStockAlert = 5
                    ),
                    Medication(
                        tradeName = "اوترينوز قطرة كبار ٠.١٪",
                        scientificName = "Oxymetazoline HCl 0.1% Adult",
                        category = "قطرات ومحاليل",
                        dosageForm = "قطرة أنفية",
                        packageUnit = "علبة",
                        unitPrice = 1500.0,
                        currentStock = 45,
                        minStockAlert = 10
                    ),
                    Medication(
                        tradeName = "بيبي كاز قطرة",
                        scientificName = "Simethicone Infantile Drops",
                        category = "قطرات ومحاليل",
                        dosageForm = "قطرة فموية",
                        packageUnit = "علبة",
                        unitPrice = 1750.0,
                        currentStock = 35,
                        minStockAlert = 8
                    ),
                    Medication(
                        tradeName = "ايكازول تحاميل",
                        scientificName = "Econazole Nitrate Suppositories",
                        category = "تحاميل ومراهم",
                        dosageForm = "تحاميل",
                        packageUnit = "باكيت",
                        unitPrice = 2250.0,
                        currentStock = 25,
                        minStockAlert = 5
                    ),
                    Medication(
                        tradeName = "نازفرين قطرة كبار ١٪",
                        scientificName = "Naphazoline HCl 1% Adult",
                        category = "قطرات ومحاليل",
                        dosageForm = "قطرة أنفية",
                        packageUnit = "علبة",
                        unitPrice = 1500.0,
                        currentStock = 30,
                        minStockAlert = 5
                    ),
                    Medication(
                        tradeName = "دياراستوب حب كبير",
                        scientificName = "Loperamide HCl + Furazolidone",
                        category = "حبوب وكبسولات",
                        dosageForm = "حبوب",
                        packageUnit = "شريط",
                        unitPrice = 1000.0,
                        currentStock = 60,
                        minStockAlert = 15
                    )
                )
                defaultList.forEach { medicationRepository.insert(it) }
            }
        }
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Pharmacy Profile Actions
    fun updatePharmacyProfile(profile: PharmacyProfile) {
        viewModelScope.launch {
            pharmacyRepository.saveProfile(profile)
            Toast.makeText(getApplication(), "تم حفظ بيانات الصيدلية والموقع بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    // Search and Filters
    fun setOrderSearchQuery(query: String) {
        _orderSearchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setInventorySearchQuery(query: String) {
        _inventorySearchQuery.value = query
    }

    fun setInventoryFilter(filter: InventoryFilter) {
        _inventoryFilter.value = filter
    }

    fun setOrderUrgency(urgency: String) {
        _orderUrgency.value = urgency
    }

    fun setOrderSupplier(supplier: String) {
        _orderSupplier.value = supplier
    }

    fun setOrderNotes(notes: String) {
        _orderNotes.value = notes
    }

    // Cart Management
    fun setItemQuantity(medication: Medication, quantity: Int) {
        val current = _cartItems.value.toMutableMap()
        if (quantity <= 0) {
            current.remove(medication.id)
        } else {
            val existing = current[medication.id]
            current[medication.id] = existing?.copy(quantity = quantity) ?: OrderItem(
                medicationId = medication.id,
                tradeName = medication.tradeName,
                scientificName = medication.scientificName,
                category = medication.category,
                dosageForm = medication.dosageForm,
                packageUnit = medication.packageUnit,
                quantity = quantity,
                unitPrice = medication.unitPrice,
                imageUrl = medication.imageUrl
            )
        }
        _cartItems.value = current
    }

    fun incrementItemQuantity(medication: Medication, delta: Int = 1) {
        val current = _cartItems.value.toMutableMap()
        val existing = current[medication.id]
        val newQty = (existing?.quantity ?: 0) + delta
        if (newQty <= 0) {
            current.remove(medication.id)
        } else {
            current[medication.id] = existing?.copy(quantity = newQty) ?: OrderItem(
                medicationId = medication.id,
                tradeName = medication.tradeName,
                scientificName = medication.scientificName,
                category = medication.category,
                dosageForm = medication.dosageForm,
                packageUnit = medication.packageUnit,
                quantity = newQty,
                unitPrice = medication.unitPrice,
                imageUrl = medication.imageUrl
            )
        }
        _cartItems.value = current
    }

    fun decrementItemQuantity(medication: Medication, delta: Int = 1) {
        incrementItemQuantity(medication, -delta)
    }

    fun removeItemFromCart(medicationId: Long) {
        val current = _cartItems.value.toMutableMap()
        current.remove(medicationId)
        _cartItems.value = current
    }

    fun updateCartItemQuantity(medicationId: Long, newQuantity: Int) {
        val current = _cartItems.value.toMutableMap()
        if (newQuantity <= 0) {
            current.remove(medicationId)
        } else {
            val existing = current[medicationId]
            if (existing != null) {
                current[medicationId] = existing.copy(quantity = newQuantity)
            }
        }
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    // Inventory CRUD
    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.insert(medication)
            Toast.makeText(getApplication(), "تمت إضافة المادة بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.update(medication)
            Toast.makeText(getApplication(), "تم تحديث بيانات الدواء", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateQuickStock(medicationId: Long, delta: Int) {
        viewModelScope.launch {
            val med = medicationRepository.getMedicationById(medicationId) ?: return@launch
            val updated = (med.currentStock + delta).coerceAtLeast(0)
            medicationRepository.updateStock(medicationId, updated)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.delete(medication)
            removeItemFromCart(medication.id)
            Toast.makeText(getApplication(), "تم حذف المادة بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllMedications() {
        viewModelScope.launch {
            medicationRepository.clearAll()
            clearCart()
            Toast.makeText(getApplication(), "تم حذف جميع المواد وتفريغ القائمة بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    // Order Summary Generation
    fun generateOrderSummary(depotNameOverride: String? = null): OrderEntity? {
        val items = _cartItems.value.values.toList()
        if (items.isEmpty()) return null

        val profile = pharmacyProfile.value
        val pharmacyName = _currentOrderPharmacyName.value.ifBlank {
            profile?.pharmacyName?.ifBlank { "السنار" } ?: "السنار"
        }
        val pharmacyLocation = _currentOrderPharmacyLocation.value.ifBlank {
            profile?.neighborhood?.ifBlank { profile.city } ?: ""
        }

        val depotName = depotNameOverride?.ifBlank { null }
            ?: _orderDepotName.value.ifBlank {
                _orderSupplier.value.ifBlank { "مذخر سبأ" }
            }

        _orderDepotName.value = depotName
        _orderSupplier.value = depotName

        val pharmacistName = profile?.pharmacistName?.ifBlank { "الصيدلي المسؤول" } ?: "الصيدلي المسؤول"
        val phone = profile?.phoneNumber.orEmpty()
        val address = buildString {
            if (pharmacyLocation.isNotBlank()) append(pharmacyLocation).append(" - ")
            if (!profile?.city.isNullOrBlank() && profile?.city != pharmacyLocation) append(profile?.city).append(" - ")
            if (!profile?.streetAddress.isNullOrBlank()) append(profile?.streetAddress)
        }.ifBlank { pharmacyLocation.ifBlank { "العنوان غير محدد" } }
        val locationUrl = profile?.locationCoordinatesOrUrl.orEmpty()

        val orderNum = "ORD-${System.currentTimeMillis().toString().takeLast(6)}"
        val totalUnits = items.sumOf { it.quantity }
        val totalPrice = items.sumOf { it.subtotal }

        // Generate the EXACT model slip requested by the user:
        // ص. السنار / حي سومر 
        // ـ جنى شراب (١٠)
        // ...
        // (مذخر سبأ)
        val formattedModelText = formatOrderSlip(
            pharmacyName = pharmacyName,
            pharmacyLocation = pharmacyLocation,
            items = items,
            depotName = depotName
        )

        val order = OrderEntity(
            orderNumber = orderNum,
            createdAt = System.currentTimeMillis(),
            pharmacyName = pharmacyName,
            pharmacistName = pharmacistName,
            phoneNumber = phone,
            fullAddress = address,
            locationCoordinatesOrUrl = locationUrl,
            itemsSummary = formattedModelText,
            totalItemsCount = items.size,
            totalUnitsCount = totalUnits,
            totalEstimatedPrice = totalPrice,
            urgencyLevel = _orderUrgency.value,
            supplierName = depotName,
            notes = _orderNotes.value.ifBlank { profile?.defaultNotes.orEmpty() },
            formattedCopyText = formattedModelText
        )

        _lastGeneratedOrder.value = order
        return order
    }

    fun saveCurrentOrderToHistory(order: OrderEntity) {
        viewModelScope.launch {
            orderRepository.saveOrder(order)
            Toast.makeText(getApplication(), "تم حفظ الطلبية في الأرشيف بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteOrderFromHistory(order: OrderEntity) {
        viewModelScope.launch {
            orderRepository.deleteOrder(order)
            Toast.makeText(getApplication(), "تم حذف الطلبية من الأرشيف", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("طلب أدوية", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ النموذج بالكامل إلى الحافظة!", Toast.LENGTH_LONG).show()
    }

    fun shareOrderText(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة طلب الأدوية عبر...")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun startNewOrderForPharmacy(pharmacyName: String, pharmacyLocation: String = "") {
        _currentOrderPharmacyName.value = pharmacyName
        _currentOrderPharmacyLocation.value = pharmacyLocation
        clearCart()
        navigateTo(Screen.OrderCreation)
    }

    fun setOrderPharmacyName(name: String) {
        _currentOrderPharmacyName.value = name
    }

    fun setOrderPharmacyLocation(location: String) {
        _currentOrderPharmacyLocation.value = location
    }

    fun setOrderDepotName(depot: String) {
        _orderDepotName.value = depot
        _orderSupplier.value = depot
    }

    fun printOrder(context: Context, order: OrderEntity, items: List<OrderItem>) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager == null) {
                Toast.makeText(context, "خدمة الطباعة غير متوفرة على هذا الجهاز", Toast.LENGTH_SHORT).show()
                return
            }

            val displayItems = if (items.isNotEmpty()) items else _cartItems.value.values.toList()
            val cleanName = order.pharmacyName.trim().let { n ->
                if (n.startsWith("ص.") || n.startsWith("ص /") || n.startsWith("صيدلية")) n else "ص. $n"
            }
            val location = _currentOrderPharmacyLocation.value.ifBlank { order.fullAddress }
            val headerText = if (location.isNotBlank()) "$cleanName / $location" else cleanName
            val depot = order.supplierName.ifBlank { _orderDepotName.value.ifBlank { "مذخر سبأ" } }
            val cleanDepot = if (depot.startsWith("مذخر")) depot else "مذخر $depot"

            val htmlContent = buildString {
                append("""
                    <!DOCTYPE html>
                    <html dir="rtl" lang="ar">
                    <head>
                        <meta charset="utf-8">
                        <title>طلبية - $headerText</title>
                        <style>
                            @page { margin: 12mm 15mm; size: auto; }
                            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 15px; direction: rtl; color: #1e293b; background: #fff; }
                            .slip-box {
                                border: 2.5px solid #0f766e;
                                border-radius: 12px;
                                padding: 22px 24px;
                                margin-bottom: 20px;
                                background-color: #ffffff;
                            }
                            .slip-header {
                                font-size: 26px;
                                font-weight: bold;
                                color: #0f766e;
                                border-bottom: 2px solid #0f766e;
                                padding-bottom: 12px;
                                margin-bottom: 18px;
                            }
                            .slip-item {
                                font-size: 20px;
                                font-weight: 600;
                                line-height: 2.1;
                                color: #0f172a;
                            }
                            .slip-footer {
                                margin-top: 24px;
                                padding-top: 12px;
                                font-size: 22px;
                                font-weight: bold;
                                color: #0d9488;
                                text-align: right;
                            }
                            .details-table {
                                width: 100%;
                                border-collapse: collapse;
                                margin-top: 20px;
                                font-size: 13px;
                            }
                            .details-table th, .details-table td {
                                border: 1px solid #cbd5e1;
                                padding: 7px 10px;
                                text-align: right;
                            }
                            .details-table th {
                                background-color: #0f766e;
                                color: #ffffff;
                            }
                            .total-row {
                                font-weight: bold;
                                background-color: #ccfbf1 !important;
                                color: #0f766e;
                            }
                            .print-footer {
                                margin-top: 25px;
                                text-align: center;
                                font-size: 11px;
                                color: #94a3b8;
                                border-top: 1px solid #e2e8f0;
                                padding-top: 8px;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="slip-box">
                            <div class="slip-header">$headerText</div>
                """.trimIndent())

                displayItems.forEach { itm ->
                    val arabicQty = toArabicDigits(itm.quantity)
                    append("""
                        <div class="slip-item">ـ ${itm.tradeName} ($arabicQty)</div>
                    """.trimIndent())
                }

                append("""
                            <div class="slip-footer">($cleanDepot)</div>
                        </div>

                        <table class="details-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>المادة بالصورة</th>
                                    <th>الكمية</th>
                                    <th>سعر الوحدة</th>
                                    <th>المجموع</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                displayItems.forEachIndexed { idx, itm ->
                    append("""
                        <tr>
                            <td>${idx + 1}</td>
                            <td><strong>${itm.tradeName}</strong></td>
                            <td><strong>${toArabicDigits(itm.quantity)} (${itm.quantity})</strong></td>
                            <td>${if (itm.unitPrice > 0) formatIraqiDinar(itm.unitPrice) else "-"}</td>
                            <td>${if (itm.subtotal > 0) formatIraqiDinar(itm.subtotal) else "-"}</td>
                        </tr>
                    """.trimIndent())
                }

                append("""
                                <tr class="total-row">
                                    <td colspan="2"><strong>الإجمالي الكلي: (${order.totalItemsCount} أصناف)</strong></td>
                                    <td><strong>${order.totalUnitsCount} عبوة</strong></td>
                                    <td>-</td>
                                    <td><strong>${formatIraqiDinar(order.totalEstimatedPrice)}</strong></td>
                                </tr>
                            </tbody>
                        </table>

                        <div class="print-footer">
                            رقم الطلبية: ${order.orderNumber} | التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(order.createdAt))}
                        </div>
                    </body>
                    </html>
                """.trimIndent())
            }

            val webView = android.webkit.WebView(context)
            webView.webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter("طلبية_${order.orderNumber}")
                    printManager.print(
                        "طلبية_${order.orderNumber}",
                        printAdapter,
                        android.print.PrintAttributes.Builder().build()
                    )
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ أثناء إرسال أمر الطباعة: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setLastOrderForViewing(order: OrderEntity) {
        _lastGeneratedOrder.value = order
        navigateTo(Screen.OrderSummary)
    }
}
