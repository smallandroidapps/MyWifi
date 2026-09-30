package com.sreeman.mywifi.di

import android.content.Context
import com.sreeman.mywifi.data.local.SecurePreferencesManager
import com.sreeman.mywifi.data.network.GatewayChecker
import com.sreeman.mywifi.data.remote.InMemoryCookieJar
import com.sreeman.mywifi.data.remote.RouterApiService
import com.sreeman.mywifi.data.remote.RouterSessionInterceptor
import com.sreeman.mywifi.data.repository.RouterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCookieJar(): InMemoryCookieJar {
        return InMemoryCookieJar()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cookieJar: InMemoryCookieJar
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(RouterSessionInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // Placeholder base URL; dynamic URLs passed via @Url parameter in RouterApiService
        return Retrofit.Builder()
            .baseUrl("http://192.168.0.1/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRouterApiService(retrofit: Retrofit): RouterApiService {
        return retrofit.create(RouterApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGatewayChecker(
        @ApplicationContext context: Context
    ): GatewayChecker {
        return GatewayChecker(context)
    }

    @Provides
    @Singleton
    fun provideSecurePreferencesManager(
        @ApplicationContext context: Context
    ): SecurePreferencesManager {
        return SecurePreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideRouterRepository(
        apiService: RouterApiService,
        cookieJar: InMemoryCookieJar,
        securePrefs: SecurePreferencesManager,
        gatewayChecker: GatewayChecker
    ): RouterRepository {
        return RouterRepository(apiService, cookieJar, securePrefs, gatewayChecker)
    }
}
