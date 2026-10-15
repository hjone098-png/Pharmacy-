package com.example.ui.screens.order

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.Medication
import com.example.data.local.OrderItem
import com.example.data.local.PharmacyProfile
import com.example.ui.components.MedicineGridCard
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.TealContainer
import com.example.ui.util.formatIraqiDinar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCreationScreen(
    medications: List<Medication>,
    cartItems: Map<Long, OrderItem>,
    pharmacyProfile: PharmacyProfile?,
    currentPharmacyName: String,
    currentPharmacyLocation: String = "",
    currentOrderDepot: String = "مذخر سبأ",
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onQuantityChange: (Medication, Int) -> Unit,
    onClearCart: () -> Unit,
    onProceedToSummary: (depotName: String) -> Unit,
    onAddNewMedicationWithImage: (Medication) -> Unit,
    onDeleteMedication: (Medication) -> Unit,
    onChangePharmacyName: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var showDepotDialog by remember { mutableStateOf(false) }
    var selectedDepotName by remember(currentOrderDepot) {
        mutableStateOf(currentOrderDepot.ifBlank { "مذخر سبأ" })
    }

    val categories = listOf(
        "الكل",
        "عام",
        "مسكنات وخافضات حرارة",
        "مضادات حيوية",
        "الجهاز الهضمي",
        "أدوية القلب والضغط",
        "الحساسية والتنفسي",
        "فيتامينات ومكملات",
        "أدوية السكري",
        "مستلزمات طبية"
    )

    val totalSelectedItems = cartItems.size
    val totalSelectedUnits = cartItems.values.sumOf { it.quantity }
    val totalEstimatedPrice = cartItems.values.sumOf { it.subtotal }

    val rawPharmacyName = currentPharmacyName.ifBlank {
        pharmacyProfile?.pharmacyName?.ifBlank { "السنار" } ?: "السنار"
    }
    val rawLocation = currentPharmacyLocation.ifBlank {
        pharmacyProfile?.neighborhood?.ifBlank { pharmacyProfile.city } ?: "حي سومر"
    }

    val cleanPharmPrefix = if (rawPharmacyName.startsWith("ص.") || rawPharmacyName.startsWith("ص /") || rawPharmacyName.startsWith("صيدلية")) {
        rawPharmacyName
    } else {
        "ص. $rawPharmacyName"
    }

    val bannerDisplay = if (rawLocation.isNotBlank()) {
        "$cleanPharmPrefix / $rawLocation"
    } else {
        cleanPharmPrefix
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("order_creation_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Target Pharmacy Banner (+ Button and Name)
            Surface(
                color = TealContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChangePharmacyName() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Prominent + icon next to pharmacy
                        Surface(
                            shape = CircleShape,
                            color = PrimaryTeal,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { onChangePharmacyName() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "بدء طلبية باسم الصيدلية والموقع",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(7.dp)
                                    .size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الطلبية موجهة إلى الصيدلية والموقع:",
                                fontSize = 11.sp,
                                color = PrimaryTeal
                            )
                            Text(
                                text = bannerDisplay,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Button to change or enter pharmacy name and location
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.clickable { onChangePharmacyName() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الصيدلية والموقع",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "الصيدلية والموقع (+)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                    }
                }
            }

            // Search Bar & Add Medicine Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_medicine_input"),
                        placeholder = { Text("بحث في المواد المضافة...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = PrimaryTeal
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Add medicine with photo and price button
                    Button(
                        onClick = { showAddMedicineDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("add_medicine_photo_btn")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مادة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Currency Banner (دينار عراقي)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🇮🇶 الأسعار بالدينار العراقي (د.ع)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )

                    if (totalSelectedItems > 0) {
                        Text(
                            text = "$totalSelectedItems مادة محددة في السلة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                }
            }

            // Quick instruction tip
            if (medications.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 اضغط على صورة المادة ليظهر لك مربع صغير لتحديد العدد المطلوب، ثم اضغط (أوكي)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // Visual Grid of User Added Medicines
            if (medications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TealContainer,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "لا توجد مواد مضافة حالياً",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "أضف المواد التي ترغب بها (الصورة + اسم الدواء + السعر بالدينار العراقي)،\nأو اضغط علامة (+) لبدء طلبية باسم الصيدلية.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { showAddMedicineDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("empty_add_medicine_btn")
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إضافة مادة جديدة (صورة + اسم + سعر د.ع)", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                enabled = false,
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChangePharmacyName() },
                                placeholder = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryTeal)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("اضغط هنا لكتابة اسم الصيدلية (+)", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = if (totalSelectedItems > 0) 100.dp else 24.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = medications,
                        key = { it.id }
                    ) { medication ->
                        val selectedQty = cartItems[medication.id]?.quantity ?: 0
                        MedicineGridCard(
                            medication = medication,
                            selectedQuantity = selectedQty,
                            onQuantityConfirmed = { newQty ->
                                onQuantityChange(medication, newQty)
                            },
                            onDeleteMedication = {
                                onDeleteMedication(medication)
                            }
                        )
                    }
                }
            }
        }

        // Sticky Bottom Bar: إرسال وطباعة الطلبية
        AnimatedVisibility(
            visible = totalSelectedItems > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sticky_order_summary_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalSelectedItems مواد ($totalSelectedUnits عبوة)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (totalEstimatedPrice > 0) {
                            Text(
                                text = "المجموع: ${formatIraqiDinar(totalEstimatedPrice)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onClearCart,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "تفريغ السلة",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = { showDepotDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("send_and_print_order_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تم انتهاء الطلبية (تحديد المذخر) ➔",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog for selecting Drug Depot (تحديد المذخر: من أي مذخر؟)
    if (showDepotDialog) {
        val popularDepots = listOf(
            "مذخر سبأ",
            "مذخر دجلة",
            "مذخر بابل",
            "مذخر النور",
            "مذخر السلام",
            "مذخر الفارابي",
            "مذخر الرشيد",
            "مذخر الأمل"
        )
        var depotInput by remember { mutableStateOf(selectedDepotName) }

        AlertDialog(
            onDismissRequest = { showDepotDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحديد المذخر (من أي مذخر؟)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "حدد اسم المذخر الموجهة إليه الطلبية لكتابته في نهاية نموذج الطلبية (${bannerDisplay} ... (اسم المذخر)):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = depotInput,
                        onValueChange = { depotInput = it },
                        label = { Text("اسم المذخر *") },
                        placeholder = { Text("مثال: مذخر سبأ") },
                        leadingIcon = {
                            Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryTeal)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_depot_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "مذاخر شائعة وسريعة:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularDepots.forEach { depot ->
                            val isSelected = depotInput.trim() == depot
                            FilterChip(
                                selected = isSelected,
                                onClick = { depotInput = depot },
                                label = { Text(depot, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealContainer,
                                    selectedLabelColor = PrimaryTeal
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalDepot = depotInput.trim().ifBlank { "مذخر سبأ" }
                        selectedDepotName = finalDepot
                        showDepotDialog = false
                        onProceedToSummary(finalDepot)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("dialog_confirm_depot_btn")
                ) {
                    Text("إصدار الطلبية بالنموذج ➔", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepotDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Medicine with Photo & Price in Iraqi Dinar Dialog
    if (showAddMedicineDialog) {
        AddMedicineWithPhotoDialog(
            onDismiss = { showAddMedicineDialog = false },
            onConfirm = { newMed ->
                onAddNewMedicationWithImage(newMed)
                showAddMedicineDialog = false
            }
        )
    }
}

@Composable
fun AddMedicineWithPhotoDialog(
    onDismiss: () -> Unit,
    onConfirm: (Medication) -> Unit
) {
    var tradeName by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var packageUnit by remember { mutableStateOf("علبة") }
    var dosageForm by remember { mutableStateOf("أقراص") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var hasAttempted by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    val isNameValid = tradeName.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مادة جديدة (صورة + اسم + سعر د.ع)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo Picker Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "صورة المادة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clickable { selectedImageUri = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "حذف الصورة",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp).size(18.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اضغط لاختيار صورة الدواء من المعرض",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                            Text(
                                text = "التقط صورة العلبة أو اختر من المعرض",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Medicine Name
                OutlinedTextField(
                    value = tradeName,
                    onValueChange = { tradeName = it },
                    label = { Text("اسم المادة / الدواء *") },
                    placeholder = { Text("مثال: بنادول، أوجمنتين، كونسور...") },
                    singleLine = true,
                    isError = hasAttempted && !isNameValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_med_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Price (سعر الدواء بالدينار العراقي)
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("سعر الدواء بالدينار العراقي (د.ع) *") },
                    placeholder = { Text("مثال: 5000 أو 7500 أو 12000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_med_price_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dosageForm,
                        onValueChange = { dosageForm = it },
                        label = { Text("الشكل الدوائي") },
                        placeholder = { Text("أقراص، شراب...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = packageUnit,
                        onValueChange = { packageUnit = it },
                        label = { Text("الوحدة") },
                        placeholder = { Text("علبة، باكت...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    hasAttempted = true
                    if (isNameValid) {
                        val price = priceStr.toDoubleOrNull() ?: 0.0
                        val newMed = Medication(
                            tradeName = tradeName.trim(),
                            unitPrice = price,
                            dosageForm = dosageForm.trim().ifBlank { "أقراص" },
                            packageUnit = packageUnit.trim().ifBlank { "علبة" },
                            category = "عام",
                            currentStock = 100,
                            imageUrl = selectedImageUri.orEmpty(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onConfirm(newMed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                modifier = Modifier.testTag("dialog_save_med_btn")
            ) {
                Text("حفظ المادة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
