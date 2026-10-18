package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import com.example.ui.theme.SecondaryCyan
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.util.toArabicDigits
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.Medication
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.StockOkGreen
import com.example.ui.theme.TealContainer
import com.example.ui.util.formatIraqiDinar

@Composable
fun MedicineGridCard(
    medication: Medication,
    selectedQuantity: Int,
    onQuantityConfirmed: (Int) -> Unit,
    onDeleteMedication: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showNumberPickerDialog by remember { mutableStateOf(false) }

    val isSelected = selectedQuantity > 0
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        label = "border_color"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TealContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("med_grid_card_${medication.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Medicine Image Container - Click to adjust quantity (+10 continuous or 1 carton)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable {
                        showNumberPickerDialog = true
                    }
            ) {
                if (medication.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = medication.imageUrl,
                        contentDescription = medication.tradeName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback visual illustration
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        DosageFormIcon(
                            dosageForm = medication.dosageForm,
                            size = 64.dp
                        )
                    }
                }

                // Price Badge in Iraqi Dinar (د.ع)
                Surface(
                    shape = RoundedCornerShape(topStart = 12.dp),
                    color = PrimaryTeal,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = formatIraqiDinar(medication.unitPrice),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Selection Counter Badge (Top-Start)
                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        color = StockOkGreen,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (selectedQuantity == 24) "كارتون واحد (٢٤)" else "$selectedQuantity ${medication.packageUnit}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Delete button (Top-End) if enabled
                if (onDeleteMedication != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clickable { onDeleteMedication() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف الدواء",
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp).size(16.dp)
                        )
                    }
                }
            }

            // Name and Details Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = medication.tradeName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (medication.scientificName.isNotBlank()) {
                    Text(
                        text = medication.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${medication.dosageForm} • ${medication.packageUnit}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 1) Direct Unit Selection Options: قطعة واحدة، عشر قطع، 24 (كارتون واحد)، 50، 100
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Row 1: قطعة واحدة (1), عشر قطع (10), 24 (كارتون)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val row1Units = listOf(
                            1 to "١ قطعة",
                            10 to "١٠ قطع",
                            24 to "٢٤ كارتون"
                        )
                        row1Units.forEach { (qty, label) ->
                            val isChosen = selectedQuantity == qty
                            Surface(
                                onClick = { onQuantityConfirmed(qty) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isChosen) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .testTag("card_unit_${medication.id}_$qty")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isChosen) Color.White else PrimaryTeal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row 2: 50, 100
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val row2Units = listOf(
                            50 to "٥٠ قطعة",
                            100 to "١٠٠ قطعة"
                        )
                        row2Units.forEach { (qty, label) ->
                            val isChosen = selectedQuantity == qty
                            Surface(
                                onClick = { onQuantityConfirmed(qty) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isChosen) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .testTag("card_unit_${medication.id}_$qty")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isChosen) Color.White else PrimaryTeal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2) Responsive Stepper Controls: Minus (-), Count / Subtotal, Plus (+)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) TealContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Minus Button (-)
                        RepeatingIconButton(
                            onAction = { onQuantityConfirmed((selectedQuantity - 1).coerceAtLeast(0)) },
                            enabled = selectedQuantity > 0,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedQuantity > 0) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .testTag("card_minus_${medication.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "إنقاص 1",
                                tint = if (selectedQuantity > 0) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Center Quantity and Label (Click to open full dialog)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showNumberPickerDialog = true }
                        ) {
                            Text(
                                text = "$selectedQuantity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selectedQuantity == 24) {
                                Text(
                                    text = "كارتون واحد",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            } else if (selectedQuantity > 0 && medication.unitPrice > 0) {
                                Text(
                                    text = formatIraqiDinar(selectedQuantity * medication.unitPrice),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SecondaryCyan
                                )
                            }
                        }

                        // Plus Button (+)
                        RepeatingIconButton(
                            onAction = { onQuantityConfirmed(selectedQuantity + 1) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal)
                                .testTag("card_plus_${medication.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "زيادة 1",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Direct Quantity Dialog when tapping medicine photo: Increment 10 by 10 with continuous press
    if (showNumberPickerDialog) {
        var dialogQuantity by remember(selectedQuantity, showNumberPickerDialog) {
            mutableIntStateOf(if (selectedQuantity > 0) selectedQuantity else 10)
        }

        AlertDialog(
            onDismissRequest = { showNumberPickerDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = medication.tradeName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${medication.dosageForm} • ${medication.packageUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "اختر من الوحدات السريعة أو استخدم أزرار (+) و (-) للتحكم الدقيق:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Requested Unit Options Grid: قطعة واحدة، عشر قطع، 24، 50، 100
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Row 1: قطعة واحدة (1), عشر قطع (10), 24 (كارتون واحد)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val optionsRow1 = listOf(
                                1 to "قطعة واحدة (1)",
                                10 to "عشر قطع (10)",
                                24 to "24 (كارتون واحد)"
                            )
                            optionsRow1.forEach { (qty, label) ->
                                val isSelectedUnit = dialogQuantity == qty
                                Surface(
                                    onClick = { dialogQuantity = qty },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelectedUnit) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelectedUnit) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    ),
                                    shadowElevation = if (isSelectedUnit) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("unit_opt_$qty")
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelectedUnit) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isSelectedUnit) Color.White else PrimaryTeal,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Row 2: 50, 100
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val optionsRow2 = listOf(
                                50 to "50 قطعة",
                                100 to "100 قطعة"
                            )
                            optionsRow2.forEach { (qty, label) ->
                                val isSelectedUnit = dialogQuantity == qty
                                Surface(
                                    onClick = { dialogQuantity = qty },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelectedUnit) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelectedUnit) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    ),
                                    shadowElevation = if (isSelectedUnit) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("unit_opt_$qty")
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelectedUnit) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isSelectedUnit) Color.White else PrimaryTeal,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Responsive Stepper Controls: Minus (-) and Plus (+) buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Minus (-) Button (fast response: single tap decrements, hold repeats smoothly)
                        RepeatingIconButton(
                            onAction = { dialogQuantity = (dialogQuantity - 1).coerceAtLeast(0) },
                            enabled = dialogQuantity > 0,
                            initialDelayMillis = 280L,
                            repeatIntervalMillis = 60L,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dialogQuantity > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .testTag("dialog_minus_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "إنقاص الكمية (-)",
                                tint = if (dialogQuantity > 0) PrimaryTeal else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Quantity Display Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = TealContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.5.dp, PrimaryTeal),
                            modifier = Modifier
                                .width(140.dp)
                                .height(72.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$dialogQuantity",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryTeal,
                                    textAlign = TextAlign.Center
                                )
                                val unitDesc = when (dialogQuantity) {
                                    1 -> "قطعة واحدة (١ ${medication.packageUnit})"
                                    10 -> "عشر قطع (١٠ ${medication.packageUnit})"
                                    24 -> "كارتون واحد (٢٤ ${medication.packageUnit})"
                                    50 -> "٥٠ ${medication.packageUnit}"
                                    100 -> "١٠٠ ${medication.packageUnit}"
                                    else -> "${toArabicDigits(dialogQuantity)} ${medication.packageUnit}"
                                }
                                Text(
                                    text = unitDesc,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (dialogQuantity == 24) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Plus (+) Button (fast response: single tap increments, hold repeats smoothly)
                        RepeatingIconButton(
                            onAction = { dialogQuantity += 1 },
                            initialDelayMillis = 280L,
                            repeatIntervalMillis = 60L,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal)
                                .testTag("dialog_plus_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "زيادة الكمية (+)",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Price preview if price available
                    if (medication.unitPrice > 0 && dialogQuantity > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "المبلغ: ${formatIraqiDinar(dialogQuantity * medication.unitPrice)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryCyan
                        )
                    }

                    if (dialogQuantity > 0 || selectedQuantity > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { dialogQuantity = 0 },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text("تصفير العداد (0)")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onQuantityConfirmed(dialogQuantity)
                        showNumberPickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("dialog_confirm_qty_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (dialogQuantity == 24) "تأكيد طلب كارتون واحد (24)" else if (dialogQuantity > 0) "تأكيد طلب ($dialogQuantity)" else "حفظ (إلغاء التحديد)",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNumberPickerDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
