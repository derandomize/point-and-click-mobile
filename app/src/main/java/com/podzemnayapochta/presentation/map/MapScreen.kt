package com.podzemnayapochta.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.podzemnayapochta.ui.theme.ParchmentLight
import com.podzemnayapochta.ui.theme.PodzemnayaPochtaTheme
import com.podzemnayapochta.ui.theme.UndergroundShadow
import com.podzemnayapochta.ui.theme.UndergroundViolet

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
    currentLocationId: String? = null,
    onLocationSelected: (String) -> Unit = {},
) {
    val hitTester = remember { HitTester() }
    val textMeasurer = rememberTextMeasurer()
    var selectedId by remember(currentLocationId) { mutableStateOf(currentLocationId) }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(locations) {
                        detectTapGestures { tap ->
                            val nx = tap.x / size.width
                            val ny = tap.y / size.height
                            val id = resolveMapTap(locations, hitTester, nx, ny) ?: return@detectTapGestures
                            selectedId = id
                            onLocationSelected(id)
                        }
                    },
        ) {
            drawMapBackground()
            drawConnections(locations)
            locations.forEach { loc ->
                drawLocationNode(
                    location = loc,
                    selected = loc.id == selectedId,
                    textMeasurer = textMeasurer,
                )
            }
        }

        Surface(
            color = UndergroundShadow.copy(alpha = LABEL_PLATE_ALPHA),
            shape = RoundedCornerShape(12.dp),
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
        ) {
            Text(
                text = "Карта города — выберите локацию",
                style = MaterialTheme.typography.titleSmall,
                color = ParchmentLight,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private const val LABEL_PLATE_ALPHA = 0.78f

private fun DrawScope.drawMapBackground() {
    drawRect(
        brush =
            androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(UndergroundViolet, UndergroundShadow),
            ),
        size = size,
    )
}

private const val LOCKED_ALPHA = 0.3f

/**
 * Разрешает тап по карте в id локации, но только если она открыта
 * ([MapLocation.unlocked]). Тап по запертой локации возвращает null.
 */
internal fun resolveMapTap(
    locations: List<MapLocation>,
    hitTester: HitTester,
    nx: Float,
    ny: Float,
): String? {
    val hit = hitTester.hitTest(locations.map { it.toHitArea() }, nx, ny) ?: return null
    return locations.firstOrNull { it.id == hit.id && it.unlocked }?.id
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
    val radius = if (selected) 52f else 40f

    val accent =
        (if (selected) LanternHoney else LanternAmber)
            .let { if (location.unlocked) it else it.copy(alpha = LOCKED_ALPHA) }
    val core = if (location.unlocked) LanternTeal else LanternTeal.copy(alpha = LOCKED_ALPHA)

    if (selected) {
        drawCircle(color = LanternHoney.copy(alpha = NODE_GLOW_ALPHA), radius = radius + 18f, center = center)
    }
    drawCircle(color = UndergroundShadow, radius = radius + 6f, center = center)
    drawCircle(color = accent, radius = radius, center = center)
    drawCircle(color = core, radius = radius / 3f, center = center)

    val layout = textMeasurer.measure(location.title)
    val labelTop = center.y + radius + 12f
    val chipWidth = layout.size.width + NODE_CHIP_PAD * 2
    val chipHeight = layout.size.height + NODE_CHIP_PAD
    drawRoundRect(
        color = UndergroundShadow.copy(alpha = LABEL_PLATE_ALPHA),
        topLeft = Offset(center.x - chipWidth / 2f, labelTop - NODE_CHIP_PAD / 2f),
        size = Size(chipWidth, chipHeight),
        cornerRadius = CornerRadius(chipHeight / 2f, chipHeight / 2f),
    )
    drawText(
        textLayoutResult = layout,
        color = if (location.unlocked) ParchmentLight else ParchmentLight.copy(alpha = LOCKED_ALPHA),
        topLeft = Offset(center.x - layout.size.width / 2f, labelTop),
    )
}

private const val NODE_GLOW_ALPHA = 0.25f
private const val NODE_CHIP_PAD = 14f

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    PodzemnayaPochtaTheme {
        MapScreen()
    }
}
