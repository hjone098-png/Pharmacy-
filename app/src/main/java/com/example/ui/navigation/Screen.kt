package com.example.ui.navigation

sealed class Screen(val route: String, val titleAr: String) {
    object OrderCreation : Screen("order_creation", "طلب أدوية")
    object OrderSummary : Screen("order_summary", "ملخص الطلبية")
    object Inventory : Screen("inventory", "إدارة المخزون")
    object PharmacyProfile : Screen("pharmacy_profile", "بيانات الصيدلية")
    object OrderHistory : Screen("order_history", "سجل الطلبيات")
}
