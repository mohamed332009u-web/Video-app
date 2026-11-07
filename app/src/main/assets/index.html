package com.vidora.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : Activity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            webViewClient = WebViewClient()
            addJavascriptInterface(WebAppInterface(this@MainActivity), "AndroidApp")
            loadUrl("file:///android_asset/index.html")
        }

        setContentView(webView)
    }

    inner class WebAppInterface(private val context: Context) {

        @JavascriptInterface
        fun startDownload(rawUrl: String, mode: String) {
            var url = rawUrl.trim()
            // معالجة أي شرطة زائدة في بداية الرابط
            if (url.startsWith("/")) {
                url = url.substring(1).trim()
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }

            runOnUiThread {
                updateStatus("جاري فك الرابط والاتصال بالمحتوى...", 25, false)
            }

            Thread {
                try {
                    val result = resolveDownloadUrl(url, mode)
                    if (result != null) {
                        runOnUiThread {
                            updateStatus("تم تجهيز الرابط! جاري الحفظ في التنزيلات...", 80, false)
                        }
                        val ext = if (mode == "audio") "mp3" else "mp4"
                        val cleanTitle = result.title.replace(Regex("[^a-zA-Z0-9أ-ي_\\-]"), "_").take(40)
                        val filename = "Vidora_${cleanTitle}_${System.currentTimeMillis()}.$ext"

                        downloadFileWithManager(result.downloadUrl, filename, result.title)

                        runOnUiThread {
                            updateStatus("تم بدء التنزيل بنجاح في مجلد Downloads/Vidora!", 100, true)
                            Toast.makeText(context, "بدأ التنزيل في الإشعارات ومجلد Download", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            updateStatus("تعذر استخراج الفيديو، تأكد من صحة الرابط وأنه عام.", 0, false, isError = true)
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        updateStatus("خطأ: ${e.localizedMessage ?: "فشل الاتصال بالشبكة"}", 0, false, isError = true)
                    }
                }
            }.start()
        }
    }

    data class MediaResult(val downloadUrl: String, val title: String)

    private fun resolveDownloadUrl(targetUrl: String, mode: String): MediaResult? {
        // 1. محرك مخصص لروابط تيك توك (TikTok)
        if (targetUrl.contains("tiktok.com", ignoreCase = true)) {
            val endpoint = "https://www.tikwm.com/api/?url=" + URLEncoder.encode(targetUrl, "UTF-8")
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                if (json.optInt("code", -1) == 0) {
                    val data = json.getJSONObject("data")
                    val title = data.optString("title", "tiktok_video")
                    val dlUrl = if (mode == "audio") data.optString("music", "") else data.optString("play", "")
                    if (dlUrl.isNotEmpty()) return MediaResult(dlUrl, title)
                }
            }
        }

        // 2. محرك عام لمنصات الفيديو الأخرى
        val fallbackApi = "https://api.vkrdownloader.com/server?vkr=" + URLEncoder.encode(targetUrl, "UTF-8")
        try {
            val conn = (URL(fallbackApi).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val data = json.optJSONObject("data")
                if (data != null) {
                    val title = data.optString("title", "video")
                    val downloads = data.optJSONArray("downloads")
                    if (downloads != null && downloads.length() > 0) {
                        for (i in 0 until downloads.length()) {
                            val item = downloads.getJSONObject(i)
                            val format = item.optString("format_id", "")
                            val url = item.optString("url", "")
                            if (mode == "audio" && (format.contains("audio") || format.contains("mp3"))) {
                                return MediaResult(url, title)
                            }
                            if (mode == "video" && url.isNotEmpty()) {
                                return MediaResult(url, title)
                            }
                        }
                        val firstUrl = downloads.getJSONObject(0).optString("url", "")
                        if (firstUrl.isNotEmpty()) return MediaResult(firstUrl, title)
                    }
                }
            }
        } catch (_: Exception) {}

        return null
    }

    private fun downloadFileWithManager(fileUrl: String, filename: String, title: String) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(fileUrl)).apply {
            setTitle("Vidora: $title")
            setDescription("جاري تنزيل الملف...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Vidora/$filename")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        downloadManager.enqueue(request)
    }

    private fun updateStatus(msg: String, progress: Int, isDone: Boolean, isError: Boolean = false) {
        val escaped = JSONObject.quote(msg)
        webView.evaluateJavascript("handleNativeStatus($escaped, $progress, $isDone, $isError);", null)
    }
}
