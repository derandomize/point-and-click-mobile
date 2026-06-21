package com.podzemnayapochta.presentation.letters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus
import com.podzemnayapochta.ui.theme.PodzemnayaPochtaTheme

/**
 * Оверлей «Сумка почтальона» поверх экрана локации (см. ROADMAP, PR 1).
 * Показывает письма игрока: заголовок, текст, подсказки и статус.
 * Адресат намеренно не раскрывается — игрок определяет его по подсказкам.
 */
@Composable
fun LetterBagOverlay(
    letters: List<Letter>,
    onClose: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xCC1A0F0C))
                .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Сумка почтальона",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            TextButton(onClick = onClose) {
                Text("Закрыть")
            }
        }

        if (letters.isEmpty()) {
            Text(
                text = "Сумка пуста — получите письмо на почтовой станции.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(letters, key = { it.id }) { letter ->
                    LetterCard(letter)
                }
            }
        }
    }
}

@Composable
private fun LetterCard(letter: Letter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = letter.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(STATUS_TITLE_FRACTION),
                )
                Text(
                    text = letter.status.bagLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = letter.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (letter.hints.isNotEmpty()) {
                Text(
                    text = "Подсказки:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp),
                )
                letter.hints.forEach { hint ->
                    Text(
                        text = "• $hint",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/** Человекочитаемая метка статуса письма для UI. */
fun LetterStatus.bagLabel(): String =
    when (this) {
        LetterStatus.LOCKED -> "Не получено"
        LetterStatus.RECEIVED -> "Получено"
        LetterStatus.IN_TRANSIT -> "В пути"
        LetterStatus.DELIVERED -> "Доставлено"
    }

private const val STATUS_TITLE_FRACTION = 0.7f

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun LetterBagOverlayPreview() {
    PodzemnayaPochtaTheme {
        LetterBagOverlay(
            letters =
                listOf(
                    Letter(
                        id = "letter-clockmaker",
                        title = "Письмо тому, кто слышит время",
                        body = "Моя кукушка снова замолчала. Только ты вернёшь ей голос.",
                        recipientNpcId = "npc-clockmaker",
                        hints = listOf("Адресат чинит часы", "Живёт рядом со старым лифтом"),
                        status = LetterStatus.RECEIVED,
                    ),
                ),
            onClose = {},
        )
    }
}
