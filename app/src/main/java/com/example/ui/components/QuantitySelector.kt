package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.TealContainer

@Composable
fun QuantitySelector(
    quantity: Int,
    unit: String,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = "item"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .background(
                    if (quantity > 0) TealContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(24.dp)
                )
                .padding(4.dp)
        ) {
            // Minus Button
            IconButton(
                onClick = { onQuantityChange((quantity - 1).coerceAtLeast(0)) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (quantity > 0) MaterialTheme.colorScheme.surface else Color.Transparent
                    )
                    .testTag("${tagPrefix}_minus_btn"),
                enabled = quantity > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "إنقاص الكمية",
                    tint = if (quantity > 0) PrimaryTeal else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Quantity Display
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$quantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (quantity > 0) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("${tagPrefix}_qty_text")
                )
            }

            // Plus Button
            IconButton(
                onClick = { onQuantityChange(quantity + 1) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryTeal)
                    .testTag("${tagPrefix}_plus_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "زيادة الكمية",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Quick add chips for convenience
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            listOf(5, 10, 20).forEach { delta ->
                Surface(
                    onClick = { onQuantityChange(quantity + delta) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .height(24.dp)
                        .testTag("${tagPrefix}_quick_add_$delta")
                ) {
                    Text(
                        text = "+$delta",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryTeal,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
