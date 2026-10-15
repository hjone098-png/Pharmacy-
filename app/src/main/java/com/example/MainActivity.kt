package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PharmaBottomBar
import com.example.ui.components.PharmaTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.history.OrderHistoryScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.order.OrderCreationScreen
import com.example.ui.screens.profile.PharmacyProfileScreen
import com.example.ui.screens.summary.OrderSummaryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.PharmaOrderViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PharmaOrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Ensure RTL layout for Arabic
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    PharmaAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PharmaAppContent(viewModel: PharmaOrderViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val pharmacyProfile by viewModel.pharmacyProfile.collectAsStateWithLifecycle()
    val currentPharmacyName by viewModel.currentOrderPharmacyName.collectAsStateWithLifecycle()
    val currentPharmacyLocation by viewModel.currentOrderPharmacyLocation.collectAsStateWithLifecycle()
    val orderDepotName by viewModel.orderDepotName.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

    val catalogMeds by viewModel.catalogMedications.collectAsStateWithLifecycle()
    val inventoryMeds by viewModel.inventoryMedications.collectAsStateWithLifecycle()
    val allMeds by viewModel.allMedications.collectAsStateWithLifecycle()

    val orderSearchQuery by viewModel.orderSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val inventorySearchQuery by viewModel.inventorySearchQuery.collectAsStateWithLifecycle()
    val inventoryFilter by viewModel.inventoryFilter.collectAsStateWithLifecycle()

    val orderUrgency by viewModel.orderUrgency.collectAsStateWithLifecycle()
    val orderSupplier by viewModel.orderSupplier.collectAsStateWithLifecycle()
    val orderNotes by viewModel.orderNotes.collectAsStateWithLifecycle()

    val lastGeneratedOrder by viewModel.lastGeneratedOrder.collectAsStateWithLifecycle()
    val orderHistory by viewModel.orderHistory.collectAsStateWithLifecycle()

    // Dialog state for "+" button asking for pharmacy name and location (بوابة اسم الصيدلية والموقع)
    var showPharmacyNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PharmaTopBar(
                currentScreen = currentScreen,
                pharmacyProfile = pharmacyProfile,
                onNavigateTo = { viewModel.navigateTo(it) },
                onStartNewOrderClick = { showPharmacyNameDialog = true },
                onClearAllMedications = { viewModel.clearAllMedications() }
            )
        },
        bottomBar = {
            PharmaBottomBar(
                currentScreen = currentScreen,
                cartItemCount = cartItems.size,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        floatingActionButton = {
            if (currentScreen == Screen.OrderCreation) {
                FloatingActionButton(
                    onClick = { showPharmacyNameDialog = true },
                    containerColor = PrimaryTeal,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_pharmacy_plus")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "بدء طلبية باسم الصيدلية والموقع")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الصيدلية والموقع (+)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.OrderCreation -> {
                OrderCreationScreen(
                    medications = catalogMeds,
                    cartItems = cartItems,
                    pharmacyProfile = pharmacyProfile,
                    currentPharmacyName = currentPharmacyName,
                    currentPharmacyLocation = currentPharmacyLocation,
                    currentOrderDepot = orderDepotName,
                    searchQuery = orderSearchQuery,
                    selectedCategory = selectedCategory,
                    onSearchQueryChange = { viewModel.setOrderSearchQuery(it) },
                    onCategoryChange = { viewModel.setSelectedCategory(it) },
                    onQuantityChange = { med, qty -> viewModel.setItemQuantity(med, qty) },
                    onClearCart = { viewModel.clearCart() },
                    onProceedToSummary = { selectedDepot ->
                        viewModel.setOrderDepotName(selectedDepot)
                        viewModel.generateOrderSummary(selectedDepot)
                        viewModel.navigateTo(Screen.OrderSummary)
                    },
                    onAddNewMedicationWithImage = { viewModel.addMedication(it) },
                    onDeleteMedication = { viewModel.deleteMedication(it) },
                    onChangePharmacyName = { showPharmacyNameDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.OrderSummary -> {
                OrderSummaryScreen(
                    order = lastGeneratedOrder,
                    cartItems = cartItems,
                    pharmacyProfile = pharmacyProfile,
                    orderUrgency = orderUrgency,
                    orderSupplier = orderSupplier,
                    orderNotes = orderNotes,
                    onUrgencyChange = { viewModel.setOrderUrgency(it) },
                    onSupplierChange = { viewModel.setOrderSupplier(it) },
                    onNotesChange = { viewModel.setOrderNotes(it) },
                    onGenerateOrder = { viewModel.generateOrderSummary() },
                    onSaveOrderToHistory = { viewModel.saveCurrentOrderToHistory(it) },
                    onCopyToClipboard = { viewModel.copyToClipboard(context, it) },
                    onShareOrder = { viewModel.shareOrderText(context, it) },
                    onPrintOrder = { ord, items -> viewModel.printOrder(context, ord, items) },
                    onStartNewOrder = { showPharmacyNameDialog = true },
                    onEditOrder = { viewModel.navigateTo(Screen.OrderCreation) },
                    onNavigateBack = { viewModel.navigateTo(Screen.OrderCreation) }
                )
            }

            Screen.Inventory -> {
                InventoryScreen(
                    medications = inventoryMeds,
                    allMedicationsCount = allMeds.size,
                    searchQuery = inventorySearchQuery,
                    currentFilter = inventoryFilter,
                    onSearchChange = { viewModel.setInventorySearchQuery(it) },
                    onFilterChange = { viewModel.setInventoryFilter(it) },
                    onAddMedication = { viewModel.addMedication(it) },
                    onUpdateMedication = { viewModel.updateMedication(it) },
                    onDeleteMedication = { viewModel.deleteMedication(it) },
                    onQuickStockChange = { id, delta -> viewModel.updateQuickStock(id, delta) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.PharmacyProfile -> {
                PharmacyProfileScreen(
                    currentProfile = pharmacyProfile,
                    onSaveProfile = { viewModel.updatePharmacyProfile(it) },
                    onNavigateBack = { viewModel.navigateTo(Screen.OrderCreation) },
                    onNavigateToCatalog = { viewModel.navigateTo(Screen.OrderCreation) }
                )
            }

            Screen.OrderHistory -> {
                OrderHistoryScreen(
                    orders = orderHistory,
                    onViewOrder = { viewModel.setLastOrderForViewing(it) },
                    onDeleteOrder = { viewModel.deleteOrderFromHistory(it) },
                    onCopyToClipboard = { viewModel.copyToClipboard(context, it) },
                    onShareOrder = { viewModel.shareOrderText(context, it) },
                    onNavigateBack = { viewModel.navigateTo(Screen.OrderCreation) }
                )
            }
        }
    }

    // Portal Dialog for "+" button: asks for pharmacy name AND location then forwards immediately to the added materials
    if (showPharmacyNameDialog) {
        val profile = pharmacyProfile
        val initialName = currentPharmacyName.ifBlank {
            profile?.pharmacyName?.ifBlank { "السنار" } ?: "السنار"
        }
        val initialLocation = currentPharmacyLocation.ifBlank {
            profile?.neighborhood?.ifBlank { profile.city.orEmpty() } ?: "حي سومر"
        }
        var enteredPharmacyName by remember(initialName) { mutableStateOf(initialName) }
        var enteredPharmacyLocation by remember(initialLocation) { mutableStateOf(initialLocation) }

        AlertDialog(
            onDismissRequest = { showPharmacyNameDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بوابة طلبية جديدة (الصيدلية والموقع)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "اكتب اسم الصيدلية وموقعها ليتم توجيهك مباشرة لقائمة المواد والصور لاختيار الأدوية والكميات المطلوبة:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // الخيار الأول: اسم الصيدلية
                    OutlinedTextField(
                        value = enteredPharmacyName,
                        onValueChange = { enteredPharmacyName = it },
                        label = { Text("اسم الصيدلية *") },
                        placeholder = { Text("مثال: ص. السنار أو السنار") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = PrimaryTeal
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_pharmacy_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // الخيار الثاني تحت اسم الصيدلية: الموقع مالتها
                    OutlinedTextField(
                        value = enteredPharmacyLocation,
                        onValueChange = { enteredPharmacyLocation = it },
                        label = { Text("موقع الصيدلية (الموقع مالتها) *") },
                        placeholder = { Text("مثال: حي سومر، المنصور، الكرادة...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryTeal
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_pharmacy_location_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = enteredPharmacyName.trim().ifBlank { "السنار" }
                        val location = enteredPharmacyLocation.trim().ifBlank { "حي سومر" }
                        viewModel.startNewOrderForPharmacy(name, location)
                        showPharmacyNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("dialog_confirm_pharmacy_btn")
                ) {
                    Text("متابعة لاختيار المواد والكميات ➔", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPharmacyNameDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
