package com.oddlemon.artly.util

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.widget.Toast

// 다운로드를 전담하는 전문가 클래스
class WebViewDownloadListener(private val context: Context) : DownloadListener {

    override fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long
    ) {
        // Base64 이미지 (React QR 등)
        if (url.startsWith("data:")) {
            saveBase64Image(url)
            return
        }

        // 일반 파일 다운로드
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)

            request.setMimeType(mimetype)
            request.addRequestHeader("User-Agent", userAgent)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(context, "다운로드 시작", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "다운로드 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // Base64 저장 로직 (내부 함수)
    private fun saveBase64Image(url: String) {
        try {
            val pureBase64 = url.substringAfter(",")
            val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            val filename = "ARTLY_${System.currentTimeMillis()}.png"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(imageBytes)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)

                Toast.makeText(context, "갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "이미지 저장 실패", Toast.LENGTH_SHORT).show()
        }
    }
}