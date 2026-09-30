package com.sreeman.mywifi.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.sreeman.mywifi.data.model.NetworkState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android system network inspector utilizing [ConnectivityManager] and [WifiManager]
 * to verify active Wi-Fi connection, detect the default router Gateway IP (e.g. 192.168.0.1),
 * and extract the current SSID/BSSID asynchronously on [Dispatchers.IO].
 */
@Singleton
class GatewayChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * Inspects active network transport and routes off the main thread to prevent binder latency.
     */
    suspend fun checkNetworkState(): NetworkState = withContext(Dispatchers.IO) {
        val activeNetwork = connectivityManager.activeNetwork ?: return@withContext NetworkState(isWifiConnected = false)
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        if (!isWifi) {
            return@withContext NetworkState(isWifiConnected = false)
        }

        // Fetch Gateway IP address from Default Route in LinkProperties
        val linkProperties = connectivityManager.getLinkProperties(activeNetwork)
        val defaultGatewayAddress: InetAddress? = linkProperties?.routes
            ?.firstOrNull { it.isDefaultRoute && it.gateway != null }
            ?.gateway

        val gatewayIp = defaultGatewayAddress?.hostAddress ?: fallbackGetGatewayIpFromDhcp()

        // Fetch SSID and BSSID safely from WifiInfo
        @Suppress("DEPRECATION")
        val wifiInfo = wifiManager.connectionInfo
        val rawSsid = wifiInfo?.ssid
        val cleanSsid = rawSsid
            ?.replace("\"", "")
            ?.takeIf { it != "<unknown ssid>" && it != "0x" }

        val bssid = wifiInfo?.bssid?.takeIf { it != "02:00:00:00:00:00" }

        // Local IP
        val localIp = linkProperties?.linkAddresses
            ?.firstOrNull { it.address is java.net.Inet4Address }
            ?.address?.hostAddress

        NetworkState(
            isWifiConnected = true,
            gatewayIp = gatewayIp ?: "192.168.0.1",
            connectedSsid = cleanSsid ?: "Connected Wi-Fi",
            bssid = bssid,
            localIpAddress = localIp
        )
    }

    /**
     * Fallback mechanism to retrieve Gateway IP address from DHCP info if routes are unavailable.
     */
    @Suppress("DEPRECATION")
    private fun fallbackGetGatewayIpFromDhcp(): String? {
        return try {
            val dhcpInfo = wifiManager.dhcpInfo ?: return null
            val gatewayInt = dhcpInfo.gateway
            if (gatewayInt == 0) return null
            "${gatewayInt and 0xFF}.${gatewayInt shr 8 and 0xFF}.${gatewayInt shr 16 and 0xFF}.${gatewayInt shr 24 and 0xFF}"
        } catch (e: Exception) {
            "192.168.0.1"
        }
    }
}
