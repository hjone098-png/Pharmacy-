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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.util.ImageStorageUtil
import com.example.ui.viewmodel.MedicationSortOrder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.DosageFormIcon
import com.example.ui.components.OrderPreSubmissionSummaryDialog
import com.example.ui.util.formatOrderSlip
import com.example.ui.util.toArabicDigits
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
    sortOrder: MedicationSortOrder = MedicationSortOrder.TIME_ADDED,
    sortAscending: Boolean = false,
    onSortOrderChange: (MedicationSortOrder) -> Unit = {},
    onToggleSortDirection: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onQuantityChange: (Medication, Int) -> Unit,
    onClearCart: () -> Unit,
    onProceedToSummary: (depotName: String) -> Unit,
    onAddNewMedicationWithImage: (Medication) -> Unit,
    onDeleteMedication: (Medication) -> Unit,
    onChangePharmacyName: () -> Unit,
    onUpdateCartItemQuantity: ((Long, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var showOrderSummaryPreview by remember { mutableStateOf(false) }
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
                        Surface(
                            shape = CircleShape,
                            color = TealContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalPharmacy,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
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

                    // Button for adding/changing pharmacy and location: ONLY '+' symbol without any words
                    Surface(
                        shape = CircleShape,
                        color = PrimaryTeal,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onChangePharmacyName() }
                            .testTag("add_pharmacy_plus_btn")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة وتعديل الصيدلية والموقع",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
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

                Spacer(modifier = Modifier.height(6.dp))

                // Sort Options Bar (وقت الإضافة / ترتيب الإضافة / حسب الأحرف الأبجدية)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "فرز",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "فرز حسب:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val sortOpts = listOf(
                            Triple(MedicationSortOrder.TIME_ADDED, "وقت الإضافة", Icons.Default.AccessTime),
                            Triple(MedicationSortOrder.ADDITION_ORDER, "ترتيب الإضافة", Icons.Default.FormatListNumbered),
                            Triple(MedicationSortOrder.ALPHABETICAL, "الأبجدية (أ-ي)", Icons.Default.SortByAlpha)
                        )

                        sortOpts.forEach { (order, label, icon) ->
                            val isSelected = sortOrder == order
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSortOrderChange(order) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryTeal,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.testTag("sort_chip_${order.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sort Direction Toggle Button (تصاعدي / تنازلي)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .height(32.dp)
                            .clickable { onToggleSortDirection() }
                            .testTag("sort_direction_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.North else Icons.Default.South,
                                contentDescription = if (sortAscending) "تصاعدي" else "تنازلي",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (sortAscending) "تصاعدي" else "تنازلي",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChangePharmacyName() }
                                    .background(TealContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalPharmacy,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = bannerDisplay,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryTeal
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryTeal,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "إضافة الصيدلية والموقع",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
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
                    // Clickable column with item counts and total price to open preview
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showOrderSummaryPreview = true }
                            .padding(vertical = 2.dp)
                    ) {
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
                                text = "المجموع: ${formatIraqiDinar(totalEstimatedPrice)} • اضغط للمعاينة",
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

                        // Preview & Submit Button
                        Button(
                            onClick = { showOrderSummaryPreview = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("send_and_print_order_btn")
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "معاينة الطلبية ($totalSelectedItems) ➔",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Comprehensive Pre-Submission Summary View Dialog
    if (showOrderSummaryPreview) {
        OrderPreSubmissionSummaryDialog(
            cartItems = cartItems,
            pharmacyName = rawPharmacyName,
            pharmacyLocation = rawLocation,
            initialDepotName = selectedDepotName,
            onQuantityChange = { medicationId, newQty ->
                if (onUpdateCartItemQuantity != null) {
                    onUpdateCartItemQuantity(medicationId, newQty)
                } else {
                    val med = medications.find { it.id == medicationId }
                    if (med != null) {
                        onQuantityChange(med, newQty)
                    }
                }
            },
            onDismiss = { showOrderSummaryPreview = false },
            onSubmitOrder = { finalDepot ->
                selectedDepotName = finalDepot
                showOrderSummaryPreview = false
                onProceedToSummary(finalDepot)
            }
        )
    }

    // Dialog for selecting Drug Depot (تحديد المذخر: من أي مذخر؟)
    if (showDepotDialog) {
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
                        text = "اكتب اسم المذخر الموجهة إليه الطلبية لكتابته في نهاية نموذج الطلبية (${bannerDisplay} ... (اسم المذخر)):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = depotInput,
                        onValueChange = { depotInput = it },
                        label = { Text("اسم المذخر *") },
                        placeholder = { Text("اكتب اسم المذخر (مثال: مذخر سبأ)") },
                        leadingIcon = {
                            Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryTeal)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_depot_name_input")
                    )
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
                    Text("إصدار وحفظ الطلبية ➔", fontWeight = FontWeight.Bold)
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tradeName by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var packageUnit by remember { mutableStateOf("علبة") }
    var dosageForm by remember { mutableStateOf("أقراص") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var isSavingPhoto by remember { mutableStateOf(false) }
    var hasAttempted by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isSavingPhoto = true
                val internalPath = ImageStorageUtil.saveImageToInternalStorage(context, uri)
                selectedImageUri = internalPath
                isSavingPhoto = false
            }
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
                // Photo Picker Card with Internal Storage
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSavingPhoto) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    if (isSavingPhoto) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "جاري حفظ الصورة في ذاكرة التطبيق الدائمة...",
                                    fontSize = 11.sp,
                                    color = PrimaryTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (selectedImageUri != null) {
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

                            // Permanent internal storage badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryTeal.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "محفوظة داخلياً في التطبيق",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

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
