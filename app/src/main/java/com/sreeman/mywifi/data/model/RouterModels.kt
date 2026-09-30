package com.sreeman.mywifi.data.model

/**
 * Session authentication state for router management.
 */
sealed interface SessionState {
    object Idle : SessionState
    object Authenticating : SessionState
    data class Authenticated(val sessionToken: String? = null, val gatewayIp: String) : SessionState
    data class SessionExpired(val reason: String = "Router session expired. Please log in again.") : SessionState
    data class Error(val message: String) : SessionState
}

/**
 * Router system information and network status.
 */
data class RouterStatus(
    val routerModel: String = "ACT Fibernet Router",
    val gatewayIp: String = "192.168.0.1",
    val currentSsid: String = "ACT_Home_WiFi",
    val wanIpAddress: String = "10.120.45.12",
    val isInternetConnected: Boolean = true,
    val uptimeSeconds: Long = 86400,
    val firmwareVersion: String = "v2.0.4-ACT",
    val macAddress: String = "00:11:22:33:44:55"
) {
    val formattedUptime: String
        get() {
            val hours = uptimeSeconds / 3600
            val minutes = (uptimeSeconds % 3600) / 60
            val seconds = uptimeSeconds % 60
            return "${hours}h ${minutes}m ${seconds}s"
        }
}

/**
 * Connected DHCP / Wi-Fi device details.
 */
data class ConnectedDevice(
    val ipAddress: String,
    val macAddress: String,
    val deviceName: String = "Unknown Device",
    val connectionType: DeviceConnectionType = DeviceConnectionType.WIFI_5G,
    val signalStrengthDbm: Int = -60,
    val isOnline: Boolean = true,
    val leaseTimeRemaining: String = "23h 45m"
)

enum class DeviceConnectionType(val label: String) {
    WIFI_2G("2.4 GHz Wi-Fi"),
    WIFI_5G("5 GHz Wi-Fi"),
    ETHERNET("Ethernet LAN"),
    UNKNOWN("Unknown")
}

/**
 * Dual-band Wi-Fi configuration model.
 */
data class WifiSettings(
    val ssid2G: String = "",
    val password2G: String = "",
    val is2GEnabled: Boolean = true,
    val ssid5G: String = "",
    val password5G: String = "",
    val is5GEnabled: Boolean = true,
    val securityType: String = "WPA2-PSK (AES)"
)

/**
 * Encrypted SharedPreferences router credentials model.
 */
data class LoginCredentials(
    val username: String,
    val password: String,
    val gatewayIp: String = "192.168.0.1"
)

/**
 * Network state detected via Android Network APIs.
 */
data class NetworkState(
    val isWifiConnected: Boolean = false,
    val gatewayIp: String? = null,
    val connectedSsid: String? = null,
    val bssid: String? = null,
    val localIpAddress: String? = null
)
