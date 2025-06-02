package com.example.reminder.features.drink.presentation.settings

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reminder.ReminderService
import com.example.reminder.DrinkActivity

@Composable
fun DrinkSettingsScreen(viewModel: DrinkSettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    // 首次进入加载
    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    val settings = state.settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("喝水提醒设置", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // 每日目标
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("每日目标")
            Spacer(Modifier.width(16.dp))
            TextField(
                value = settings.dailyGoal.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let {
                        viewModel.updateField { it.copy(dailyGoal = value.toIntOrNull() ?: 0) }
                    }
                },
                modifier = Modifier.width(100.dp),
                suffix = { Text("ml") }
            )
        }

        Spacer(Modifier.height(16.dp))

        // 提醒间隔
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("提醒间隔")
            Spacer(Modifier.width(16.dp))
            TextField(
                value = settings.intervalMinutes.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let {
                        viewModel.updateField { it.copy(intervalMinutes = value.toIntOrNull() ?: 0) }
                    }
                },
                modifier = Modifier.width(100.dp),
                suffix = { Text("分钟") }
            )
        }

        Spacer(Modifier.height(16.dp))

        // 重复（星期选择）
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("重复")
            Spacer(Modifier.width(16.dp))
            WeekdaySelector(
                selectedDays = settings.repeatDays,
                onDayToggle = { day, selected ->
                    val newSet = if (selected) settings.repeatDays + day else settings.repeatDays - day
                    viewModel.updateField { it.copy(repeatDays = newSet) }
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // 时间范围
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("时间范围")
            Spacer(Modifier.width(16.dp))
            TextField(
                value = "%02d:%02d".format(settings.startHour, settings.startMinute),
                onValueChange = { /* 可用 TimePickerDialog 替换 */ },
                modifier = Modifier.width(80.dp),
                readOnly = true
            )
            Text(" 至 ")
            TextField(
                value = "%02d:%02d".format(settings.endHour, settings.endMinute),
                onValueChange = { /* 可用 TimePickerDialog 替换 */ },
                modifier = Modifier.width(80.dp),
                readOnly = true
            )
        }

        Spacer(Modifier.height(32.dp))
        
        // 保存设置按钮
        Button(onClick = { 
            viewModel.save(context)
            // 保存设置后启动喝水提醒服务
            startDrinkReminderService(context)
        }) {
            Text("保存设置")
        }
        
        if (state.isSaved) {
            Spacer(Modifier.height(16.dp))
            Text("设置已保存，喝水提醒已启动！", color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(Modifier.height(16.dp))
        
        // 立即测试按钮
        OutlinedButton(
            onClick = { 
                openDrinkActivity(context)
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("🧪 立即测试喝水界面")
        }
    }
}

// 启动喝水提醒服务
private fun startDrinkReminderService(context: android.content.Context) {
    val intent = Intent(context, ReminderService::class.java)
    intent.action = ReminderService.ACTION_START_DRINK_REMINDER
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

// 打开喝水界面（用于测试）
private fun openDrinkActivity(context: android.content.Context) {
    val intent = Intent(context, DrinkActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

// 星期选择器
@Composable
fun WeekdaySelector(selectedDays: Set<Int>, onDayToggle: (Int, Boolean) -> Unit) {
    val days = listOf("日", "一", "二", "三", "四", "五", "六")
    Row {
        days.forEachIndexed { idx, label ->
            val dayNum = idx + 1
            val selected = selectedDays.contains(dayNum)
            Button(
                onClick = { onDayToggle(dayNum, !selected) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Text(label)
            }
            Spacer(Modifier.width(4.dp))
        }
    }
} 