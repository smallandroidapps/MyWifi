package com.sreeman.mywifi.data.remote

import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit interface targeting ACT Fibernet / TP-Link router CGI endpoints.
 */
interface RouterApiService {

    @GET
    suspend fun getLoginPage(
        @Url url: String
    ): Response<String>

    @FormUrlEncoded
    @POST
    suspend fun login(
        @Url url: String,
        @Header("Referer") referer: String? = null,
        @Header("Authorization") authHeader: String? = null,
        @FieldMap fields: Map<String, String>
    ): Response<String>

    @POST
    suspend fun postUrl(
        @Url url: String,
        @Header("Referer") referer: String? = null,
        @Header("Authorization") authHeader: String? = null
    ): Response<String>

    @GET
    suspend fun getStatusPage(
        @Url url: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<String>

    @GET
    suspend fun getConnectedDevicesPage(
        @Url url: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<String>

    @GET
    suspend fun getWifiSettingsPage(
        @Url url: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<String>

    @FormUrlEncoded
    @POST
    suspend fun updateWifiSettings(
        @Url url: String,
        @Header("Referer") referer: String? = null,
        @Header("Authorization") authHeader: String? = null,
        @FieldMap fields: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST
    suspend fun rebootRouter(
        @Url url: String,
        @Header("Referer") referer: String? = null,
        @Header("Authorization") authHeader: String? = null,
        @FieldMap fields: Map<String, String>
    ): Response<String>
}
