package com.podzemnayapochta.presentation.location

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.podzemnayapochta.engine.HitTester
import com.podzemnayapochta.engine.Scene
import com.podzemnayapochta.engine.SceneObject
import com.podzemnayapochta.engine.SceneObjectKind
import com.podzemnayapochta.ui.theme.LanternAmber
import com.podzemnayapochta.ui.theme.LanternHoney
import com.podzemnayapochta.ui.theme.LanternTeal
import com.podzemnayapochta.ui.theme.ParchmentLight
import com.podzemnayapochta.ui.theme.UndergroundShadow
import com.podzemnayapochta.ui.theme.UndergroundViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Экран локации — «открытка» point-and-click (см. docs/architecture.md, SceneRenderer).
 * Рендерит нарисованный фон локации и портреты NPC из ассетов; выходы и сюжетные
 * точки помечаются компактными «табличками» поверх фона (без больших цветных
 * плашек). Тап определяется через [HitTester] и ведёт либо в диалог с NPC,
 * либо в соседнюю локацию.
 */
@Composable
fun LocationScreen(
    scene: Scene,
    onNpcTapped: (String) -> Unit = {},
    onExitTapped: (String) -> Unit = {},
    onHotspotTapped: (String) -> Unit = {},
    onOpenBag: () -> Unit = {},
) {
    val hitTester = remember { HitTester() }
    val textMeasurer = rememberTextMeasurer()
    val images = rememberSceneImages(scene)

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
                                SceneObjectKind.HOTSPOT -> obj.payload?.let(onHotspotTapped)
                            }
                        }
                    },
        ) {
            drawBackground(images[scene.backgroundAsset])
            scene.objects.forEach { obj ->
                drawSceneObject(obj, obj.imageAsset?.let { images[it] }, textMeasurer)
            }
        }

        Surface(
            color = UndergroundShadow.copy(alpha = PLATE_ALPHA),
            shape = RoundedCornerShape(12.dp),
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
        ) {
            Text(
                text = scene.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

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

/** Асинхронно декодирует фон и портреты сцены из ассетов в [ImageBitmap]. */
@Composable
private fun rememberSceneImages(scene: Scene): Map<String, ImageBitmap> {
    val context = LocalContext.current
    val paths =
        remember(scene) {
            (listOf(scene.backgroundAsset) + scene.objects.mapNotNull { it.imageAsset }).distinct()
        }
    val images by produceState(initialValue = emptyMap<String, ImageBitmap>(), paths, context) {
        value =
            withContext(Dispatchers.IO) {
                paths.mapNotNull { path -> decodeAsset(context, path)?.let { path to it } }.toMap()
            }
    }
    return images
}

private fun decodeAsset(
    context: Context,
    path: String,
): ImageBitmap? =
    runCatching {
        context.assets
            .open(path)
            .use { BitmapFactory.decodeStream(it) }
            ?.asImageBitmap()
    }.getOrNull()

private fun DrawScope.drawBackground(image: ImageBitmap?) {
    if (image != null) {
        drawImage(
            image = image,
            srcSize = IntSize(image.width, image.height),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
        )
    } else {
        drawRect(
            brush =
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(UndergroundViolet, UndergroundShadow),
                ),
            size = size,
        )
    }
}

private fun DrawScope.drawSceneObject(
    obj: SceneObject,
    image: ImageBitmap?,
    textMeasurer: TextMeasurer,
) {
    val area = obj.area
    val left = area.left * size.width
    val top = area.top * size.height
    val width = (area.right - area.left) * size.width
    val height = (area.bottom - area.top) * size.height
    val centerX = left + width / 2f

    when (obj.kind) {
        SceneObjectKind.NPC -> {
            if (image != null) {
                drawPortrait(image, centerX = centerX, bottom = top + height, areaHeight = height)
            } else {
                drawRoundRect(
                    color = LanternAmber,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(16f, 16f),
                )
            }
            drawLabelChip(textMeasurer, obj.label, centerX, top + height + CHIP_GAP, ParchmentLight)
        }

        SceneObjectKind.EXIT ->
            drawLabelChip(textMeasurer, "↦ ${obj.label}", centerX, top + height / 2f, LanternTeal)

        SceneObjectKind.HOTSPOT -> {
            drawCircle(
                color = LanternHoney.copy(alpha = GLOW_ALPHA),
                radius = minOf(width, height) / 2f,
                center = Offset(centerX, top + height / 2f),
            )
            drawLabelChip(textMeasurer, obj.label, centerX, top + height + CHIP_GAP, LanternHoney)
        }
    }
}

/** Полупрозрачная «табличка» с подписью, центрированная в точке (cx, cy). */
private fun DrawScope.drawLabelChip(
    textMeasurer: TextMeasurer,
    text: String,
    cx: Float,
    cy: Float,
    textColor: Color,
) {
    val layout = textMeasurer.measure(text)
    val chipWidth = layout.size.width + CHIP_PAD_H * 2
    val chipHeight = layout.size.height + CHIP_PAD_V * 2
    drawRoundRect(
        color = UndergroundShadow.copy(alpha = PLATE_ALPHA),
        topLeft = Offset(cx - chipWidth / 2f, cy - chipHeight / 2f),
        size = Size(chipWidth, chipHeight),
        cornerRadius = CornerRadius(chipHeight / 2f, chipHeight / 2f),
    )
    drawText(
        textLayoutResult = layout,
        color = textColor,
        topLeft = Offset(cx - layout.size.width / 2f, cy - layout.size.height / 2f),
    )
}

private fun DrawScope.drawPortrait(
    image: ImageBitmap,
    centerX: Float,
    bottom: Float,
    areaHeight: Float,
) {
    val scale = areaHeight / image.height
    val drawWidth = image.width * scale
    drawImage(
        image = image,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset((centerX - drawWidth / 2f).toInt(), (bottom - areaHeight).toInt()),
        dstSize = IntSize(drawWidth.toInt(), areaHeight.toInt()),
    )
}

private const val PLATE_ALPHA = 0.78f
private const val GLOW_ALPHA = 0.25f
private const val CHIP_PAD_H = 18f
private const val CHIP_PAD_V = 10f
private const val CHIP_GAP = 18f
