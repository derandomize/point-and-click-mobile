package com.podzemnayapochta.presentation.location

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.podzemnayapochta.ui.theme.LanternHoney
import com.podzemnayapochta.ui.theme.LanternTeal
import com.podzemnayapochta.ui.theme.UndergroundShadow
import com.podzemnayapochta.ui.theme.UndergroundViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Экран локации — «открытка» point-and-click (см. docs/architecture.md, SceneRenderer).
 * Рендерит нарисованный фон локации и портреты NPC из ассетов; выходы и сюжетные
 * точки подсвечиваются полупрозрачными зонами поверх фона. Тап определяется через
 * [HitTester] и ведёт либо в диалог с NPC, либо в соседнюю локацию.
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

    if (obj.kind == SceneObjectKind.NPC && image != null) {
        drawPortrait(image, centerX = left + width / 2f, bottom = top + height, areaHeight = height)
    } else {
        val highlight = if (obj.kind == SceneObjectKind.EXIT) LanternTeal else LanternHoney
        drawRoundRect(
            color = highlight.copy(alpha = HIGHLIGHT_ALPHA),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius =
                androidx.compose.ui.geometry
                    .CornerRadius(16f, 16f),
        )
    }

    val layout = textMeasurer.measure(obj.label)
    drawText(
        textLayoutResult = layout,
        color = Color.White,
        topLeft = Offset(left + (width - layout.size.width) / 2f, top + height + 6f),
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

private const val HIGHLIGHT_ALPHA = 0.30f
