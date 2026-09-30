package com.sreeman.mywifi.data.remote

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe in-memory [CookieJar] for OkHttp to capture, maintain,
 * and attach router session cookies (e.g., SESSIONID, sys_auth, token) across requests.
 */
class InMemoryCookieJar : CookieJar {

    private val cookieStore: ConcurrentHashMap<String, MutableList<Cookie>> = ConcurrentHashMap()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val currentCookies = cookieStore.getOrPut(host) { mutableListOf() }

        synchronized(currentCookies) {
            for (cookie in cookies) {
                // Remove existing cookie with the same name if present
                currentCookies.removeAll { it.name == cookie.name }
                if (cookie.expiresAt > System.currentTimeMillis()) {
                    currentCookies.add(cookie)
                }
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val cookies = cookieStore[host] ?: return emptyList()

        val now = System.currentTimeMillis()
        synchronized(cookies) {
            // Remove expired cookies
            cookies.removeAll { it.expiresAt < now }
            return cookies.toList()
        }
    }

    /**
     * Clears stored cookies to terminate the router admin session upon logout or timeout.
     */
    fun clearCookies() {
        cookieStore.clear()
    }

    /**
     * Returns true if active session cookies exist for the specified host.
     */
    fun hasActiveSession(host: String = "192.168.0.1"): Boolean {
        val cookies = cookieStore[host] ?: return false
        val now = System.currentTimeMillis()
        return synchronized(cookies) {
            cookies.any { it.expiresAt > now }
        }
    }
}
