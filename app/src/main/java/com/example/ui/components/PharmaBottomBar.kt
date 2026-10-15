package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalPharmacy
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

@Composable
fun PharmaBottomBar(
    currentScreen: Screen,
    cartItemCount: Int,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("pharma_bottom_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        // 1. Order / Catalog
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
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.OrderCreation) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryTeal,
                selectedTextColor = PrimaryTeal,
                indicatorColor = TealContainer
            ),
            modifier = Modifier.testTag("nav_order_creation")
        )

        // 2. Order Summary
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
                        contentDescription = "ملخص الطلبية"
                    )
                }
            },
            label = {
                Text(
                    text = "ملخص الطلب",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.OrderSummary) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryTeal,
                selectedTextColor = PrimaryTeal,
                indicatorColor = TealContainer
            ),
            modifier = Modifier.testTag("nav_order_summary")
        )

        // 3. Inventory Management
        NavigationBarItem(
            selected = currentScreen == Screen.Inventory,
            onClick = { onNavigate(Screen.Inventory) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Inventory) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                    contentDescription = "المخزون"
                )
            },
            label = {
                Text(
                    text = "المخزون",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.Inventory) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryTeal,
                selectedTextColor = PrimaryTeal,
                indicatorColor = TealContainer
            ),
            modifier = Modifier.testTag("nav_inventory")
        )

        // 4. Pharmacy & Location Profile
        NavigationBarItem(
            selected = currentScreen == Screen.PharmacyProfile,
            onClick = { onNavigate(Screen.PharmacyProfile) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.PharmacyProfile) Icons.Filled.LocalPharmacy else Icons.Outlined.LocalPharmacy,
                    contentDescription = "الصيدلية"
                )
            },
            label = {
                Text(
                    text = "الصيدلية",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.PharmacyProfile) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryTeal,
                selectedTextColor = PrimaryTeal,
                indicatorColor = TealContainer
            ),
            modifier = Modifier.testTag("nav_profile")
        )

        // 5. Order History
        NavigationBarItem(
            selected = currentScreen == Screen.OrderHistory,
            onClick = { onNavigate(Screen.OrderHistory) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.OrderHistory) Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = "الأرشيف"
                )
            },
            label = {
                Text(
                    text = "الأرشيف",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.OrderHistory) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryTeal,
                selectedTextColor = PrimaryTeal,
                indicatorColor = TealContainer
            ),
            modifier = Modifier.testTag("nav_history")
        )
    }
}

