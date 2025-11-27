package com.oddlemon.artly.util

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class WebChromeClient(private val activity: ComponentActivity) : WebChromeClient() {

    private var mPermissionRequest: PermissionRequest? = null
    private var mGeolocationCallback: GeolocationPermissions.Callback? = null
    private var mGeolocationOrigin: String? = null

    companion object {
        private const val TAG = "WebChromeClient"
        private const val RESOURCE_GEOLOCATION = "android.webkit.resource.GEOLOCATION"
    }

    private val permissionResultLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions: Map<String, Boolean> ->

            val requestedWebResources = mPermissionRequest?.resources ?: emptyArray()
            val grantedWebResources = mutableListOf<String>()

            // 카메라 권한 결과 확인
            if (permissions[Manifest.permission.CAMERA] == true &&
                requestedWebResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                grantedWebResources.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
            }

            // 위치 권한 결과 확인
            val hasLocationPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (hasLocationPermission && requestedWebResources.contains(RESOURCE_GEOLOCATION)) {
                grantedWebResources.add(RESOURCE_GEOLOCATION)
            }

            // 허용된 권한만 웹페이지에 전달
            if (grantedWebResources.isNotEmpty()) {
                mPermissionRequest?.grant(grantedWebResources.toTypedArray())
            } else {
                mPermissionRequest?.deny()
            }
            mPermissionRequest = null
        }

    private val geolocationPermissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions: Map<String, Boolean> ->

            val hasLocationPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            mGeolocationCallback?.invoke(mGeolocationOrigin, hasLocationPermission, false)

            mGeolocationCallback = null
            mGeolocationOrigin = null
        }

    override fun onPermissionRequest(request: PermissionRequest?) {
        val requestedResources = request?.resources ?: emptyArray()
        val permissionsToRequest = mutableListOf<String>()

        // 카메라 요청이 있는지 확인
        if (requestedResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        // 위치 정보 요청이 있는지 확인
        if (requestedResources.contains(RESOURCE_GEOLOCATION)) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // 요청할 권한이 있다면
        if (permissionsToRequest.isNotEmpty()) {
            this.mPermissionRequest = request
            permissionResultLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            request?.deny()
        }
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        // 이미 권한이 있는지 확인
        val hasFineLocation = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            callback?.invoke(origin, true, false)
        } else {
            mGeolocationCallback = callback
            mGeolocationOrigin = origin
            geolocationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
}