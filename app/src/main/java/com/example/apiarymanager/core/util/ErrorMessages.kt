package com.example.apiarymanager.core.util

import com.example.apiarymanager.core.network.ApiException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> when (code) {
        400  -> parseBackendMessage(rawBody) ?: "Nieprawidłowe dane. Sprawdź formularz."
        401  -> "Sesja wygasła. Zaloguj się ponownie."
        403  -> "Brak uprawnień do wykonania tej operacji."
        404  -> "Nie znaleziono zasobu."
        409  -> "Konflikt danych — zasób już istnieje."
        422  -> parseBackendMessage(rawBody) ?: "Dane nie przeszły walidacji."
        in 500..599 -> "Błąd serwera. Spróbuj ponownie za chwilę."
        else -> "Błąd komunikacji (HTTP $code)."
    }
    is IOException   -> "Brak połączenia z siecią. Sprawdź internet."
    is IllegalStateException -> message ?: "Błąd synchronizacji danych."
    else             -> message ?: "Wystąpił nieznany błąd."
}

private fun parseBackendMessage(body: String?): String? {
    body ?: return null
    return runCatching {
        val obj = Json.parseToJsonElement(body).jsonObject
        obj["message"]?.jsonPrimitive?.contentOrNull
            ?: obj["error"]?.jsonPrimitive?.contentOrNull
            ?: obj["detail"]?.jsonPrimitive?.contentOrNull
            ?: obj["title"]?.jsonPrimitive?.contentOrNull
    }.getOrNull()
}
