package com.example.apiarymanager.core.auth

import android.app.Activity
import android.content.Context
import com.example.apiarymanager.R
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Scope exposed by the backend API registration in Azure AD.
 * Adjust if the backend exposes a different scope name.
 */
private val API_SCOPES = arrayOf(
    "api://8b6b8a12-10b6-4e36-b985-f6cd0582e935/User.ReadWrite"
)

@Singleton
class MsalAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val appDeferred = CompletableDeferred<ISingleAccountPublicClientApplication>()

    init {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            R.raw.msal_config,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(app: ISingleAccountPublicClientApplication) {
                    appDeferred.complete(app)
                }
                override fun onError(ex: MsalException) {
                    appDeferred.completeExceptionally(ex)
                }
            }
        )
    }

    private suspend fun app() = appDeferred.await()

    // ─── Sign-in ──────────────────────────────────────────────────────────────

    suspend fun signIn(activity: Activity, loginHint: String? = null): Result<IAccount> = try {
        val app = app()
        getCurrentAccount()?.let { return Result.success(it) }
        suspendCancellableCoroutine { cont ->
            app.signIn(activity, loginHint, API_SCOPES, object : AuthenticationCallback {
                override fun onSuccess(result: IAuthenticationResult) =
                    cont.resume(Result.success(result.account))
                override fun onError(ex: MsalException) =
                    cont.resume(Result.failure(ex))
                override fun onCancel() =
                    cont.resume(Result.failure(Exception("Logowanie anulowane")))
            })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ─── Sign-out ─────────────────────────────────────────────────────────────

    suspend fun signOut(): Result<Unit> = try {
        val app = app()
        suspendCancellableCoroutine { cont ->
            app.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() = cont.resume(Result.success(Unit))
                override fun onError(ex: MsalException) = cont.resume(Result.failure(ex))
            })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ─── Token acquisition ────────────────────────────────────────────────────

    suspend fun getToken(forceRefresh: Boolean = false): String? = withContext(Dispatchers.IO) {
        try {
            val app = app()
            val account = getCurrentAccount() ?: return@withContext null
            suspendCancellableCoroutine<String?> { cont ->
                val params = AcquireTokenSilentParameters.Builder()
                    .fromAuthority(account.authority)
                    .forAccount(account)
                    .withScopes(API_SCOPES.toList())
                    .forceRefresh(forceRefresh)
                    .withCallback(object : SilentAuthenticationCallback {
                        override fun onSuccess(result: IAuthenticationResult) =
                            cont.resume(result.accessToken)
                        override fun onError(ex: MsalException) =
                            cont.resume(null)
                    })
                    .build()
                app.acquireTokenSilentAsync(params)
            }
        } catch (e: Exception) {
            null
        }
    }

    // ─── Account state ────────────────────────────────────────────────────────

    suspend fun getCurrentAccount(): IAccount? = try {
        val app = app()
        suspendCancellableCoroutine { cont ->
            app.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
                override fun onAccountLoaded(activeAccount: IAccount?) =
                    cont.resume(activeAccount)
                override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) =
                    cont.resume(currentAccount)
                override fun onError(ex: MsalException) =
                    cont.resume(null)
            })
        }
    } catch (e: Exception) {
        null
    }

    suspend fun isSignedIn(): Boolean = getCurrentAccount() != null
}
