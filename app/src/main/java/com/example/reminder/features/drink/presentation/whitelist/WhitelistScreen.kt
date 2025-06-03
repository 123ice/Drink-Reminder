package com.example.reminder.features.drink.presentation.whitelist

import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.reminder.features.drink.domain.model.WhitelistApp
import com.example.reminder.features.drink.domain.repository.WhitelistRepository
import com.example.reminder.utils.AppDetectionUtils

@Composable
fun WhitelistScreen() {
    val context = LocalContext.current
    var whitelist by remember { mutableStateOf(WhitelistRepository.loadWhitelist(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasUsagePermission by remember { mutableStateOf(AppDetectionUtils.hasUsageStatsPermission(context)) }
    
    // 权限请求launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        hasUsagePermission = AppDetectionUtils.hasUsageStatsPermission(context)
        if (!hasUsagePermission) {
            showPermissionDialog = true
        }
    }
    
    LaunchedEffect(Unit) {
        if (!hasUsagePermission) {
            showPermissionDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "应用白名单",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Switch(
                checked = whitelist.isWhitelistEnabled,
                onCheckedChange = { enabled ->
                    WhitelistRepository.setWhitelistEnabled(context, enabled)
                    whitelist = whitelist.copy(isWhitelistEnabled = enabled)
                }
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            "当这些应用活跃时，将暂停全屏弹窗提醒",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(Modifier.height(16.dp))
        
        // 权限状态提示
        if (!hasUsagePermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "需要使用情况访问权限",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "为了检测当前运行的应用，需要授予使用情况访问权限",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            permissionLauncher.launch(intent)
                        }
                    ) {
                        Text("授予权限")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        
        // 添加应用按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "白名单应用 (${whitelist.apps.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            IconButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加应用")
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // 应用列表
        LazyColumn {
            items(whitelist.apps) { app ->
                WhitelistAppItem(
                    app = app,
                    onToggle = { packageName ->
                        WhitelistRepository.toggleAppEnabled(context, packageName)
                        whitelist = WhitelistRepository.loadWhitelist(context)
                    },
                    onDelete = { packageName ->
                        WhitelistRepository.removeApp(context, packageName)
                        whitelist = WhitelistRepository.loadWhitelist(context)
                    }
                )
            }
        }
        
        if (whitelist.apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "暂无白名单应用",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "点击 + 按钮添加应用到白名单",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
    
    // 添加应用对话框
    if (showAddDialog) {
        AddAppDialog(
            onDismiss = { showAddDialog = false },
            onAppSelected = { app ->
                WhitelistRepository.addApp(context, app)
                whitelist = WhitelistRepository.loadWhitelist(context)
                showAddDialog = false
            }
        )
    }
    
    // 权限说明对话框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("权限说明") },
            text = { 
                Text("应用白名单功能需要「使用情况访问」权限来检测当前运行的应用。请在设置中找到本应用并开启此权限。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        permissionLauncher.launch(intent)
                        showPermissionDialog = false
                    }
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun WhitelistAppItem(
    app: WhitelistApp,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Switch(
                checked = app.isEnabled,
                onCheckedChange = { onToggle(app.packageName) }
            )
            
            Spacer(Modifier.width(8.dp))
            
            IconButton(
                onClick = { onDelete(app.packageName) }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddAppDialog(
    onDismiss: () -> Unit,
    onAppSelected: (WhitelistApp) -> Unit
) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        installedApps = AppDetectionUtils.getInstalledApps(context)
        isLoading = false
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "选择应用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn {
                        items(installedApps) { (packageName, appName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAppSelected(
                                            WhitelistApp(
                                                packageName = packageName,
                                                appName = appName,
                                                isEnabled = true
                                            )
                                        )
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                }
            }
        }
    }
} 