package com.liara.smartass.static_objects

import android.util.Log
import com.google.gson.Gson
import com.liara.smartass.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream

const val TAG = "AppUpdater"
const val UPDATE_URL = "https://github.com/liperium/hdp-app/releases/latest/download/app.apk"
const val FILE_PATH = "hdp-liara/app.apk"

object AppUpdater {
    fun getLatestRelease(owner: String, repo: String): Release? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/releases/latest")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return null
            }
            val body = response.body?.string() ?: return null
            return Gson().fromJson(body, Release::class.java)
        }
    }

    // Function to download the file
    fun downloadFile(url: String, outputPath: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return
            }
            val inputStream = response.body?.byteStream() ?: return
            val outputStream = FileOutputStream(outputPath)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    // Usage example returns true if downloaded new file
    fun checkAndDownloadLatestRelease(owner: String, repo: String, outputPath: String): Boolean {
        val release = getLatestRelease(owner, repo)
        if (release != null && release.assets.isNotEmpty() && release.isDifferentFromCurrentApk()) {
            val downloadUrl = release.getItemFromName("app-release.apk")[0].browser_download_url
            downloadFile(downloadUrl, outputPath)
            return true
        }
        return false
    }

    data class Release(
        val assets: List<Asset>,
        val tag_name: String,
    ) {
        fun getItemFromName(fileName: String): List<Asset> {
            return assets.filter { it.name == fileName }
        }
        fun isDifferentFromCurrentApk(): Boolean {
            Log.d(
                TAG,
                "Current app version: ${BuildConfig.VERSION_NAME}, downloaded release $tag_name"
            )
            return normalizeVersion(tag_name.trim()) != normalizeVersion(BuildConfig.VERSION_NAME.trim())
        }

        private fun normalizeVersion(version: String): String {
            return version.trim().replace(Regex("[^\\w\\d]"), "").lowercase()
        }
    }

    data class Asset(
        val browser_download_url: String,
        val name: String
    )
}