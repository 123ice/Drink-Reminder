package com.example.reminder.features.drink.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reminder.features.drink.domain.repository.DrinkSettingsRepository
import com.example.reminder.features.drink.domain.model.DrinkSettingsState
import com.example.reminder.features.drink.domain.model.DrinkReminderSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DrinkSettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DrinkSettingsState())
    val uiState: StateFlow<DrinkSettingsState> = _uiState

    fun load(context: Context) {
        val settings = DrinkSettingsRepository.loadSettings(context)
        _uiState.value = _uiState.value.copy(settings = settings)
    }

    fun updateSettings(newSettings: DrinkReminderSettings) {
        _uiState.value = _uiState.value.copy(settings = newSettings, isSaved = false)
    }

    fun updateField(update: (DrinkReminderSettings) -> DrinkReminderSettings) {
        val newSettings = update(_uiState.value.settings)
        updateSettings(newSettings)
    }

    fun save(context: Context) {
        viewModelScope.launch {
            DrinkSettingsRepository.saveSettings(context, _uiState.value.settings)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun clearSavedFlag() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
} 