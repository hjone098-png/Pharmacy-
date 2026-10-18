package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.TealContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaTopBar(
    currentScreen: Screen,
    currentPharmacyName: String,
    currentPharmacyLocation: String,
    onStartNewOrderClick: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onClearAllMedications: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = currentScreen.titleAr,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Quick Pharmacy & Location Chip (Clicking it directly opens the pharmacy & location dialog)
                val displayName = currentPharmacyName.trim()
                val displayLocation = currentPharmacyLocation.trim()
                val chipText = when {
                    displayName.isNotBlank() && displayLocation.isNotBlank() -> "ص. $displayName / $displayLocation (تعديل)"
                    displayName.isNotBlank() -> "ص. $displayName (تعديل)"
                    displayLocation.isNotBlank() -> "$displayLocation (تعديل)"
                    else -> "تحديد الصيدلية والموقع (+)"
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TealContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clickable { onStartNewOrderClick() }
                        .testTag("topbar_pharmacy_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chipText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryTeal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        actions = {
            // Prominent "+" Button to start new order for a pharmacy as requested by user
            IconButton(
                onClick = onStartNewOrderClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryTeal, CircleShape)
                    .testTag("topbar_add_order_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "بدء طلبية باسم الصيدلية والموقع",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Registered Orders List Button
            IconButton(
                onClick = onNavigateToOrders,
                modifier = Modifier.testTag("topbar_orders_list_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "قائمة الطلبات المسجلة",
                    tint = PrimaryTeal
                )
            }

            // More Menu
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.testTag("topbar_more_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "خيارات إضافية"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("تعديل الصيدلية والموقع") },
                    onClick = {
                        showMenu = false
                        onStartNewOrderClick()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = PrimaryTeal)
                    }
                )
                DropdownMenuItem(
                    text = { Text("قائمة الطلبات المسجلة") },
                    onClick = {
                        showMenu = false
                        onNavigateToOrders()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = PrimaryTeal)
                    }
                )
                DropdownMenuItem(
                    text = { Text("حذف وتفريغ جميع المواد") },
                    onClick = {
                        showMenu = false
                        onClearAllMedications()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
