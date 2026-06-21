package com.podzemnayapochta.presentation.hud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.podzemnayapochta.ui.theme.PodzemnayaPochtaTheme

/**
 * Компактный HUD с прогрессом игрока (см. ROADMAP, PR 3):
 * набранные очки и число доставленных писем.
 */
@Composable
fun GameHud(
    score: Int,
    delivered: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = HUD_ALPHA),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Очки: $score",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Доставлено: $delivered/$total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private const val HUD_ALPHA = 0.85f

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun GameHudPreview() {
    PodzemnayaPochtaTheme {
        GameHud(score = 25, delivered = 2, total = 6)
    }
}
