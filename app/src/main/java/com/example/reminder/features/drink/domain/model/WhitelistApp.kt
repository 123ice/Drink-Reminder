package com.example.reminder.features.drink.domain.model

data class WhitelistApp(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true
)

data class WhitelistSettings(
    val apps: List<WhitelistApp> = emptyList(),
    val isWhitelistEnabled: Boolean = true
) 