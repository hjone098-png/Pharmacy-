package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.TealContainer

/**
 * Bottom navigation bar with ONLY:
 * 1. طلب أدوية (Order / Medicine selection)
 * 2. ملخص الطلب (Order Summary)
 * 3. قائمة الطلبات المسجلة (Registered orders list)
 *
 * (Inventory and Pharmacy profile removed per user request)
 */
@Composable
fun PharmaBottomBar(
    currentScreen: Screen,
    cartItemCount: Int,
    registeredOrdersCount: Int = 0,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("pharma_bottom_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        // 1. طلب أدوية
        NavigationBarItem(
            selected = currentScreen == Screen.OrderCreation,
            onClick = { onNavigate(Screen.OrderCreation) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.OrderCreation) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                    contentDescription = "طلب أدوية"
                )
            },
            label = {
                Text(
                    text = "طلب أدوية",
                    fontSize = 12.sp,
                    fontWeight = if (currentScreen == Screen.OrderCreation) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryTeal,
                selectedTextColor = PrimaryTeal,
                indicatorColor = TealContainer
            ),
            modifier = Modifier.testTag("nav_order_creation")
        )

        // 2. ملخص الطلب
        NavigationBarItem(
            selected = currentScreen == Screen.OrderSummary,
            onClick = { onNavigate(Screen.OrderSummary) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = PrimaryTeal,
                                contentColor = Color.White
                            ) {
                                Text("$cartItemCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentScreen == Screen.OrderSummary) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                        contentDescription = "ملخص الطلب"
                    )
                }
            },
            label = {
                Text(
                    text = "ملخص الطلب",
                    fontSize = 12.sp,
                    fontWeight = if (currentScreen == Screen.OrderSummary) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryTeal,
                selectedTextColor = PrimaryTeal,
                indicatorColor = TealContainer
            ),
            modifier = Modifier.testTag("nav_order_summary")
        )

        // 3. قائمة الطلبات المسجلة
        NavigationBarItem(
            selected = currentScreen == Screen.OrderHistory,
            onClick = { onNavigate(Screen.OrderHistory) },
            icon = {
                BadgedBox(
                    badge = {
                        if (registeredOrdersCount > 0) {
                            Badge(
                                containerColor = PrimaryTeal,
                                contentColor = Color.White
                            ) {
                                Text("$registeredOrdersCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentScreen == Screen.OrderHistory) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                        contentDescription = "الطلبات المسجلة"
                    )
                }
            },
            label = {
                Text(
                    text = "الطلبات المسجلة",
                    fontSize = 12.sp,
                    fontWeight = if (currentScreen == Screen.OrderHistory) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryTeal,
                selectedTextColor = PrimaryTeal,
                indicatorColor = TealContainer
            ),
            modifier = Modifier.testTag("nav_order_history")
        )
    }
}
