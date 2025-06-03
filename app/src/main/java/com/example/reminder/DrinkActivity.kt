package com.example.reminder

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.app.KeyguardManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.example.reminder.ui.theme.ReminderTheme
import com.example.reminder.features.drink.domain.repository.DrinkSettingsRepository
import com.example.reminder.features.drink.data.repository.DrinkRecordRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DrinkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置窗口标志，确保能在锁屏上显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 全屏显示
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        
        // 在其他应用之上显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
        }
        
        setContent {
            ReminderTheme {
                DrinkScreen(
                    context = this,
                    onDrink = { amount ->
                        recordDrink(amount)
                        finish()
                    },
                    onSkip = {
                        // 跳过此次提醒，也要清除通知
                        clearNotifications()
                        finish()
                    },
                    onFinish = { 
                        // 倒计时结束，也要清除通知
                        clearNotifications()
                        finish() 
                    }
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 沉浸式全屏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
            }
        }
    }

    override fun onBackPressed() {
        // 禁止返回键，防止用户意外关闭
        // 用户必须通过界面按钮来关闭
    }

    private fun recordDrink(amount: Int) {
        // 保存喝水记录到Room数据库
        val repository = DrinkRecordRepository(this)
        lifecycleScope.launch {
            repository.addDrinkRecord(amount)
        }
        
        // 清除所有相关通知
        clearNotifications()
        
        // 通知服务记录了喝水
        val intent = Intent(this, ReminderService::class.java)
        intent.action = ReminderService.ACTION_RECORD_DRINK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    private fun clearNotifications() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 清除全屏通知 (ID: 3)
        notificationManager.cancel(3)
        // 清除系统通知 (ID: 2)
        notificationManager.cancel(2)
    }
}

@Composable
fun DrinkScreen(
    context: Context,
    onDrink: (Int) -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    var countdown by remember { mutableStateOf(15) } // 15秒倒计时
    
    // 使用Room数据库
    val repository = remember { DrinkRecordRepository(context) }
    val settings = DrinkSettingsRepository.loadSettings(context)
    
    // 使用Flow监听今日喝水量变化
    val todayDrinkFlow = repository.getTodayTotalAmountFlow()
    val todayDrink by todayDrinkFlow.collectAsState(initial = 0)
    
    val dailyGoal = settings.dailyGoal
    val actualTodayDrink = todayDrink ?: 0
    val progress = (actualTodayDrink.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)

    // 倒计时逻辑
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        } else {
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 标题
        Text(
            "💧 该喝水了！", 
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            "保持水分充足，保护身体健康", 
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(Modifier.height(32.dp))
        
        // 今日进度卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "今日饮水进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "${actualTodayDrink}ml / ${dailyGoal}ml",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Spacer(Modifier.height(12.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (progress >= 1f) Color.Green else MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    if (progress >= 1f) "🎉 今日目标已完成！" else "还需 ${dailyGoal - actualTodayDrink}ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (progress >= 1f) Color.Green else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // 喝水量选择
        Text(
            "选择喝水量",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(Modifier.height(16.dp))
        
        // 喝水量按钮网格
        val amounts = listOf(50, 100, 200, 300)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                amounts.take(2).forEach { amount ->
                    Button(
                        onClick = { onDrink(amount) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${amount}ml")
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                amounts.drop(2).forEach { amount ->
                    Button(
                        onClick = { onDrink(amount) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${amount}ml")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // 倒计时和跳过按钮
        Text(
            "${countdown}秒后自动关闭",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("⏰ 跳过此次")
        }
    }
} 