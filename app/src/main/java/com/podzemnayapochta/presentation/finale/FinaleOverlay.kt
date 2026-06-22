package com.podzemnayapochta.presentation.finale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.podzemnayapochta.domain.model.Ending
import com.podzemnayapochta.ui.theme.PodzemnayaPochtaTheme

/**
 * Оверлей финального выбора у старого лифта (см. docs/idea.md — 2 концовки).
 * Две кнопки ведут к разным концовкам; тап по фону закрывает без выбора.
 */
@Composable
fun FinaleOverlay(
    onChoose: (Ending) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xCC1A0F0C))
                .clickable(onClick = onDismiss)
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Старый лифт",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text =
                        "Тросы натянуты, рычаг под рукой. Поднять лифт к поверхности — " +
                            "или навсегда оставить путь наверх тайной?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )
                Button(
                    onClick = { onChoose(Ending.OPEN_PATH) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Открыть путь наверх")
                }
                Button(
                    onClick = { onChoose(Ending.KEEP_SECRET) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                ) {
                    Text("Оставить тайну")
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun FinaleOverlayPreview() {
    PodzemnayaPochtaTheme {
        FinaleOverlay(onChoose = {}, onDismiss = {})
    }
}
