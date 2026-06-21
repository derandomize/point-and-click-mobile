package com.podzemnayapochta.presentation.dialogue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.podzemnayapochta.domain.model.DialogueChoice
import com.podzemnayapochta.domain.model.DialogueNode
import com.podzemnayapochta.presentation.game.DialogueUiState
import com.podzemnayapochta.ui.theme.PodzemnayaPochtaTheme

/**
 * Оверлей диалога поверх экрана локации (см. docs/architecture.md, DialogueEngine).
 * Показывает имя говорящего, реплику и доступные варианты ответа.
 * Если вариантов нет — тап по фону завершает диалог.
 */
@Composable
fun DialogueOverlay(
    dialogue: DialogueUiState,
    onChoice: (DialogueChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    val terminal = dialogue.availableChoices.isEmpty()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xCC1A0F0C))
                .clickable(enabled = terminal, onClick = onDismiss)
                .padding(24.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                dialogue.speakerName?.let { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = dialogue.node.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )

                if (terminal) {
                    Text(
                        text = "Нажмите, чтобы закрыть",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = FontStyle.Italic,
                    )
                } else {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    dialogue.availableChoices.forEach { choice ->
                        Text(
                            text = "• ${choice.text}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onChoice(choice) }
                                    .padding(vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun DialogueOverlayPreview() {
    PodzemnayaPochtaTheme {
        DialogueOverlay(
            dialogue =
                DialogueUiState(
                    npcId = "npc-pm",
                    node =
                        DialogueNode(
                            id = "n1",
                            speakerNpcId = "npc-pm",
                            text = "Новенький? Вот первое письмо.",
                        ),
                    speakerName = "Начальник почты",
                    availableChoices =
                        listOf(
                            DialogueChoice("Беру письмо.", targetNodeId = "n2"),
                            DialogueChoice("Позже.", targetNodeId = null),
                        ),
                ),
            onChoice = {},
            onDismiss = {},
        )
    }
}
