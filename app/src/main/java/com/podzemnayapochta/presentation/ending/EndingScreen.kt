package com.podzemnayapochta.presentation.ending

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.podzemnayapochta.domain.model.Ending
import com.podzemnayapochta.ui.theme.PodzemnayaPochtaTheme

/**
 * Экран концовки игры (см. docs/idea.md — 2 концовки: открыть лифт / оставить тайну).
 */
@Composable
fun EndingScreen(
    ending: Ending,
    onBackToMenu: () -> Unit,
) {
    val (title, body) = endingText(ending)
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onBackToMenu,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("В меню")
        }
    }
}

private fun endingText(ending: Ending): Pair<String, String> =
    when (ending) {
        Ending.OPEN_PATH ->
            "Путь наверх открыт" to
                "Лифт со скрежетом пополз вверх. Впервые за столько лет на лица подземного " +
                "города упал настоящий солнечный свет. Что бы ни ждало наверху — теперь у людей есть выбор."
        Ending.KEEP_SECRET ->
            "Тайна сохранена" to
                "Ты отвёл руку от рычага. Пусть лифт молчит и дальше: под землёй спокойно и " +
                "безопасно. Письма всё так же будут находить своих адресатов, а тайна останется тайной."
    }

@Preview(showBackground = true)
@Composable
private fun EndingScreenPreview() {
    PodzemnayaPochtaTheme {
        EndingScreen(ending = Ending.OPEN_PATH, onBackToMenu = {})
    }
}
