package com.example.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.reminder.ui.theme.ReminderTheme
import com.example.reminder.PreferenceHelper

class MainActivity : ComponentActivity() {

    // 权限申请回调
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startReminderService()
        } else {
            Toast.makeText(
                this,
                "未授予通知权限，提醒功能可能无法正常使用",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ 动态申请通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startReminderService()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // 低于Android 13直接启动服务
            startReminderService()
        }

        setContent {
            ReminderTheme {
                MainScreen(context = this)
            }
        }
    }

    private fun startReminderService() {
        val intent = Intent(this, ReminderService::class.java)
        intent.action = "com.example.reminder.ACTION_START_TIMER"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
fun MainScreen(context: android.content.Context) {
    var workMinutes by remember { mutableStateOf(PreferenceHelper.getWorkMinutes(context)) }
    var restMinutes by remember { mutableStateOf(PreferenceHelper.getRestMinutes(context)) }
    var showSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("休息提醒设置", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("工作时长(分钟): ")
            TextField(
                value = workMinutes.toString(),
                onValueChange = { value ->
                    workMinutes = value.toIntOrNull() ?: 0
                },
                modifier = Modifier.width(80.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("休息时长(分钟): ")
            TextField(
                value = restMinutes.toString(),
                onValueChange = { value ->
                    restMinutes = value.toIntOrNull() ?: 0
                },
                modifier = Modifier.width(80.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = {
            PreferenceHelper.saveTimes(context, workMinutes, restMinutes)
            showSaved = true
            // 发送启动计时的Intent
            val intent = Intent(context, ReminderService::class.java)
            intent.action = "com.example.reminder.ACTION_START_TIMER"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }) {
            Text("保存设置")
        }
        if (showSaved) {
            Spacer(Modifier.height(16.dp))
            Text("设置已保存！", color = MaterialTheme.colorScheme.primary)
        }
    }
}