package com.example.apiarymanager.core.util

/**
 * Generic wrapper for UI state representing an async operation outcome.
 *
 * Usage in ViewModel:
 *   _state.value = Resource.Loading
 *   _state.value = Resource.Success(data)
 *   _state.value = Resource.Error("Something went wrong")
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
}

/** Convenience — returns data or null without pattern matching. */
fun <T> Resource<T>.dataOrNull(): T? = (this as? Resource.Success)?.data

/** Returns true only for the Loading state. */
val Resource<*>.isLoading: Boolean get() = this is Resource.Loading
