package com.example.apiarymanager.data.remote.source

import com.example.apiarymanager.data.dto.AnalysisResultDto
import com.example.apiarymanager.data.dto.AnalyzePhotoRequest
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.remote.api.HivePhotoApi
import com.example.apiarymanager.domain.model.HivePhoto
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HivePhotoSource @Inject constructor(private val api: HivePhotoApi) {

    suspend fun getByHive(hiveId: Long, page: Int = 1, pageSize: Int = 50): Result<List<HivePhoto>> = safeCall {
        api.getByHive(hiveId, page, pageSize).items.map { it.toDomain() }
    }

    suspend fun upload(
        hiveId: Long,
        file: MultipartBody.Part,
        inspectionId: Long? = null
    ): Result<HivePhoto> = safeCall {
        api.upload(hiveId, file, inspectionId).bodyOrThrow().toDomain()
    }

    suspend fun delete(hiveId: Long, photoId: Long): Result<Unit> = safeCall {
        val response = api.delete(hiveId, photoId)
        if (!response.isSuccessful) throw HttpException(response)
    }

    suspend fun analyze(hiveId: Long, photoId: Long, request: AnalyzePhotoRequest): Result<AnalysisResultDto> = safeCall {
        api.analyze(hiveId, photoId, request)
    }

    private fun <T> Response<T>.bodyOrThrow(): T {
        if (!isSuccessful) throw HttpException(this)
        return body() ?: throw HttpException(this)
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: IOException) {
        Result.failure(e)
    }
}
