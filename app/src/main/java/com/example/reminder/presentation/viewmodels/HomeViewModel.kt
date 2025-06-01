package com.example.reminder.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    fun startTimer() {
        _isTimerRunning.value = true
        // TODO: 实现计时器逻辑
    }

    fun stopTimer() {
        _isTimerRunning.value = false
        // TODO: 停止计时器
    }

    fun resetTimer() {
        _remainingTime.value = 0
    }
} 