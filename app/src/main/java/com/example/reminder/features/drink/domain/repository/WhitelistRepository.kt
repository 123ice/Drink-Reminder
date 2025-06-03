package com.example.reminder.features.drink.domain.repository

import android.content.Context
import com.example.reminder.features.drink.domain.model.WhitelistApp
import com.example.reminder.features.drink.domain.model.WhitelistSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object WhitelistRepository {
    private val _whitelistFlow = MutableStateFlow(WhitelistSettings())
    
    fun getWhitelistFlow(): Flow<WhitelistSettings> = _whitelistFlow.asStateFlow()
    
    fun loadWhitelist(context: Context): WhitelistSettings {
        val sp = context.getSharedPreferences("app_whitelist", Context.MODE_PRIVATE)
        
        val isEnabled = sp.getBoolean("whitelist_enabled", true)
        val appsJson = sp.getString("whitelist_apps", "")
        
        val apps = if (appsJson.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                // 简单的序列化，格式：packageName1|appName1|enabled1;packageName2|appName2|enabled2
                appsJson.split(";").mapNotNull { appData ->
                    val parts = appData.split("|")
                    if (parts.size == 3) {
                        WhitelistApp(
                            packageName = parts[0],
                            appName = parts[1],
                            isEnabled = parts[2].toBoolean()
                        )
                    } else null
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        val settings = WhitelistSettings(apps = apps, isWhitelistEnabled = isEnabled)
        _whitelistFlow.value = settings
        return settings
    }
    
    fun saveWhitelist(context: Context, settings: WhitelistSettings) {
        val sp = context.getSharedPreferences("app_whitelist", Context.MODE_PRIVATE)
        
        // 序列化应用列表
        val appsJson = settings.apps.joinToString(";") { app ->
            "${app.packageName}|${app.appName}|${app.isEnabled}"
        }
        
        sp.edit()
            .putBoolean("whitelist_enabled", settings.isWhitelistEnabled)
            .putString("whitelist_apps", appsJson)
            .apply()
            
        _whitelistFlow.value = settings
    }
    
    fun addApp(context: Context, app: WhitelistApp) {
        val current = _whitelistFlow.value
        val newApps = current.apps.toMutableList()
        
        // 检查是否已存在
        val existingIndex = newApps.indexOfFirst { it.packageName == app.packageName }
        if (existingIndex >= 0) {
            newApps[existingIndex] = app
        } else {
            newApps.add(app)
        }
        
        val newSettings = current.copy(apps = newApps)
        saveWhitelist(context, newSettings)
    }
    
    fun removeApp(context: Context, packageName: String) {
        val current = _whitelistFlow.value
        val newApps = current.apps.filter { it.packageName != packageName }
        val newSettings = current.copy(apps = newApps)
        saveWhitelist(context, newSettings)
    }
    
    fun toggleAppEnabled(context: Context, packageName: String) {
        val current = _whitelistFlow.value
        val newApps = current.apps.map { app ->
            if (app.packageName == packageName) {
                app.copy(isEnabled = !app.isEnabled)
            } else app
        }
        val newSettings = current.copy(apps = newApps)
        saveWhitelist(context, newSettings)
    }
    
    fun setWhitelistEnabled(context: Context, enabled: Boolean) {
        val current = _whitelistFlow.value
        val newSettings = current.copy(isWhitelistEnabled = enabled)
        saveWhitelist(context, newSettings)
    }
    
    fun getEnabledPackageNames(): Set<String> {
        val current = _whitelistFlow.value
        return if (current.isWhitelistEnabled) {
            current.apps.filter { it.isEnabled }.map { it.packageName }.toSet()
        } else {
            emptySet()
        }
    }
} 