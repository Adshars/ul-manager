package com.example.apiarymanager.core.security

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

    /** True if the device has enrolled biometrics and can authenticate. */
    fun isAvailable(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
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
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Error code 13 = negative button pressed (user chose "Use PIN") — not an error
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                    errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                // Individual failure — do nothing, system shows feedback automatically
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_WEAK or BIOMETRIC_STRONG)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
