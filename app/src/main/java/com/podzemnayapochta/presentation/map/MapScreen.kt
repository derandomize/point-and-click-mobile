package com.podzemnayapochta.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.podzemnayapochta.engine.HitTester
import com.podzemnayapochta.ui.theme.LanternAmber
import com.podzemnayapochta.ui.theme.LanternHoney
import com.podzemnayapochta.ui.theme.LanternTeal
import com.podzemnayapochta.ui.theme.ParchmentDim
import com.podzemnayapochta.ui.theme.PodzemnayaPochtaTheme
import com.podzemnayapochta.ui.theme.UndergroundShadow

/**
 * Экран карты подземного города. Плейсхолдер-рендер на Compose Canvas:
 * локации — светящиеся узлы, связи — линии. Тап по узлу определяется
 * через [HitTester] (см. docs/architecture.md).
 *
 * Реальные фоны-«открытки» появятся, когда будет готова графика
 * (промпты в docs/style.md).
 */
@Composable
fun MapScreen(
    locations: List<MapLocation> = MapLocation.placeholderCity(),
    onLocationSelected: (String) -> Unit = {},
) {
    val hitTester = remember { HitTester() }
    val textMeasurer = rememberTextMeasurer()
    var selectedId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(locations) {
                        detectTapGestures { tap ->
                            val nx = tap.x / size.width
                            val ny = tap.y / size.height
                            val hit = hitTester.hitTest(locations.map { it.toHitArea() }, nx, ny)
                            if (hit != null) {
                                selectedId = hit.id
                                onLocationSelected(hit.id)
                            }
                        }
                    },
        ) {
            drawConnections(locations)
            locations.forEach { loc ->
                drawLocationNode(
                    location = loc,
                    selected = loc.id == selectedId,
                    textMeasurer = textMeasurer,
                )
            }
        }

        Text(
            text = "Карта города — выберите локацию",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
        )
    }
}

private fun DrawScope.drawConnections(locations: List<MapLocation>) {
    val byId = locations.associateBy { it.id }
    locations.forEach { loc ->
        loc.connectedIds.forEach { targetId ->
            val target = byId[targetId] ?: return@forEach
            drawLine(
                color = ParchmentDim,
                start = Offset(loc.x * size.width, loc.y * size.height),
                end = Offset(target.x * size.width, target.y * size.height),
                strokeWidth = 4f,
            )
        }
    }
}

private fun DrawScope.drawLocationNode(
    location: MapLocation,
    selected: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val center = Offset(location.x * size.width, location.y * size.height)
    val radius = if (selected) 42f else 32f

    drawCircle(color = UndergroundShadow, radius = radius + 6f, center = center)
    drawCircle(
        color = if (selected) LanternHoney else LanternAmber,
        radius = radius,
        center = center,
    )
    drawCircle(color = LanternTeal, radius = radius / 3f, center = center)

    val layout = textMeasurer.measure(location.title)
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(center.x - layout.size.width / 2f, center.y + radius + 8f),
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    PodzemnayaPochtaTheme {
        MapScreen()
    }
}
