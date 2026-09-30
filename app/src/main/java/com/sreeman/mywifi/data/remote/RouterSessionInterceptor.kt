package com.sreeman.mywifi.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RouterSessionExpiredException(message: String) : IOException(message)

/**
 * OkHttp Interceptor that injects desktop User-Agent, default Referer matching host IP,
 * and monitors response status to detect session expiration.
 */
class RouterSessionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val host = originalRequest.url.host
        val defaultReferer = "http://$host/"

        val builder = originalRequest.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Connection", "keep-alive")

        if (originalRequest.header("Referer") == null) {
            builder.header("Referer", defaultReferer)
        }

        val modifiedRequest = builder.build()
        val response = chain.proceed(modifiedRequest)

        // Check for HTTP 401 Unauthorized
        if (response.code == 401) {
            throw RouterSessionExpiredException("Router session unauthenticated or expired (HTTP 401).")
        }

        // Detect HTML redirects to login page (common in ACT router CGI endpoints)
        val redirectUrl = response.header("Location")
        if (redirectUrl != null && (redirectUrl.contains("login") || redirectUrl.contains("auth"))) {
            throw RouterSessionExpiredException("Router session expired (Redirected to login).")
        }

        return response
    }
}
