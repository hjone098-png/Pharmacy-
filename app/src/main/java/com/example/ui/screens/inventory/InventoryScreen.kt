package com.example.ui.screens.inventory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Medication
import com.example.ui.components.DosageFormIcon
import com.example.ui.components.StockBadge
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.StockDangerContainer
import com.example.ui.theme.StockDangerRed
import com.example.ui.theme.StockOkContainer
import com.example.ui.theme.StockOkGreen
import com.example.ui.theme.StockWarningAmber
import com.example.ui.theme.StockWarningContainer
import com.example.ui.theme.TealContainer
import com.example.ui.viewmodel.InventoryFilter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    medications: List<Medication>,
    allMedicationsCount: Int,
    searchQuery: String,
    currentFilter: InventoryFilter,
    onSearchChange: (String) -> Unit,
    onFilterChange: (InventoryFilter) -> Unit,
    onAddMedication: (Medication) -> Unit,
    onUpdateMedication: (Medication) -> Unit,
    onDeleteMedication: (Medication) -> Unit,
    onQuickStockChange: (Long, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<Medication?>(null) }
    var deletingMedication by remember { mutableStateOf<Medication?>(null) }

    val lowStockCount = medications.count { it.isLowStock }
    val outOfStockCount = medications.count { it.isOutOfStock }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("inventory_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Stats KPI Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All items stat
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TealContainer.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الأصناف", fontSize = 11.sp, color = PrimaryTeal, fontWeight = FontWeight.Medium)
                        Text("$allMedicationsCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                    }
                }

                // Low stock stat
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StockWarningContainer.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("نقص وشيك", fontSize = 11.sp, color = StockWarningAmber, fontWeight = FontWeight.Medium)
                        Text("$lowStockCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StockWarningAmber)
                    }
                }

                // Out of stock stat
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StockDangerContainer.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("نفد المخزون", fontSize = 11.sp, color = StockDangerRed, fontWeight = FontWeight.Medium)
                        Text("$outOfStockCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StockDangerRed)
                    }
                }
            }

            // Search Bar & Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("بحث في المخزون بالاسم أو الشركة...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                SecondaryTabRow(
                    selectedTabIndex = currentFilter.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryTeal
                ) {
                    Tab(
                        selected = currentFilter == InventoryFilter.ALL,
                        onClick = { onFilterChange(InventoryFilter.ALL) },
                        text = { Text("الكل ($allMedicationsCount)", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = currentFilter == InventoryFilter.LOW_STOCK,
                        onClick = { onFilterChange(InventoryFilter.LOW_STOCK) },
                        text = { Text("منخفض ($lowStockCount)", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = currentFilter == InventoryFilter.OUT_OF_STOCK,
                        onClick = { onFilterChange(InventoryFilter.OUT_OF_STOCK) },
                        text = { Text("نفد ($outOfStockCount)", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Inventory List
            if (medications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد أدوية مطابقة لهذا الفلتر",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة دواء للمخزون")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = medications,
                        key = { it.id }
                    ) { med ->
                        InventoryMedicineCard(
                            medication = med,
                            onEdit = { editingMedication = med },
                            onDelete = { deletingMedication = med },
                            onQuickStockChange = { delta ->
                                onQuickStockChange(med.id, delta)
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Medicine
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryTeal,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_medication_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة دواء")
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة دواء", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Medicine Dialog
    if (showAddDialog) {
        MedicationFormDialog(
            title = "إضافة دواء جديد للمخزون",
            initialMedication = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newMed ->
                onAddMedication(newMed)
                showAddDialog = false
            }
        )
    }

    // Edit Medicine Dialog
    if (editingMedication != null) {
        MedicationFormDialog(
            title = "تعديل بيانات الدواء",
            initialMedication = editingMedication,
            onDismiss = { editingMedication = null },
            onConfirm = { updatedMed ->
                onUpdateMedication(updatedMed)
                editingMedication = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (deletingMedication != null) {
        AlertDialog(
            onDismissRequest = { deletingMedication = null },
            title = {
                Text(
                    text = "تأكيد حذف الدواء",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف \"${deletingMedication?.tradeName}\" من المخزون نهائياً؟"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deletingMedication?.let { onDeleteMedication(it) }
                        deletingMedication = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingMedication = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun InventoryMedicineCard(
    medication: Medication,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickStockChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${medication.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (medication.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = medication.imageUrl,
                            contentDescription = medication.tradeName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        DosageFormIcon(
                            dosageForm = medication.dosageForm,
                            size = 48.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = medication.tradeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (medication.scientificName.isNotBlank()) {
                            Text(
                                text = medication.scientificName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${medication.category} • ${medication.dosageForm} • ${medication.packageUnit}",
                            fontSize = 11.sp,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action Buttons: Edit & Delete
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_med_${medication.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_med_${medication.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Stock adjustments and price row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    StockBadge(
                        currentStock = medication.currentStock,
                        minStock = medication.minStockAlert,
                        unit = medication.packageUnit
                    )
                    if (medication.unitPrice > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "السعر: ${String.format(Locale.US, "%.2f", medication.unitPrice)} ر.س",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick stock steppers (-1, +1, +5)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "تعديل سريع:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Surface(
                        onClick = { onQuickStockChange(-1) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        enabled = medication.currentStock > 0
                    ) {
                        Text(
                            text = "-1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (medication.currentStock > 0) MaterialTheme.colorScheme.error else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        onClick = { onQuickStockChange(1) },
                        shape = RoundedCornerShape(8.dp),
                        color = TealContainer
                    ) {
                        Text(
                            text = "+1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        onClick = { onQuickStockChange(10) },
                        shape = RoundedCornerShape(8.dp),
                        color = TealContainer
                    ) {
                        Text(
                            text = "+10",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationFormDialog(
    title: String,
    initialMedication: Medication?,
    onDismiss: () -> Unit,
    onConfirm: (Medication) -> Unit
) {
    var tradeName by remember { mutableStateOf(initialMedication?.tradeName.orEmpty()) }
    var scientificName by remember { mutableStateOf(initialMedication?.scientificName.orEmpty()) }
    var category by remember { mutableStateOf(initialMedication?.category ?: "مسكنات وخافضات حرارة") }
    var dosageForm by remember { mutableStateOf(initialMedication?.dosageForm ?: "أقراص") }
    var packageUnit by remember { mutableStateOf(initialMedication?.packageUnit ?: "علبة") }
    var stockStr by remember { mutableStateOf(initialMedication?.currentStock?.toString() ?: "20") }
    var minStockStr by remember { mutableStateOf(initialMedication?.minStockAlert?.toString() ?: "10") }
    var priceStr by remember { mutableStateOf(initialMedication?.unitPrice?.let { if (it > 0) it.toString() else "" } ?: "25.0") }
    var manufacturer by remember { mutableStateOf(initialMedication?.manufacturer.orEmpty()) }
    var notes by remember { mutableStateOf(initialMedication?.notes.orEmpty()) }
    var selectedImageUri by remember { mutableStateOf<String?>(initialMedication?.imageUrl) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    var hasAttempted by remember { mutableStateOf(false) }
    val isTradeNameValid = tradeName.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photo Picker Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    if (!selectedImageUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "صورة الدواء",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .clickable { selectedImageUri = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "حذف الصورة",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp).size(16.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إضافة صورة الدواء",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                                Text(
                                    text = "اضغط لاختيار صورة من المعرض",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = tradeName,
                    onValueChange = { tradeName = it },
                    label = { Text("الاسم التجاري للدواء *") },
                    placeholder = { Text("مثال: Panadol Extra") },
                    singleLine = true,
                    isError = hasAttempted && !isTradeNameValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_trade_name"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = scientificName,
                    onValueChange = { scientificName = it },
                    label = { Text("الاسم العلمي / المادة الفعالة") },
                    placeholder = { Text("مثال: Paracetamol + Caffeine") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف") },
                        placeholder = { Text("مضادات، مسكنات...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = dosageForm,
                        onValueChange = { dosageForm = it },
                        label = { Text("الشكل الدوائي") },
                        placeholder = { Text("أقراص، شراب...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = packageUnit,
                        onValueChange = { packageUnit = it },
                        label = { Text("وحدة العبوة") },
                        placeholder = { Text("علبة، شريط، باكت") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("السعر التقديري (ر.س)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("الكمية المتوفرة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = { Text("حد التنبيه بالنقص") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = { Text("الشركة المنتجة / المستودع") },
                    placeholder = { Text("مثال: GSK, Novartis, Pfizer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    placeholder = { Text("تنبيهات الاستخدام أو مكان الحفظ") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    hasAttempted = true
                    if (isTradeNameValid) {
                        val stock = stockStr.toIntOrNull() ?: 0
                        val minStock = minStockStr.toIntOrNull() ?: 10
                        val price = priceStr.toDoubleOrNull() ?: 0.0

                        val updated = (initialMedication ?: Medication(tradeName = "")).copy(
                            tradeName = tradeName.trim(),
                            scientificName = scientificName.trim(),
                            category = category.trim().ifBlank { "عام" },
                            dosageForm = dosageForm.trim().ifBlank { "أقراص" },
                            packageUnit = packageUnit.trim().ifBlank { "علبة" },
                            currentStock = stock,
                            minStockAlert = minStock,
                            unitPrice = price,
                            manufacturer = manufacturer.trim(),
                            imageUrl = selectedImageUri.orEmpty(),
                            notes = notes.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onConfirm(updated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                modifier = Modifier.testTag("form_submit_btn")
            ) {
                Text(if (initialMedication != null) "حفظ التعديلات" else "إضافة للمخزون")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
