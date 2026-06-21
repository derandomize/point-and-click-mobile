package com.podzemnayapochta.presentation.location

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.podzemnayapochta.engine.HitTester
import com.podzemnayapochta.engine.Scene
import com.podzemnayapochta.engine.SceneObject
import com.podzemnayapochta.engine.SceneObjectKind
import com.podzemnayapochta.ui.theme.LanternAmber
import com.podzemnayapochta.ui.theme.LanternHoney
import com.podzemnayapochta.ui.theme.LanternTeal
import com.podzemnayapochta.ui.theme.UndergroundShadow
import com.podzemnayapochta.ui.theme.UndergroundViolet

/**
 * Экран локации — «открытка» point-and-click (см. docs/architecture.md, SceneRenderer).
 * Плейсхолдер-рендер на Compose Canvas: градиентный фон, NPC и выходы как
 * кликабельные объекты. Тап определяется через [HitTester]; результат —
 * либо вход в диалог с NPC, либо переход в соседнюю локацию.
 *
 * Реальные фоны-«открытки» появятся, когда будет готова графика (docs/style.md).
 */
@Composable
fun LocationScreen(
    scene: Scene,
    onNpcTapped: (String) -> Unit = {},
    onExitTapped: (String) -> Unit = {},
    onOpenBag: () -> Unit = {},
) {
    val hitTester = remember { HitTester() }
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(scene) {
                        detectTapGestures { tap ->
                            val nx = tap.x / size.width
                            val ny = tap.y / size.height
                            val hit = hitTester.hitTest(scene.hitAreas, nx, ny) ?: return@detectTapGestures
                            val obj = scene.objects.firstOrNull { it.area.id == hit.id } ?: return@detectTapGestures
                            when (obj.kind) {
                                SceneObjectKind.NPC -> obj.payload?.let(onNpcTapped)
                                SceneObjectKind.EXIT -> obj.payload?.let(onExitTapped)
                                SceneObjectKind.HOTSPOT -> Unit
                            }
                        }
                    },
        ) {
            drawBackground()
            scene.objects.forEach { obj -> drawSceneObject(obj, textMeasurer) }
        }

        Text(
            text = scene.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
        )

        Button(
            onClick = onOpenBag,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
        ) {
            Text("Сумка")
        }
    }
}

private fun DrawScope.drawBackground() {
    drawRect(
        brush =
            androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(UndergroundViolet, UndergroundShadow),
            ),
        size = size,
    )
}

private fun DrawScope.drawSceneObject(
    obj: SceneObject,
    textMeasurer: TextMeasurer,
) {
    val area = obj.area
    val left = area.left * size.width
    val top = area.top * size.height
    val width = (area.right - area.left) * size.width
    val height = (area.bottom - area.top) * size.height

    val fill =
        when (obj.kind) {
            SceneObjectKind.NPC -> LanternAmber
            SceneObjectKind.EXIT -> LanternTeal
            SceneObjectKind.HOTSPOT -> LanternHoney
        }

    drawRoundRect(
        color = fill,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius =
            androidx.compose.ui.geometry
                .CornerRadius(16f, 16f),
    )

    val layout = textMeasurer.measure(obj.label)
    drawText(
        textLayoutResult = layout,
        color = Color.White,
        topLeft = Offset(left + (width - layout.size.width) / 2f, top + height + 6f),
    )
}
