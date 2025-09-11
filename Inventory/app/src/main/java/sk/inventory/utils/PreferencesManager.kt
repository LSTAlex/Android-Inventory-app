package sk.inventory.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREFS_NAME = "inventory_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_ROLE = "user_role"
    private const val KEY_TEMP_USER = "temp_username"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getPreferences(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(KEY_TOKEN, null)
    }

    fun saveRole(context: Context, role: String) {
        getPreferences(context).edit().putString(KEY_ROLE, role).apply()
    }

    fun getRole(context: Context): String? {
        return getPreferences(context).getString(KEY_ROLE, null)
    }

    fun clear(context: Context) {
        getPreferences(context).edit().clear().apply()
    }

    fun saveTempUser(context: Context, username: String) {
        getPreferences(context).edit().putString(KEY_TEMP_USER, username).apply()
    }

    fun getTempUser(context: Context): String? {
        return getPreferences(context).getString(KEY_TEMP_USER, null)
    }

    fun clearTempUser(context: Context) {
        getPreferences(context).edit().remove(KEY_TEMP_USER).apply()
    }
}