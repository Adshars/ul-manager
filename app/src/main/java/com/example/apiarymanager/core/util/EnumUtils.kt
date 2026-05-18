package com.example.apiarymanager.core.util

/**
 * Parses an enum by name first, then falls back to ordinal index if the server sends integers.
 */
inline fun <reified T : Enum<T>> parseEnum(value: String, default: T): T =
    runCatching { enumValueOf<T>(value) }.getOrElse {
        value.toIntOrNull()
            ?.let { idx -> runCatching { enumValues<T>()[idx] }.getOrElse { default } }
            ?: default
    }
