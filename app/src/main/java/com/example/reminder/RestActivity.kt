package com.example.reminder

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reminder.ui.theme.ReminderTheme
import kotlinx.coroutines.delay

class RestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restMinutes = PreferenceHelper.getRestMinutes(this)
        val totalSeconds = restMinutes * 60
        setContent {
            ReminderTheme {
                RestScreen(
                    totalSeconds = totalSeconds,
                    tip = "休息一下，保护眼睛！",
                    onPostpone = {
                        val intent = Intent(this, ReminderService::class.java)
                        intent.action = "com.example.reminder.ACTION_RESTART_WORK"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                        finish()
                    },
                    onFinish = { finish() }
                )
            }
        }
    }
}

@Composable
fun RestScreen(
    totalSeconds: Int,
    tip: String,
    onPostpone: () -> Unit,
    onFinish: () -> Unit
) {
    var remaining by remember { mutableStateOf(totalSeconds) }

    // 倒计时逻辑
    LaunchedEffect(remaining) {
        if (remaining > 0) {
            delay(1000)
            remaining--
        } else {
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("改变焦点", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text(tip, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))
        LinearProgressIndicator(
            progress = (remaining / totalSeconds.toFloat()).coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth(0.5f)
        )
        Spacer(Modifier.height(8.dp))
        Text("还有 ${remaining / 60} 分钟 ${remaining % 60} 秒")
        Spacer(Modifier.height(32.dp))
        Button(onClick = onPostpone) {
            Text("延后此次休息")
        }
    }
}

fun finishRestActivity(context: Context) {
    val intent = Intent("com.example.reminder.ACTION_RESTART_WORK")
    context.sendBroadcast(intent)
} 