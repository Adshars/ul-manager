package com.example.apiarymanager.di

import com.example.apiarymanager.BuildConfig
import com.example.apiarymanager.core.network.AuthInterceptor
import com.example.apiarymanager.core.network.MsalTokenProvider
import com.example.apiarymanager.core.network.TokenProvider
import com.example.apiarymanager.data.remote.api.ApiaryApi
import com.example.apiarymanager.data.remote.api.AuthApi
import com.example.apiarymanager.data.remote.api.DashboardApi
import com.example.apiarymanager.data.remote.api.FeedingApi
import com.example.apiarymanager.data.remote.api.HiveApi
import com.example.apiarymanager.data.remote.api.HivePhotoApi
import com.example.apiarymanager.data.remote.api.HoneyHarvestApi
import com.example.apiarymanager.data.remote.api.InspectionApi
import com.example.apiarymanager.data.remote.api.StatsApi
import com.example.apiarymanager.data.remote.api.TaskApi
import com.example.apiarymanager.data.remote.api.TreatmentApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenProvider(msalTokenProvider: MsalTokenProvider): TokenProvider = msalTokenProvider

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${BuildConfig.API_BASE_URL}/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideApiaryApi(retrofit: Retrofit): ApiaryApi =
        retrofit.create(ApiaryApi::class.java)

    @Provides @Singleton
    fun provideHiveApi(retrofit: Retrofit): HiveApi =
        retrofit.create(HiveApi::class.java)

    @Provides @Singleton
    fun provideInspectionApi(retrofit: Retrofit): InspectionApi =
        retrofit.create(InspectionApi::class.java)

    @Provides @Singleton
    fun provideTaskApi(retrofit: Retrofit): TaskApi =
        retrofit.create(TaskApi::class.java)

    @Provides @Singleton
    fun provideHivePhotoApi(retrofit: Retrofit): HivePhotoApi =
        retrofit.create(HivePhotoApi::class.java)

    @Provides @Singleton
    fun provideFeedingApi(retrofit: Retrofit): FeedingApi =
        retrofit.create(FeedingApi::class.java)

    @Provides @Singleton
    fun provideTreatmentApi(retrofit: Retrofit): TreatmentApi =
        retrofit.create(TreatmentApi::class.java)

    @Provides @Singleton
    fun provideHoneyHarvestApi(retrofit: Retrofit): HoneyHarvestApi =
        retrofit.create(HoneyHarvestApi::class.java)

    @Provides @Singleton
    fun provideDashboardApi(retrofit: Retrofit): DashboardApi =
        retrofit.create(DashboardApi::class.java)

    @Provides @Singleton
    fun provideStatsApi(retrofit: Retrofit): StatsApi =
        retrofit.create(StatsApi::class.java)
}
