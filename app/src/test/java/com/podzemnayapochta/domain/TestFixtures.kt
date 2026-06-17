package com.podzemnayapochta.domain

import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus
import com.podzemnayapochta.domain.model.Location

/** Фабрики тестовых данных для domain-тестов. */
object TestFixtures {
    fun letter(
        id: String = "letter-1",
        recipientNpcId: String = "npc-clockmaker",
        reward: Int = 10,
        status: LetterStatus = LetterStatus.RECEIVED,
    ) = Letter(
        id = id,
        title = "Письмо часовщику",
        body = "Тому, кто слышит тиканье под землёй.",
        recipientNpcId = recipientNpcId,
        hints = listOf("Он чинит время"),
        reward = reward,
        status = status,
    )

    fun location(
        id: String = "post-office",
        connected: List<String> = listOf("market", "clock-house"),
    ) = Location(
        id = id,
        title = "Почтовая станция",
        description = "Стены латунных труб.",
        backgroundAsset = "art/post-office/bg.png",
        connectedLocationIds = connected,
    )

    fun state(
        currentLocationId: String = "post-office",
        letters: List<Letter> = listOf(letter()),
        flags: Map<String, Boolean> = emptyMap(),
        score: Int = 0,
    ) = GameState(
        currentLocationId = currentLocationId,
        letters = letters.associateBy { it.id },
        flags = flags,
        score = score,
        unlockedLocationIds = setOf(currentLocationId),
    )
}
