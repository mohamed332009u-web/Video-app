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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : Activity() {

    private lateinit var webView: WebView

    data class MediaResult(val downloadUrl: String, val title: String)

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

    // واجهة التفاعل بين JavaScript والكود البرمجي (Kotlin)
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
                        val cleanTitle = result.title.replace(Regex("[^a-zA-Z0-9_\\-\\s]"), "_").take(40)
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
                        updateStatus(
                            "خطأ: ${e.localizedMessage ?: "فشل الاتصال بالشبكة"}",
                            0,
                            false,
                            isError = true
                        )
                    }
                }
            }.start()
        }
    }

    // دالة استخراج روابط التحميل من الخدمات المختلفة
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

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.use { it.readText() }
                val json = JSONObject(responseText)

                if (json.optInt("code") == 0) {
                    val data = json.getJSONObject("data")
                    val title = data.optString("title", "tiktok_video")

                    val downloadUrl = if (mode == "audio") {
                        data.optString("music", "")
                    } else {
                        // الرابط المباشر للفيديو بدون علامة مائية
                        data.optString("play", "")
                    }

                    if (downloadUrl.isNotEmpty()) {
                        return MediaResult(downloadUrl, title)
                    }
                }
            }
        }

        // في حال لم يكن تيك توك أو فشل الاستخراج
        return null
    }

    // دالة بدء التنزيل باستخدام DownloadManager في أندرويد
    private fun downloadFileWithManager(downloadUrl: String, filename: String, title: String) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle(title)
            setDescription("جاري تنزيل الملف عبر Vidora...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Vidora/$filename")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    // دالة إرسال التحديثات لصفحة الويب (JavaScript)
    private fun updateStatus(message: String, progress: Int, isDone: Boolean, isError: Boolean = false) {
        val jsCommand = "javascript:if(typeof updateDownloadStatus === 'function') { updateDownloadStatus('$message', $progress, $isDone, $isError); }"
        webView.evaluateJavascript(jsCommand, null)
    }
}
