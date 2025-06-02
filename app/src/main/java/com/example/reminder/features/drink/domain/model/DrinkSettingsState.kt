package com.example.reminder.features.drink.domain.model

data class DrinkSettingsState(
    val settings: DrinkReminderSettings = DrinkReminderSettings(),
    val isSaved: Boolean = false
) 