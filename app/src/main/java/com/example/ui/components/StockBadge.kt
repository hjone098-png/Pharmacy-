package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StockDangerContainer
import com.example.ui.theme.StockDangerRed
import com.example.ui.theme.StockOkContainer
import com.example.ui.theme.StockOkGreen
import com.example.ui.theme.StockWarningAmber
import com.example.ui.theme.StockWarningContainer

@Composable
fun StockBadge(
    currentStock: Int,
    minStock: Int,
    unit: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text, icon) = when {
        currentStock <= 0 -> Quadruple(
            StockDangerContainer,
            StockDangerRed,
            "نفد من المخزون",
            Icons.Default.ErrorOutline
        )
        currentStock <= minStock -> Quadruple(
            StockWarningContainer,
            StockWarningAmber,
            "مخزون منخفض ($currentStock $unit)",
            Icons.Default.Warning
        )
        else -> Quadruple(
            StockOkContainer,
            StockOkGreen,
            "متوفر ($currentStock $unit)",
            Icons.Default.CheckCircle
        )
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = " $text",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
