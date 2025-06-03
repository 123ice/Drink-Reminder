package com.example.reminder

object PreferenceHelper {
    private const val PREF_NAME = "reminder_prefs"
    private const val KEY_WORK_MINUTES = "work_minutes"
    private const val KEY_REST_MINUTES = "rest_minutes"

    fun saveTimes(context: android.content.Context, workMinutes: Int, restMinutes: Int) {
        val sp = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE)
        sp.edit()
            .putInt(KEY_WORK_MINUTES, workMinutes)
            .putInt(KEY_REST_MINUTES, restMinutes)
            .apply()
    }

    fun getWorkMinutes(context: android.content.Context): Int {
        val sp = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE)
        return sp.getInt(KEY_WORK_MINUTES, 30) // 默认30分钟
    }

    fun getRestMinutes(context: android.content.Context): Int {
        val sp = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE)
        return sp.getInt(KEY_REST_MINUTES, 5) // 默认5分钟
    }
}
