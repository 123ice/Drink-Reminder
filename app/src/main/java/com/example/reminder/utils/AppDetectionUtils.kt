package com.example.reminder.utils

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

object AppDetectionUtils {
    
    private const val TAG = "AppDetectionUtils"
    
    /**
     * 检查是否有使用情况访问权限
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } else {
            true
        }
    }
    
    /**
     * 请求使用情况访问权限
     */
    fun requestUsageStatsPermission(activity: ComponentActivity, onResult: (Boolean) -> Unit) {
        if (!hasUsageStatsPermission(activity)) {
            val launcher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { _ ->
                onResult(hasUsageStatsPermission(activity))
            }
            
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            launcher.launch(intent)
        } else {
            onResult(true)
        }
    }
    
    /**
     * 获取当前前台应用的包名
     */
    fun getCurrentForegroundApp(context: Context, intervalMinutes: Int = 30): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && hasUsageStatsPermission(context)) {
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                
                // 方案1：使用事件查询（检测最近的应用切换）
                val eventResult = getCurrentForegroundAppByEvents(usageStatsManager, time)
                
                // 方案2：使用使用统计查询（检测最近活跃的应用）
                val statsResult = getCurrentForegroundAppByStats(usageStatsManager, time, intervalMinutes)
                
                // 优先使用事件查询结果，如果没有则使用统计查询结果
                val result = eventResult ?: statsResult
                
                Log.d(TAG, "检测结果 - 事件查询: $eventResult, 统计查询: $statsResult, 最终结果: $result (间隔: ${intervalMinutes}分钟)")
                
                result
            } catch (e: Exception) {
                Log.e(TAG, "获取前台应用失败: ${e.message}")
                null
            }
        } else {
            // Fallback for older versions (requires different permission)
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                @Suppress("DEPRECATION")
                val runningTasks = activityManager.getRunningTasks(1)
                val currentApp = if (runningTasks.isNotEmpty()) {
                    runningTasks[0].topActivity?.packageName
                } else null
                
                Log.d(TAG, "使用 ActivityManager 检测到前台应用: $currentApp")
                currentApp
            } catch (e: Exception) {
                Log.e(TAG, "ActivityManager 检测失败: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 通过事件查询获取当前前台应用（适用于最近有应用切换的情况）
     */
    private fun getCurrentForegroundAppByEvents(usageStatsManager: UsageStatsManager, time: Long): String? {
        try {
            // 查询最近5分钟的事件
            val usageEvents = usageStatsManager.queryEvents(time - 1000 * 60 * 5, time)
            val event = android.app.usage.UsageEvents.Event()
            var lastForegroundApp: String? = null
            var lastEventTime = 0L
            
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED && event.timeStamp > lastEventTime) {
                    lastForegroundApp = event.packageName
                    lastEventTime = event.timeStamp
                }
            }
            
            if (lastEventTime > 0) {
                val timeDiff = time - lastEventTime
                Log.d(TAG, "事件检测: $lastForegroundApp (${timeDiff}ms前)")
                
                // 如果事件太久远（超过2分钟），认为可能不准确
                if (timeDiff > 1000 * 60 * 2) {
                    Log.w(TAG, "事件过于久远，可能不准确")
                    return null
                }
            }
            
            return lastForegroundApp
        } catch (e: Exception) {
            Log.e(TAG, "事件查询失败: ${e.message}")
            return null
        }
    }
    
    /**
     * 通过使用统计获取当前前台应用（适用于长时间使用同一应用的情况）
     */
    private fun getCurrentForegroundAppByStats(usageStatsManager: UsageStatsManager, time: Long, intervalMinutes: Int): String? {
        try {
            // 动态查询窗口：使用喝水间隔时间，最少30分钟，最多2小时
            val windowMinutes = maxOf(30, minOf(intervalMinutes, 120))
            val windowMillis = windowMinutes * 60 * 1000L
            
            // 活跃判断阈值：喝水间隔的一半，最少5分钟，最多30分钟
            val activeThresholdMinutes = maxOf(5, minOf(intervalMinutes / 2, 30))
            val activeThresholdMillis = activeThresholdMinutes * 60 * 1000L
            
            Log.d(TAG, "统计查询窗口: ${windowMinutes}分钟, 活跃阈值: ${activeThresholdMinutes}分钟")
            
            val usageStats = usageStatsManager.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_BEST,
                time - windowMillis,
                time
            )
            
            if (usageStats.isEmpty()) {
                Log.d(TAG, "统计查询: 没有使用数据")
                return null
            }
            
            // 找到最近使用的应用（按最后使用时间排序）
            val recentApp = usageStats
                .filter { it.lastTimeUsed > 0 && it.totalTimeInForeground > 0 }
                .maxByOrNull { it.lastTimeUsed }
            
            if (recentApp != null) {
                val timeDiff = time - recentApp.lastTimeUsed
                val timeDiffMinutes = timeDiff / (1000 * 60)
                Log.d(TAG, "统计检测: ${recentApp.packageName} (最后使用: ${timeDiffMinutes}分钟前, 前台时长: ${recentApp.totalTimeInForeground}ms)")
                
                // 如果最后使用时间在阈值内，认为可能仍在使用
                if (timeDiff <= activeThresholdMillis) {
                    Log.d(TAG, "应用在活跃阈值内，认为仍在使用")
                    return recentApp.packageName
                } else {
                    Log.d(TAG, "应用最后使用时间太久远: ${timeDiffMinutes}分钟前 (阈值: ${activeThresholdMinutes}分钟)")
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "统计查询失败: ${e.message}")
            return null
        }
    }
    
    /**
     * 获取已安装的应用列表（显示所有有启动器的应用）
     */
    fun getInstalledApps(context: Context): List<Pair<String, String>> {
        val packageManager = context.packageManager
        
        // 获取所有有启动器入口的应用
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val launchableApps = packageManager.queryIntentActivities(launcherIntent, 0)
        
        return launchableApps
            .mapNotNull { resolveInfo ->
                try {
                    val packageName = resolveInfo.activityInfo.packageName
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    
                    // 排除系统核心应用（如设置、拨号器等）
                    if (isSystemCoreApp(packageName)) {
                        null
                    } else {
                        packageName to appName
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .distinctBy { it.first } // 去重（同一个应用可能有多个启动器入口）
            .sortedBy { it.second } // 按应用名称排序
    }
    
    /**
     * 判断是否为系统核心应用（这些应用通常不需要加入白名单）
     */
    private fun isSystemCoreApp(packageName: String): Boolean {
        val systemCoreApps = setOf(
            "com.android.settings",
            "com.android.systemui",
            "com.android.launcher",
            "com.android.launcher3",
            "com.android.dialer",
            "com.android.contacts",
            "com.android.mms",
            "com.android.phone",
            "com.android.calculator2",
            "com.android.calendar",
            "com.android.deskclock",
            "com.android.gallery3d",
            "com.android.camera",
            "com.android.camera2",
            "android"
        )
        return systemCoreApps.contains(packageName) || packageName.startsWith("com.android.internal")
    }
    
    /**
     * 检查指定应用是否在白名单中且当前活跃
     */
    fun isWhitelistAppActive(context: Context, whitelistPackages: Set<String>, intervalMinutes: Int = 30): Boolean {
        if (whitelistPackages.isEmpty()) {
            Log.d(TAG, "白名单为空，跳过检查")
            return false
        }
        
        val currentApp = getCurrentForegroundApp(context, intervalMinutes)
        val isActive = currentApp != null && whitelistPackages.contains(currentApp)
        
        Log.d(TAG, "白名单检查 - 当前应用: $currentApp, 白名单: $whitelistPackages, 是否匹配: $isActive (间隔: ${intervalMinutes}分钟)")
        
        return isActive
    }
} 