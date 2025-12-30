package com.oddlemon.artly.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class WebChromeClient(private val activity: ComponentActivity) : WebChromeClient() {

    private var mPermissionRequest: PermissionRequest? = null
    private var mGeolocationCallback: GeolocationPermissions.Callback? = null
    private var mGeolocationOrigin: String? = null
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val RESOURCE_GEOLOCATION = "android.webkit.resource.GEOLOCATION"
    }

    private fun copyToCache(uri: Uri): Uri? {
        try {
            val contentResolver = activity.contentResolver
            val fileName = "upload_${System.currentTimeMillis()}.jpg"
            val cacheFile = File(activity.cacheDir, fileName)

            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(cacheFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return Uri.fromFile(cacheFile)
        } catch (e: Exception) {
            return null
        }
    }

    private val fileChooserLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()

    ) { result ->
        if (fileUploadCallback == null) return@registerForActivityResult

        var results: Array<Uri>? = null

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    val uris = ArrayList<Uri>()
                    for (i in 0 until count) {
                        val originUri = data.clipData!!.getItemAt(i).uri
                        copyToCache(originUri)?.let { uris.add(it) }
                    }
                    results = uris.toTypedArray()
                } else if (data.data != null) {
                    val originUri = data.data!!
                    copyToCache(originUri)?.let { results = arrayOf(it) }
                }
            }
        }

        fileUploadCallback?.onReceiveValue(results)
        fileUploadCallback = null
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        if (this.fileUploadCallback != null) {
            this.fileUploadCallback?.onReceiveValue(null)
            this.fileUploadCallback = null
        }
        this.fileUploadCallback = filePathCallback

        val intent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        try {
            fileChooserLauncher.launch(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            try {
                fileChooserLauncher.launch(fallbackIntent)
            } catch (e2: Exception) {
                this.fileUploadCallback?.onReceiveValue(null)
                return false
            }
        }
        return true
    }

    private val permissionResultLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions: Map<String, Boolean> ->
            val requestedWebResources = mPermissionRequest?.resources ?: emptyArray()
            val grantedWebResources = mutableListOf<String>()

            if (permissions[Manifest.permission.CAMERA] == true &&
                requestedWebResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                grantedWebResources.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
            }

            val hasLocationPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (hasLocationPermission && requestedWebResources.contains(RESOURCE_GEOLOCATION)) {
                grantedWebResources.add(RESOURCE_GEOLOCATION)
            }

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

        if (requestedResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (requestedResources.contains(RESOURCE_GEOLOCATION)) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

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