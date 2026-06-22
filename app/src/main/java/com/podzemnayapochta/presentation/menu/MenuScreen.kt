package com.podzemnayapochta.presentation.menu

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
import com.podzemnayapochta.ui.theme.PodzemnayaPochtaTheme

/**
 * Главное меню (см. docs/idea.md — главное меню и экран карты).
 * «Продолжить» доступно только при наличии сохранения (см. ROADMAP, PR 6).
 */
@Composable
fun MenuScreen(
    hasSave: Boolean,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Подземная Почта",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Письма ждут своего почтальона…",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onContinue,
            enabled = hasSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Продолжить")
        }
        Button(
            onClick = onNewGame,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
        ) {
            Text("Новая игра")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    PodzemnayaPochtaTheme {
        MenuScreen(hasSave = true, onContinue = {}, onNewGame = {})
    }
}
