package com.example.apiarymanager.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Thin wrapper around [BiometricPrompt] for use in Compose screens.
 * Call [authenticate] from an [FragmentActivity] context (obtainable via
 * `LocalContext.current as FragmentActivity`).
 */
object BiometricHelper {

    private var activePrompt: BiometricPrompt? = null

    /** Programmatically dismisses the currently showing biometric prompt, if any. */
    fun cancelAuthentication() {
        activePrompt?.cancelAuthentication()
        activePrompt = null
    }

    /** True if the device has enrolled biometrics and can authenticate. */
    fun isAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BIOMETRIC_WEAK or BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Shows the system biometric prompt.
     * @param activity   hosting FragmentActivity (use `LocalContext.current as FragmentActivity`)
     * @param title      title shown in the dialog
     * @param subtitle   subtitle / hint
     * @param negativeButtonText  text for the "cancel / use PIN" button
     * @param onSuccess  called when authentication succeeds
     * @param onError    called with a user-readable error message
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Uwierzytelnienie",
        subtitle: String = "Użyj biometrii, aby się zalogować",
        negativeButtonText: String = "Użyj PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {},
        onCancelled: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                activePrompt = null
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                activePrompt = null
                when (errorCode) {
                    BiometricPrompt.ERROR_CANCELED -> { /* programmatic cancel — ignore */ }
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED -> onCancelled()
                    else -> onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                onFailed()
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_WEAK or BIOMETRIC_STRONG)
            .build()

        activePrompt = BiometricPrompt(activity, executor, callback)
        activePrompt!!.authenticate(promptInfo)
    }
}
