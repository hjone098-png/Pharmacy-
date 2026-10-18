package com.example.ui.screens.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderEntity
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TealContainer
import com.example.ui.util.formatIraqiDinar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * قائمة الطلبات المسجلة (التي يسجلها المستخدم فقط)
 */
@Composable
fun OrderHistoryScreen(
    orders: List<OrderEntity>,
    onViewOrder: (OrderEntity) -> Unit,
    onDeleteOrder: (OrderEntity) -> Unit,
    onCopyToClipboard: (String) -> Unit,
    onShareOrder: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    var searchQuery by remember { mutableStateOf("") }
    var orderToDelete by remember { mutableStateOf<OrderEntity?>(null) }

    val filteredOrders = remember(orders, searchQuery) {
        if (searchQuery.isBlank()) {
            orders
        } else {
            val q = searchQuery.trim().lowercase()
            orders.filter { order ->
                order.pharmacyName.lowercase().contains(q) ||
                        order.fullAddress.lowercase().contains(q) ||
                        order.supplierName.lowercase().contains(q) ||
                        order.orderNumber.lowercase().contains(q) ||
                        order.formattedCopyText.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("order_history_screen")
    ) {
        // Header & Search Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "قائمة الطلبات المسجلة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TealContainer
                    ) {
                        Text(
                            text = "${orders.size} طلبية",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (orders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث باسم الصيدلية، الموقع، أو المذخر...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_history_orders")
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "لا توجد نتائج مطابقة للبحث" else "لا توجد طلبات مسجلة بعد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "جرب البحث بكلمة أخرى" else "عند انتهاء وإصدار أي طلبية بالنموذج، ستسجل تلقائياً هنا في هذه القائمة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onView = { onViewOrder(order) },
                        onCopy = { onCopyToClipboard(order.formattedCopyText) },
                        onShare = { onShareOrder(order.formattedCopyText) },
                        onDelete = { orderToDelete = order }
                    )
                }
            }
        }
    }

    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("حذف الطلبية من القائمة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف الطلبية الخاصة بـ (${orderToDelete?.pharmacyName})؟") },
            confirmButton = {
                Button(
                    onClick = {
                        orderToDelete?.let { onDeleteOrder(it) }
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun OrderHistoryCard(
    order: OrderEntity,
    onView: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date(order.createdAt))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order Number + Urgency
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (order.urgencyLevel) {
                        "طارئ" -> Color(0xFFFEE2E2)
                        "عاجل" -> Color(0xFFFEF3C7)
                        else -> TealContainer
                    }
                ) {
                    Text(
                        text = order.urgencyLevel,
                        color = when (order.urgencyLevel) {
                            "طارئ" -> Color(0xFFDC2626)
                            "عاجل" -> Color(0xFFD97706)
                            else -> PrimaryTeal
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Pharmacy Name and Location
            val cleanPharm = when {
                order.pharmacyName.isBlank() -> ""
                order.pharmacyName.startsWith("ص.") || order.pharmacyName.startsWith("ص /") || order.pharmacyName.startsWith("صيدلية") -> order.pharmacyName
                else -> "ص. ${order.pharmacyName}"
            }
            val locationPart = if (order.fullAddress.isNotBlank()) {
                if (cleanPharm.isNotBlank()) " / ${order.fullAddress}" else order.fullAddress
            } else ""
            val titleText = when {
                cleanPharm.isNotBlank() && locationPart.isNotBlank() -> "$cleanPharm$locationPart"
                cleanPharm.isNotBlank() -> cleanPharm
                locationPart.isNotBlank() -> locationPart
                else -> "طلبية بدون اسم صيدلية"
            }
            Text(
                text = titleText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryTeal
            )

            // Depot Name
            if (order.supplierName.isNotBlank()) {
                val cleanDepot = if (order.supplierName.startsWith("مذخر")) order.supplierName else "مذخر ${order.supplierName}"
                Text(
                    text = "($cleanDepot)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryCyan
                )
            }

            Text(
                text = dateStr,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Text preview of the exact order model slip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "نموذج الطلبية المسجل:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = order.formattedCopyText,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.totalItemsCount} أصناف (${order.totalUnitsCount} عبوة)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (order.totalEstimatedPrice > 0) {
                    Text(
                        text = formatIraqiDinar(order.totalEstimatedPrice),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCopy,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ النموذج", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onView,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("عرض", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onShare,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", modifier = Modifier.size(14.dp))
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف الطلبية",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
