package com.clipvault.clipvault.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SessionManager {
    private const val PREF_NAME = "user_session"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "USER_ID" // Tambahkan konstanta biar rapi

    private var preferences: SharedPreferences? = null

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        // Gunakan commit() biar langsung ditulis detik itu juga (Synchronous)
        val success = preferences?.edit()?.putString(KEY_TOKEN, token)?.commit() ?: false

        if (success) {
            Log.d("SessionManager", "✅ Token BERHASIL disimpan: $token")
        } else {
            Log.e("SessionManager", "❌ Gagal menyimpan token! Preferences mungkin null.")
        }
    }

    fun getToken(): String? {
        val token = preferences?.getString(KEY_TOKEN, null)
        Log.d("SessionManager", "🔍 Mengambil Token: $token")
        return token
    }

    fun clearSession() {
        preferences?.edit()?.clear()?.commit()
    }

    // === PERBAIKAN DI SINI ===

    fun saveUserId(id: Int) {
        // Ganti 'prefs' jadi 'preferences'
        // Pakai 'apply()' biar tersimpan di background (lebih cepat dari commit)
        preferences?.edit()?.putInt(KEY_USER_ID, id)?.apply()
    }

    fun getUserId(): Int {
        // Ganti 'prefs' jadi 'preferences'
        // Pakai elvis operator (?: 0) untuk jaga-jaga kalau preferences null
        return preferences?.getInt(KEY_USER_ID, 0) ?: 0
    }
}