package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.OrderItem
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TealContainer
import com.example.ui.util.formatIraqiDinar
import com.example.ui.util.formatOrderSlip
import com.example.ui.util.toArabicDigits

/**
 * Summary view dialog that calculates the total number of items ordered and
 * displays an interactive preview list of all selected medications before submitting the order.
 */
@Composable
fun OrderPreSubmissionSummaryDialog(
    cartItems: Map<Long, OrderItem>,
    pharmacyName: String,
    pharmacyLocation: String,
    initialDepotName: String,
    onQuantityChange: (medicationId: Long, newQuantity: Int) -> Unit,
    onDismiss: () -> Unit,
    onSubmitOrder: (depotName: String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var depotInput by remember(initialDepotName) {
        mutableStateOf(initialDepotName.ifBlank { "مذخر سبأ" })
    }
    var copiedToast by remember { mutableStateOf(false) }

    // Calculations of Total Items Ordered
    val itemsList = cartItems.values.toList()
    val totalItemsCount = itemsList.size
    val totalUnitsCount = itemsList.sumOf { it.quantity }
    val totalEstimatedPrice = itemsList.sumOf { it.subtotal }

    // Calculation of Cartons (24 items = 1 carton)
    val exactCartonItemsCount = itemsList.count { it.quantity == 24 }
    val totalCartonsEquivalent = totalUnitsCount / 24
    val remainingLooseUnits = totalUnitsCount % 24

    // Slip text preview
    val currentSlipText = remember(pharmacyName, pharmacyLocation, itemsList, depotInput) {
        formatOrderSlip(
            pharmacyName = pharmacyName,
            pharmacyLocation = pharmacyLocation,
            items = itemsList,
            depotName = depotInput.trim().ifBlank { "مذخر سبأ" }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("order_preview_summary_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Surface(
                    color = PrimaryTeal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "معاينة وملخص الطلبية قبل الإرسال",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "مراجعة المواد المحسوبة وتحديد المذخر",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_order_preview_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(2.dp))

                        // Target Pharmacy & Location Info Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalPharmacy,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "الطلبية موجهة باسم الصيدلية والموقع:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val prefix = if (pharmacyName.startsWith("ص.") || pharmacyName.startsWith("صيدلية")) pharmacyName else "ص. $pharmacyName"
                                    val fullLoc = if (pharmacyLocation.isNotBlank()) "$prefix / $pharmacyLocation" else prefix
                                    Text(
                                        text = fullLoc,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PrimaryTeal
                                    )
                                }
                            }
                        }
                    }

                    // Total Calculation Cards (Calculates total number of items ordered)
                    item {
                        Text(
                            text = "حساب مجاميع الطلبية (Total Ordered):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // 4 Metrics Grid (2x2)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Total Distinct Items
                                CalculationMetricCard(
                                    title = "عدد المواد (الأصناف)",
                                    value = "$totalItemsCount أصناف",
                                    subValue = "أدوية مختلفة",
                                    icon = Icons.Default.ReceiptLong,
                                    containerColor = TealContainer.copy(alpha = 0.5f),
                                    accentColor = PrimaryTeal,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preview_total_items_badge")
                                )

                                // 2. Total Units (العبوات)
                                CalculationMetricCard(
                                    title = "إجمالي العبوات",
                                    value = "$totalUnitsCount عبوة",
                                    subValue = "(${toArabicDigits(totalUnitsCount)} عبوة مطلوبة)",
                                    icon = Icons.Default.ShoppingCart,
                                    containerColor = Color(0xFFE0F2FE).copy(alpha = 0.7f),
                                    accentColor = SecondaryCyan,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preview_total_units_badge")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 3. Cartons Calculation (24 units = 1 carton)
                                CalculationMetricCard(
                                    title = "حساب الكراتين (24=1)",
                                    value = if (totalCartonsEquivalent > 0) {
                                        "$totalCartonsEquivalent كارتون" + if (remainingLooseUnits > 0) " و $remainingLooseUnits" else ""
                                    } else {
                                        "$totalUnitsCount عبوة مفردة"
                                    },
                                    subValue = if (exactCartonItemsCount > 0) "$exactCartonItemsCount صنف بطلب كارتون كامل" else "الكرتون = 24 عبوة",
                                    icon = Icons.Default.Store,
                                    containerColor = Color(0xFFFEF3C7).copy(alpha = 0.7f),
                                    accentColor = Color(0xFFB45309),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preview_cartons_badge")
                                )

                                // 4. Total Price in IQD
                                CalculationMetricCard(
                                    title = "المجموع التقديري",
                                    value = formatIraqiDinar(totalEstimatedPrice),
                                    subValue = "بالدينار العراقي",
                                    icon = Icons.Default.ReceiptLong,
                                    containerColor = Color(0xFFDCFCE7).copy(alpha = 0.7f),
                                    accentColor = Color(0xFF15803D),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preview_total_price_badge")
                                )
                            }
                        }
                    }

                    // Depot Selector (المذخر الموجهة إليه الطلبية)
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "المذخر الموجهة إليه الطلبية (من أي مذخر؟):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PrimaryTeal
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Depot Chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("مذخر سبأ", "مذخر دجلة", "مذخر بابل", "مذخر السلام", "مذخر النور", "مذخر الفارابي").forEach { depot ->
                                        val isSelected = depotInput.trim() == depot
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { depotInput = depot },
                                            label = { Text(depot, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TealContainer,
                                                selectedLabelColor = PrimaryTeal
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = depotInput,
                                    onValueChange = { depotInput = it },
                                    label = { Text("اسم المذخر في النموذج النهائي *") },
                                    placeholder = { Text("مثال: مذخر سبأ") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("preview_depot_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // Section Title: Preview list of all selected medications
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "قائمة معاينة الأدوية المطلوبة (${itemsList.size} أصناف):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "استمر بالضغط على (+) لزيادة 10",
                                fontSize = 10.sp,
                                color = PrimaryTeal
                            )
                        }
                    }

                    // Empty or List of items
                    if (itemsList.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "لم يتم اختيار أي دواء حتى الآن",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "يرجى الرجوع للكتالوج واختيار المواد المراد طلبها.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = itemsList,
                            key = { _, item -> item.medicationId }
                        ) { index, item ->
                            PreviewMedicationRow(
                                index = index + 1,
                                item = item,
                                onSelectQuantity = { qty ->
                                    onQuantityChange(item.medicationId, qty)
                                },
                                onIncrement10 = {
                                    onQuantityChange(item.medicationId, item.quantity + 1)
                                },
                                onDecrement10 = {
                                    onQuantityChange(item.medicationId, (item.quantity - 1).coerceAtLeast(0))
                                },
                                onSetCarton24 = {
                                    onQuantityChange(item.medicationId, 24)
                                },
                                onRemove = {
                                    onQuantityChange(item.medicationId, 0)
                                }
                            )
                        }
                    }

                    // Live Formatted Slip Preview
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "معاينة نص الطلبية النهائي (نموذج الواتساب):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PrimaryTeal
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = TealContainer,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(currentSlipText))
                                            copiedToast = true
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = PrimaryTeal,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (copiedToast) "تم النسخ ✓" else "نسخ النص",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryTeal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = currentSlipText,
                                        fontSize = 13.sp,
                                        lineHeight = 22.sp,
                                        color = Color(0xFF0F172A),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Sticky Bottom Action Bar inside Dialog
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "إجمالي الأصناف: $totalItemsCount مواد ($totalUnitsCount عبوة)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (totalEstimatedPrice > 0) {
                                    Text(
                                        text = "المبلغ: ${formatIraqiDinar(totalEstimatedPrice)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PrimaryTeal
                                    )
                                }
                            }

                            Text(
                                text = "المذخر: ${depotInput.trim().ifBlank { "مذخر سبأ" }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Back / Continue selecting items
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("preview_back_to_catalog_btn")
                            ) {
                                Text(
                                    text = "تعديل / إضافة مواد أخرى",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Confirm and Submit Order
                            Button(
                                onClick = {
                                    val finalDepot = depotInput.trim().ifBlank { "مذخر سبأ" }
                                    onSubmitOrder(finalDepot)
                                },
                                enabled = itemsList.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(46.dp)
                                    .testTag("confirm_and_submit_order_btn")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تأكيد وإصدار الطلبية ➔",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationMetricCard(
    title: String,
    value: String,
    subValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subValue,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PreviewMedicationRow(
    index: Int,
    item: OrderItem,
    onSelectQuantity: (Int) -> Unit,
    onIncrement10: () -> Unit,
    onDecrement10: () -> Unit,
    onSetCarton24: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("preview_med_item_${item.medicationId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image or Icon
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.tradeName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    DosageFormIcon(
                        dosageForm = item.dosageForm,
                        size = 46.dp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$index. ${item.tradeName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.dosageForm} • ${item.packageUnit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.unitPrice > 0) {
                        Text(
                            text = "سعر المفرد: ${formatIraqiDinar(item.unitPrice)}",
                            fontSize = 10.sp,
                            color = SecondaryCyan
                        )
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف المادة من الطلبية",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )

            // Unit Selection Quick Options: قطعة واحدة، عشر قطع، 24، 50، 100
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val previewUnitOpts = listOf(
                    1 to "قطعة واحدة (1)",
                    10 to "عشر قطع (10)",
                    24 to "24 (كارتون واحد)",
                    50 to "50",
                    100 to "100"
                )
                previewUnitOpts.forEach { (qty, label) ->
                    val isSelected = item.quantity == qty
                    Surface(
                        onClick = {
                            if (qty == 24) onSetCarton24()
                            else onSelectQuantity(qty)
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else PrimaryTeal
                            )
                        }
                    }
                }
            }

            // Stepper and Quantity Display Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantity Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.quantity == 24) Color(0xFFFEF3C7) else TealContainer
                ) {
                    Text(
                        text = if (item.quantity == 24) {
                            "📦 كارتون واحد (24 ${item.packageUnit})"
                        } else {
                            "${item.quantity} ${item.packageUnit} (${toArabicDigits(item.quantity)})"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.quantity == 24) Color(0xFFB45309) else PrimaryTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Interactive Stepper Controls (- / +)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Minus Button
                    RepeatingIconButton(
                        onAction = onDecrement10,
                        initialDelayMillis = 280L,
                        repeatIntervalMillis = 60L,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "إنقاص",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${item.quantity}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = PrimaryTeal,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )

                    // Plus Button
                    RepeatingIconButton(
                        onAction = onIncrement10,
                        initialDelayMillis = 280L,
                        repeatIntervalMillis = 60L,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "زيادة",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
