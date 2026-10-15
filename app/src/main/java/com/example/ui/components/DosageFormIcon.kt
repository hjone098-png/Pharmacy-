package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryCyan

@Composable
fun DosageFormIcon(
    dosageForm: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val (icon: ImageVector, bgTint: Color, iconTint: Color) = when {
        dosageForm.contains("حقن") || dosageForm.contains("أمبول") ->
            Triple(Icons.Default.Vaccines, Color(0xFFFEE2E2), Color(0xFFDC2626))

        dosageForm.contains("شراب") || dosageForm.contains("سائل") ->
            Triple(Icons.Default.LocalDrink, Color(0xFFE0F2FE), SecondaryCyan)

        dosageForm.contains("بخاخ") || dosageForm.contains("استنشاق") ->
            Triple(Icons.Default.Air, Color(0xFFF3E8FF), Color(0xFF9333EA))

        dosageForm.contains("مرهم") || dosageForm.contains("كريم") || dosageForm.contains("جل") ->
            Triple(Icons.Default.Healing, Color(0xFFFEF3C7), Color(0xFFD97706))

        dosageForm.contains("محلول") || dosageForm.contains("مطهر") ->
            Triple(Icons.Default.Science, Color(0xFFE0E7FF), Color(0xFF4F46E5))

        dosageForm.contains("قطرات") ->
            Triple(Icons.Default.LocalDrink, Color(0xFFCCFBF1), PrimaryTeal)

        else -> // أقراص / كبسولات
            Triple(Icons.Default.Medication, Color(0xFFCCFBF1), PrimaryTeal)
    }

    Box(
        modifier = modifier
            .size(size)
            .background(bgTint, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = dosageForm,
            tint = iconTint,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
