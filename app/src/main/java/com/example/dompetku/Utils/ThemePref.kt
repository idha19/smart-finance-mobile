package com.example.dompetku.Utils

import android.content.Context

class ThemePref(context: Context) {

    private val pref = context.getSharedPreferences("theme_mode", Context.MODE_PRIVATE)

    fun saveDarkMode(state: Boolean) {
        pref.edit().putBoolean("dark_mode", state).apply()
    }
    fun isDarkMode(): Boolean {
        return pref.getBoolean("dark_mode", false)
    }
    fun clear() {
        pref.edit().clear().apply()
    }
}