package com.sreeman.mywifi.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sreeman.mywifi.data.model.LoginCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure local storage manager using [EncryptedSharedPreferences] with AES256_GCM key scheme
 * to safely persist sensitive router admin credentials and gateway settings.
 */
@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback or handle key invalidation (e.g. key store corrupted during OS upgrade)
        context.getSharedPreferences("${PREFS_FILENAME}_fallback", Context.MODE_PRIVATE)
    }

    fun saveCredentials(username: String, password: String, gatewayIp: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_GATEWAY_IP, gatewayIp)
            .putBoolean(KEY_REMEMBER_ME, true)
            .apply()
    }

    fun getSavedCredentials(): LoginCredentials? {
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val password = prefs.getString(KEY_PASSWORD, null) ?: return null
        val gatewayIp = prefs.getString(KEY_GATEWAY_IP, "192.168.0.1") ?: "192.168.0.1"
        return LoginCredentials(username = username, password = password, gatewayIp = gatewayIp)
    }

    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .putBoolean(KEY_REMEMBER_ME, false)
            .apply()
    }

    fun saveGatewayIp(gatewayIp: String) {
        prefs.edit().putString(KEY_GATEWAY_IP, gatewayIp).apply()
    }

    fun getGatewayIp(): String {
        return prefs.getString(KEY_GATEWAY_IP, "192.168.0.1") ?: "192.168.0.1"
    }

    companion object {
        private const val PREFS_FILENAME = "secure_router_admin_prefs"
        private const val KEY_USERNAME = "router_admin_username"
        private const val KEY_PASSWORD = "router_admin_password"
        private const val KEY_GATEWAY_IP = "router_gateway_ip"
        private const val KEY_REMEMBER_ME = "router_remember_me"
    }
}
