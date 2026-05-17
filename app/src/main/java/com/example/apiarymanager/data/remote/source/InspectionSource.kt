package com.example.apiarymanager.data.remote.source

import com.example.apiarymanager.data.dto.CreateInspectionRequest
import com.example.apiarymanager.data.dto.UpdateInspectionRequest
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.remote.api.InspectionApi
import com.example.apiarymanager.domain.model.Inspection
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InspectionSource @Inject constructor(private val api: InspectionApi) {

    private val etags = ConcurrentHashMap<Long, String>()

    suspend fun getByHive(hiveId: Long, page: Int = 1, pageSize: Int = 50): Result<List<Inspection>> = safeCall {
        api.getByHive(hiveId, page, pageSize).items.map { it.toDomain() }
    }

    suspend fun getById(id: Long): Result<Inspection> = safeCall {
        val response = api.getById(id)
        response.cacheEtag(id)
        response.bodyOrThrow().toDomain()
    }

    suspend fun create(request: CreateInspectionRequest): Result<Inspection> = safeCall {
        val response = api.create(request)
        val body = response.bodyOrThrow()
        response.cacheEtag(body.id)
        body.toDomain()
    }

    suspend fun update(id: Long, request: UpdateInspectionRequest): Result<Inspection> = safeCall {
        val response = api.update(id, etags.require(id), request)
        val body = response.bodyOrThrow()
        response.cacheEtag(id)
        body.toDomain()
    }

    suspend fun delete(id: Long): Result<Unit> = safeCall {
        val response = api.delete(id, etags.require(id))
        if (!response.isSuccessful) throw HttpException(response)
        etags.remove(id)
    }

    private fun <T> Response<T>.cacheEtag(id: Long) {
        headers()["ETag"]?.let { etags[id] = it }
    }

    private fun <T> Response<T>.bodyOrThrow(): T {
        if (!isSuccessful) throw HttpException(this)
        return body() ?: throw HttpException(this)
    }

    private fun ConcurrentHashMap<Long, String>.require(id: Long): String =
        this[id] ?: throw IllegalStateException("No ETag cached for id=$id — call getById first")

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: IOException) {
        Result.failure(e)
    }
}
