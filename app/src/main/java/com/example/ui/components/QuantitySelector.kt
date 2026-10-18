package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
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

/**
 * High performance quantity selector providing:
 * 1) The exact 5 requested unit options: قطعة واحدة (1)، عشر قطع (10)، 24 (كارتون)، 50، 100
 * 2) Plus (+) and Minus (-) stepper buttons with smooth, responsive feedback.
 */
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
        // Quick Unit Options: قطعة واحدة، عشر قطع، 24، 50، 100
        Row(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val unitOptions = listOf(
                1 to "قطعة واحدة (1)",
                10 to "عشر قطع (10)",
                24 to "24 (كارتون واحد)",
                50 to "50",
                100 to "100"
            )
            unitOptions.forEach { (qty, label) ->
                val isSelected = quantity == qty
                Surface(
                    onClick = { onQuantityChange(qty) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("${tagPrefix}_unit_$qty")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp),
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

        // Stepper Row (- [count] +)
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
            RepeatingIconButton(
                onAction = { onQuantityChange((quantity - 1).coerceAtLeast(0)) },
                enabled = quantity > 0,
                initialDelayMillis = 280L,
                repeatIntervalMillis = 60L,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (quantity > 0) MaterialTheme.colorScheme.surface else Color.Transparent
                    )
                    .testTag("${tagPrefix}_minus_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "إنقاص 1",
                    tint = if (quantity > 0) PrimaryTeal else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Quantity Display
            Box(
                modifier = Modifier
                    .width(48.dp)
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
            RepeatingIconButton(
                onAction = { onQuantityChange(quantity + 1) },
                initialDelayMillis = 280L,
                repeatIntervalMillis = 60L,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PrimaryTeal)
                    .testTag("${tagPrefix}_plus_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "زيادة 1",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
