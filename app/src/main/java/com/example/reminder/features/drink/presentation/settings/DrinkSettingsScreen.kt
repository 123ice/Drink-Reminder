package com.example.reminder.features.drink.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import com.example.reminder.ReminderService
import com.example.reminder.DrinkActivity
import com.example.reminder.features.drink.presentation.whitelist.WhitelistScreen
import com.example.reminder.utils.PermissionUtils

@Composable
fun DrinkSettingsScreen(viewModel: DrinkSettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var permissionMessage by remember { mutableStateOf("") }
    
    // 时间选择器状态
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    // 白名单界面状态
    var showWhitelistScreen by remember { mutableStateOf(false) }
    
    // 权限请求launchers - 在Compose中正确注册
    val fullScreenPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val hasPermission = PermissionUtils.hasFullScreenIntentPermission(context)
        if (hasPermission) {
            viewModel.updateField { it.copy(usePopupReminder = true) }
            permissionMessage = ""
        } else {
            permissionMessage = "需要开启全屏通知权限才能使用全屏弹窗提醒"
        }
    }
    
    val systemAlertPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val hasPermission = PermissionUtils.hasSystemAlertPermission(context)
        if (hasPermission) {
            viewModel.updateField { it.copy(usePopupReminder = true) }
            permissionMessage = ""
        } else {
            permissionMessage = "需要开启悬浮窗权限才能使用全屏弹窗提醒"
        }
    }

    // 首次进入加载
    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    val settings = state.settings

    if (showWhitelistScreen) {
        WhitelistScreen()
        // 添加返回按钮
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Button(
                onClick = { showWhitelistScreen = false }
            ) {
                Text("← 返回设置")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("喝水提醒设置", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            // 提醒方式选择
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("提醒方式")
                Spacer(Modifier.width(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            viewModel.updateField { it.copy(usePopupReminder = false) }
                            permissionMessage = ""
                        }
                        .padding(end = 16.dp)
                ) {
                    RadioButton(
                        selected = !settings.usePopupReminder,
                        onClick = {
                            viewModel.updateField { it.copy(usePopupReminder = false) }
                            permissionMessage = ""
                        }
                    )
                    Text("静默通知", modifier = Modifier.padding(start = 4.dp))
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            // 检查全屏通知权限（Android 14+优先检查）
                            if (!PermissionUtils.hasFullScreenIntentPermission(context)) {
                                // 请求全屏通知权限
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                    intent.data = Uri.parse("package:${context.packageName}")
                                    fullScreenPermissionLauncher.launch(intent)
                                }
                            }
                            // 检查系统弹窗权限（备用方案）
                            else if (!PermissionUtils.hasSystemAlertPermission(context)) {
                                // 请求系统弹窗权限
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    systemAlertPermissionLauncher.launch(intent)
                                }
                            } else {
                                viewModel.updateField { it.copy(usePopupReminder = true) }
                                permissionMessage = ""
                            }
                        }
                ) {
                    RadioButton(
                        selected = settings.usePopupReminder,
                        onClick = {
                            // 检查全屏通知权限（Android 14+优先检查）
                            if (!PermissionUtils.hasFullScreenIntentPermission(context)) {
                                // 请求全屏通知权限
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                    intent.data = Uri.parse("package:${context.packageName}")
                                    fullScreenPermissionLauncher.launch(intent)
                                }
                            }
                            // 检查系统弹窗权限（备用方案）
                            else if (!PermissionUtils.hasSystemAlertPermission(context)) {
                                // 请求系统弹窗权限
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    systemAlertPermissionLauncher.launch(intent)
                                }
                            } else {
                                viewModel.updateField { it.copy(usePopupReminder = true) }
                                permissionMessage = ""
                            }
                        }
                    )
                    Text("弹窗", modifier = Modifier.padding(start = 4.dp))
                }
            }
            
            // 权限提示信息
            if (permissionMessage.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = permissionMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

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
                
                // 开始时间
                OutlinedTextField(
                    value = "%02d:%02d".format(settings.startHour, settings.startMinute),
                    onValueChange = { },
                    modifier = Modifier
                        .width(80.dp)
                        .clickable { showStartTimePicker = true },
                    readOnly = true,
                    enabled = false,
                    placeholder = { Text("09:00") },
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Text(" 至 ")
                
                // 结束时间
                OutlinedTextField(
                    value = "%02d:%02d".format(settings.endHour, settings.endMinute),
                    onValueChange = { },
                    modifier = Modifier
                        .width(80.dp)
                        .clickable { showEndTimePicker = true },
                    readOnly = true,
                    enabled = false,
                    placeholder = { Text("18:00") },
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            
            // 时间范围说明
            Text(
                "点击时间可修改，只在此时间段内提醒",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 应用白名单设置
            OutlinedButton(
                onClick = { showWhitelistScreen = true },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = "应用白名单",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("应用白名单管理")
            }
            
            Text(
                "当白名单应用活跃时暂停全屏提醒",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))
            
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
        
        // 开始时间选择器
        if (showStartTimePicker) {
            TimePickerDialog(
                title = "选择开始时间",
                initialHour = settings.startHour,
                initialMinute = settings.startMinute,
                onTimeSelected = { hour, minute ->
                    // 验证开始时间不能晚于结束时间
                    val startMinutes = hour * 60 + minute
                    val endMinutes = settings.endHour * 60 + settings.endMinute
                    
                    if (startMinutes >= endMinutes) {
                        // 如果开始时间晚于结束时间，自动调整结束时间
                        val newEndHour = if (hour == 23) 0 else hour + 1
                        val newEndMinute = minute
                        viewModel.updateField { 
                            it.copy(
                                startHour = hour, 
                                startMinute = minute,
                                endHour = newEndHour,
                                endMinute = newEndMinute
                            ) 
                        }
                    } else {
                        viewModel.updateField { it.copy(startHour = hour, startMinute = minute) }
                    }
                    showStartTimePicker = false
                },
                onDismiss = { showStartTimePicker = false }
            )
        }
        
        // 结束时间选择器
        if (showEndTimePicker) {
            TimePickerDialog(
                title = "选择结束时间",
                initialHour = settings.endHour,
                initialMinute = settings.endMinute,
                onTimeSelected = { hour, minute ->
                    // 验证结束时间不能早于开始时间
                    val startMinutes = settings.startHour * 60 + settings.startMinute
                    val endMinutes = hour * 60 + minute
                    
                    if (endMinutes <= startMinutes) {
                        // 如果结束时间早于开始时间，自动调整开始时间
                        val newStartHour = if (hour == 0) 23 else hour - 1
                        val newStartMinute = minute
                        viewModel.updateField { 
                            it.copy(
                                startHour = newStartHour,
                                startMinute = newStartMinute,
                                endHour = hour, 
                                endMinute = minute
                            ) 
                        }
                    } else {
                        viewModel.updateField { it.copy(endHour = hour, endMinute = minute) }
                    }
                    showEndTimePicker = false
                },
                onDismiss = { showEndTimePicker = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onTimeSelected(timePickerState.hour, timePickerState.minute)
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
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
    // 按照周一到周日的顺序，对应Calendar的值：周日=1, 周一=2, ..., 周六=7
    val weekdays = listOf(
        2 to "周一", 3 to "周二", 4 to "周三", 5 to "周四", 
        6 to "周五", 7 to "周六", 1 to "周日"
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weekdays.forEach { (dayNum, label) ->
            val selected = selectedDays.contains(dayNum)
            Button(
                onClick = { onDayToggle(dayNum, !selected) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(
                    text = label.substring(1), // 只显示"一二三四五六日"
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 