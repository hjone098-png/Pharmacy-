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

    // Filtered Medications for Order Catalog
    val catalogMedications: StateFlow<List<Medication>> = combine(
        allMedications,
        _orderSearchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { med ->
            val matchesCategory = (category == "الكل" || med.category == category)
            val matchesQuery = query.isBlank() ||
                    med.tradeName.contains(query, ignoreCase = true) ||
                    med.scientificName.contains(query, ignoreCase = true) ||
                    med.category.contains(query, ignoreCase = true) ||
                    med.dosageForm.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Medications for Inventory Management
    val inventoryMedications: StateFlow<List<Medication>> = combine(
        allMedications,
        _inventorySearchQuery,
        _inventoryFilter
    ) { list, query, filter ->
        list.filter { med ->
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Order Cart: Map of MedicationId -> OrderItem
    private val _cartItems = MutableStateFlow<Map<Long, OrderItem>>(emptyMap())
    val cartItems: StateFlow<Map<Long, OrderItem>> = _cartItems.asStateFlow()

    // Pharmacy Name for Current Order
    private val _currentOrderPharmacyName = MutableStateFlow("")
    val currentOrderPharmacyName: StateFlow<String> = _currentOrderPharmacyName.asStateFlow()

    // Order Meta State
    private val _orderUrgency = MutableStateFlow("عادي")
    val orderUrgency: StateFlow<String> = _orderUrgency.asStateFlow()

    private val _orderSupplier = MutableStateFlow("")
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
        // Ensure initial profile or data if needed
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (pharmacyRepository.getProfile() == null) {
                    AppDatabase.seedInitialData(database)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    // Inventory CRUD
    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.insert(medication)
            Toast.makeText(getApplication(), "تمت إضافة الدواء بنجاح", Toast.LENGTH_SHORT).show()
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
            // also remove from cart if present
            removeItemFromCart(medication.id)
            Toast.makeText(getApplication(), "تم حذف الدواء من المخزون", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetInitialData() {
        viewModelScope.launch {
            AppDatabase.seedInitialData(database)
            Toast.makeText(getApplication(), "تمت استعادة الأدوية النموذجية", Toast.LENGTH_SHORT).show()
        }
    }

    // Order Summary Generation
    fun generateOrderSummary(): OrderEntity? {
        val items = _cartItems.value.values.toList()
        if (items.isEmpty()) return null

        val profile = pharmacyProfile.value
        val pharmacyName = _currentOrderPharmacyName.value.ifBlank {
            profile?.pharmacyName?.ifBlank { "صيدلية غير محددة" } ?: "صيدلية عامة"
        }
        val pharmacistName = profile?.pharmacistName?.ifBlank { "الصيدلي المسؤول" } ?: "الصيدلي المسؤول"
        val phone = profile?.phoneNumber.orEmpty()
        val address = buildString {
            if (!profile?.city.isNullOrBlank()) append(profile?.city).append(" - ")
            if (!profile?.neighborhood.isNullOrBlank()) append(profile?.neighborhood).append(" - ")
            if (!profile?.streetAddress.isNullOrBlank()) append(profile?.streetAddress)
        }.ifBlank { "العنوان غير محدد" }
        val locationUrl = profile?.locationCoordinatesOrUrl.orEmpty()

        val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date())
        val orderNum = "ORD-${System.currentTimeMillis().toString().takeLast(6)}"

        val totalUnits = items.sumOf { it.quantity }
        val totalPrice = items.sumOf { it.subtotal }

        // Build itemized text for summary & copying
        val itemsText = buildString {
            items.forEachIndexed { index, item ->
                append("${index + 1}. ${item.tradeName}")
                if (item.dosageForm.isNotBlank()) append(" (${item.dosageForm})")
                append(" - الكمية: ${item.quantity} ${item.packageUnit}")
                if (item.unitPrice > 0) {
                    append(" (سعر الوحدة: ${String.format(Locale.US, "%.2f", item.unitPrice)} ر.س)")
                }
                append("\n")
            }
        }

        val formattedMessage = buildString {
            append("🏥 *طلب أدوية جديد - $pharmacyName*\n")
            append("🔖 *رقم الطلبية:* $orderNum\n")
            append("📅 *التاريخ:* $dateStr\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📍 *بيانات الصيدلية والموقع:*\n")
            append("• *الصيدلية:* $pharmacyName\n")
            append("• *المسؤول:* $pharmacistName")
            if (!profile?.licenseNumber.isNullOrBlank()) append(" (ترخيص: ${profile?.licenseNumber})")
            append("\n")
            if (phone.isNotBlank()) append("• *الهاتف / واتساب:* $phone\n")
            append("• *العنوان:* $address\n")
            if (locationUrl.isNotBlank()) append("• *الموقع الجغرافي:* $locationUrl\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📦 *قائمة المواد والكميات المطلوبة:*\n")
            append(itemsText)
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("🔢 *إجمالي الأصناف:* ${items.size} أصناف ($totalUnits عبوة/وحدة)\n")
            if (totalPrice > 0) {
                append("💰 *إجمالي القيمة التقديرية:* ${String.format(Locale.US, "%.2f", totalPrice)} ر.س\n")
            }
            append("🚨 *درجة الأولوية:* ${_orderUrgency.value}\n")
            if (_orderSupplier.value.isNotBlank()) {
                append("🏢 *الموزع / المستودع الموجه له:* ${_orderSupplier.value}\n")
            }
            if (_orderNotes.value.isNotBlank()) {
                append("📝 *ملاحظات التوريد:* ${_orderNotes.value}\n")
            } else if (!profile?.defaultNotes.isNullOrBlank()) {
                append("📝 *ملاحظات التوريد:* ${profile?.defaultNotes}\n")
            }
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("✅ *تم التوليد عبر تطبيق إدارة طلبات الأدوية*")
        }

        val order = OrderEntity(
            orderNumber = orderNum,
            createdAt = System.currentTimeMillis(),
            pharmacyName = pharmacyName,
            pharmacistName = pharmacistName,
            phoneNumber = phone,
            fullAddress = address,
            locationCoordinatesOrUrl = locationUrl,
            itemsSummary = itemsText,
            totalItemsCount = items.size,
            totalUnitsCount = totalUnits,
            totalEstimatedPrice = totalPrice,
            urgencyLevel = _orderUrgency.value,
            supplierName = _orderSupplier.value.ifBlank { profile?.preferredSupplier.orEmpty() },
            notes = _orderNotes.value.ifBlank { profile?.defaultNotes.orEmpty() },
            formattedCopyText = formattedMessage
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
        Toast.makeText(context, "تم نسخ تفاصيل الطلبية بالكامل إلى الحافظة!", Toast.LENGTH_LONG).show()
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

    fun startNewOrderForPharmacy(pharmacyName: String) {
        _currentOrderPharmacyName.value = pharmacyName
        clearCart()
        navigateTo(Screen.OrderCreation)
    }

    fun setOrderPharmacyName(name: String) {
        _currentOrderPharmacyName.value = name
    }

    fun printOrder(context: Context, order: OrderEntity, items: List<OrderItem>) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager == null) {
                Toast.makeText(context, "خدمة الطباعة غير متوفرة على هذا الجهاز", Toast.LENGTH_SHORT).show()
                return
            }

            val htmlContent = buildString {
                append("""
                    <!DOCTYPE html>
                    <html dir="rtl" lang="ar">
                    <head>
                        <meta charset="utf-8">
                        <title>طلبية أدوية - ${order.pharmacyName}</title>
                        <style>
                            body { font-family: sans-serif; margin: 20px; direction: rtl; color: #1e293b; }
                            .header { text-align: center; border-bottom: 2px solid #0f766e; padding-bottom: 12px; margin-bottom: 20px; }
                            .header h1 { margin: 0; color: #0f766e; font-size: 24px; }
                            .meta { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 13px; background: #f1f5f9; padding: 10px; border-radius: 8px; }
                            table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 13px; }
                            th, td { border: 1px solid #cbd5e1; padding: 8px 10px; text-align: right; }
                            th { background-color: #0f766e; color: white; }
                            tr:nth-child(even) { background-color: #f8fafc; }
                            .total-row { font-weight: bold; background-color: #ccfbf1 !important; color: #0f766e; }
                            .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #e2e8f0; padding-top: 10px; }
                        </style>
                    </head>
                    <body>
                        <div class="header">
                            <h1>🏥 طلبية أدوية كاملة</h1>
                            <h3>${order.pharmacyName}</h3>
                        </div>
                        <div class="meta">
                            <div><strong>رقم الطلبية:</strong> ${order.orderNumber}</div>
                            <div><strong>التاريخ:</strong> ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(order.createdAt))}</div>
                            <div><strong>الأولوية:</strong> ${order.urgencyLevel}</div>
                        </div>
                        <div style="margin-bottom: 15px; font-size: 13px;">
                            ${if (order.phoneNumber.isNotBlank()) "<div><strong>الهاتف:</strong> ${order.phoneNumber}</div>" else ""}
                            ${if (order.fullAddress.isNotBlank()) "<div><strong>العنوان:</strong> ${order.fullAddress}</div>" else ""}
                            ${if (order.notes.isNotBlank()) "<div><strong>ملاحظات:</strong> ${order.notes}</div>" else ""}
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>اسم المادة الدوائية</th>
                                    <th>الشكل / الوحدة</th>
                                    <th>الكمية المطلوبة</th>
                                    <th>سعر الوحدة</th>
                                    <th>المجموع</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                val displayItems = if (items.isNotEmpty()) items else _cartItems.value.values.toList()
                displayItems.forEachIndexed { idx, itm ->
                    append("""
                        <tr>
                            <td>${idx + 1}</td>
                            <td><strong>${itm.tradeName}</strong>${if (itm.scientificName.isNotBlank()) "<br><small style='color:#64748b;'>${itm.scientificName}</small>" else ""}</td>
                            <td>${itm.dosageForm} (${itm.packageUnit})</td>
                            <td><strong>${itm.quantity}</strong></td>
                            <td>${if (itm.unitPrice > 0) String.format(Locale.US, "%.2f ر.س", itm.unitPrice) else "-"}</td>
                            <td>${if (itm.subtotal > 0) String.format(Locale.US, "%.2f ر.س", itm.subtotal) else "-"}</td>
                        </tr>
                    """.trimIndent())
                }

                append("""
                                <tr class="total-row">
                                    <td colspan="3"><strong>الإجمالي الكلي: (${order.totalItemsCount} أصناف)</strong></td>
                                    <td><strong>${order.totalUnitsCount} عبوة</strong></td>
                                    <td>-</td>
                                    <td><strong>${String.format(Locale.US, "%.2f ر.س", order.totalEstimatedPrice)}</strong></td>
                                </tr>
                            </tbody>
                        </table>
                        <div class="footer">
                            تمت طباعة الطلبية عبر تطبيق طلبيات الأدوية
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
