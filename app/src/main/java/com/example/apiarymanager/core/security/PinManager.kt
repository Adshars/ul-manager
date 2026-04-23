package com.example.apiarymanager.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** True if the user has set a PIN. */
    val isPinSet: Boolean
        get() = prefs.getString(KEY_PIN_HASH, null) != null

    /** True if the user has enabled biometric login. */
    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) { prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply() }

    /** True if the onboarding (carousel + pin setup) has been completed. */
    var isOnboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) { prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply() }

    /**
     * Saves the PIN hash. Overwrites any existing PIN.
     * @param pin 4-digit string
     */
    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    /**
     * Verifies the entered PIN against the stored hash.
     * @return true if correct
     */
    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return stored == hash(pin)
    }

    /** Removes the stored PIN and disables biometric login. */
    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val PREFS_FILE           = "apiary_secure_prefs"
        const val KEY_PIN_HASH         = "pin_hash"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_ONBOARDING_DONE  = "onboarding_done"
    }
}
