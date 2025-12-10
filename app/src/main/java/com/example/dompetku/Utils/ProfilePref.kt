package com.example.dompetku.Utils

import android.content.Context

class ProfilePref(context: Context) {

    private val pref = context.getSharedPreferences("profile_data", Context.MODE_PRIVATE)

    fun saveName(name: String) {
        pref.edit().putString("nama", name).apply()
    }

    fun getName(): String {
        return pref.getString("nama", "Nama Pengguna") ?: "Nama Pengguna"
    }

    fun savePhoto(uri: String) {
        pref.edit().putString("foto", uri).apply()
    }

    fun getPhoto(): String? {
        return pref.getString("foto", null)
    }

    fun clear() {
        pref.edit().clear().apply()
    }
}