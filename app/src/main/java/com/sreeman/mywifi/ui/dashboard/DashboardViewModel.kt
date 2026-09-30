package com.sreeman.mywifi.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreeman.mywifi.data.model.ConnectedDevice
import com.sreeman.mywifi.data.model.RouterStatus
import com.sreeman.mywifi.data.model.WifiSettings
import com.sreeman.mywifi.data.repository.RouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isRefreshing: Boolean = false,
    val routerStatus: RouterStatus = RouterStatus(),
    val connectedDevices: List<ConnectedDevice> = emptyList(),
    val wifiSettings: WifiSettings = WifiSettings(
        ssid2G = "ACT_Fibernet_2.4G",
        password2G = "Act@Home123",
        ssid5G = "ACT_Fibernet_5G",
        password5G = "Act@Home123"
    ),
    val isRebooting: Boolean = false,
    val showRebootDialog: Boolean = false,
    val isUpdatingWifi: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RouterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshDashboardData()
    }

    fun refreshDashboardData() {
        _uiState.update { it.copy(isRefreshing = true) }

        viewModelScope.launch {
            val statusResult = repository.fetchDashboardStatus()
            val devicesResult = repository.fetchConnectedDevices()

            _uiState.update { currentState ->
                currentState.copy(
                    isRefreshing = false,
                    routerStatus = statusResult.getOrDefault(currentState.routerStatus),
                    connectedDevices = devicesResult.getOrDefault(currentState.connectedDevices)
                )
            }
        }
    }

    fun showRebootConfirmation(show: Boolean) {
        _uiState.update { it.copy(showRebootDialog = show) }
    }

    fun confirmRebootRouter() {
        _uiState.update { it.copy(showRebootDialog = false, isRebooting = true) }

        viewModelScope.launch {
            val result = repository.rebootRouter()
            _uiState.update { currentState ->
                currentState.copy(
                    isRebooting = false,
                    snackbarMessage = if (result.isSuccess) {
                        "Reboot command sent to router successfully. Please wait 60s."
                    } else {
                        "Failed to send reboot command to router."
                    }
                )
            }
        }
    }

    fun updateWifiSettings(
        ssid2G: String,
        pass2G: String,
        ssid5G: String,
        pass5G: String
    ) {
        _uiState.update { it.copy(isUpdatingWifi = true) }

        viewModelScope.launch {
            val newSettings = WifiSettings(
                ssid2G = ssid2G,
                password2G = pass2G,
                ssid5G = ssid5G,
                password5G = pass5G
            )

            val result = repository.updateWifiSettings(newSettings)
            _uiState.update { currentState ->
                currentState.copy(
                    isUpdatingWifi = false,
                    wifiSettings = if (result.isSuccess) newSettings else currentState.wifiSettings,
                    snackbarMessage = if (result.isSuccess) {
                        "Wi-Fi settings updated successfully!"
                    } else {
                        "Failed to update Wi-Fi settings."
                    }
                )
            }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun logout() {
        repository.logout()
    }
}
