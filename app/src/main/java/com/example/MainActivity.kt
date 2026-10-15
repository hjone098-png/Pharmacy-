package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
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

    // Dialog state for "+" button asking for pharmacy name
    var showPharmacyNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PharmaTopBar(
                currentScreen = currentScreen,
                pharmacyProfile = pharmacyProfile,
                onNavigateTo = { viewModel.navigateTo(it) },
                onStartNewOrderClick = { showPharmacyNameDialog = true },
                onResetData = { viewModel.resetInitialData() }
            )
        },
        bottomBar = {
            PharmaBottomBar(
                currentScreen = currentScreen,
                cartItemCount = cartItems.size,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.OrderCreation -> {
                OrderCreationScreen(
                    medications = catalogMeds,
                    cartItems = cartItems,
                    pharmacyProfile = pharmacyProfile,
                    currentPharmacyName = currentPharmacyName,
                    searchQuery = orderSearchQuery,
                    selectedCategory = selectedCategory,
                    onSearchQueryChange = { viewModel.setOrderSearchQuery(it) },
                    onCategoryChange = { viewModel.setSelectedCategory(it) },
                    onQuantityChange = { med, qty -> viewModel.setItemQuantity(med, qty) },
                    onClearCart = { viewModel.clearCart() },
                    onProceedToSummary = {
                        viewModel.generateOrderSummary()
                        viewModel.navigateTo(Screen.OrderSummary)
                    },
                    onAddNewMedicationWithImage = { viewModel.addMedication(it) },
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

    // Dialog for "+" button: asks for pharmacy name then forwards immediately to the added materials
    if (showPharmacyNameDialog) {
        val initialName = currentPharmacyName.ifBlank {
            pharmacyProfile?.pharmacyName?.ifBlank { "صيدلية النخبة الطبية" } ?: ""
        }
        var enteredPharmacyName by remember(initialName) { mutableStateOf(initialName) }

        AlertDialog(
            onDismissRequest = { showPharmacyNameDialog = false },
            title = {
                Text(
                    text = "بدء طلبية أدوية جديدة",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "اكتب اسم الصيدلية ليتم تحويلك مباشرة لاختيار المواد والكميات وتوليد الطلبية:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredPharmacyName,
                        onValueChange = { enteredPharmacyName = it },
                        label = { Text("اسم الصيدلية") },
                        placeholder = { Text("مثال: صيدلية النور، صيدلية السلام") },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = enteredPharmacyName.trim().ifBlank { "صيدلية عامة" }
                        viewModel.startNewOrderForPharmacy(name)
                        showPharmacyNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("dialog_confirm_pharmacy_btn")
                ) {
                    Text("متابعة لاختيار المواد ➔", fontWeight = FontWeight.Bold)
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
