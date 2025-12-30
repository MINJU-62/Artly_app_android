package com.oddlemon.artly.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

// 네트워크 연결되어 있는지 확인하는 class
class NetworkMonitor(context: Context) {

    // 인스턴스 획득
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // 네트워크 상태가 변할 때 실행될 함수 (콜백)
    var onNetworkChanged: ((Boolean) -> Unit)? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // 연결됨
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            onNetworkChanged?.invoke(true)
        }

        // 끊김
        override fun onLost(network: Network) {
            super.onLost(network)
            onNetworkChanged?.invoke(false)
        }
    }

    // 감지 시작
    fun startMonitoring() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    // 감지 종료
    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // 이미 해제된 경우 무시
        }
    }

    // 현재 연결 상태 확인 (앱 켤 때 확인용)
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}