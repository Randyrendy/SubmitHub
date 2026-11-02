package com.submithub.app.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.util.concurrent.TimeUnit

/**
 * Minimal client for Supabase Storage's REST API.
 */
object SupabaseStorage {

    private const val SUPABASE_URL = "https://liypsaxnxdyeeappgqoe.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_UPTm7pvDJ2bbvkrgO5VrVw_elWXVoCP"
    private const val BUCKET = "submissions"


    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        path: String,
        mimeType: String,
        onProgress: ((percent: Int) -> Unit)? = null
    ): String {
        return withContext(Dispatchers.IO) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Could not read the selected file")

            val body = ProgressRequestBody(bytes, mimeType.toMediaTypeOrNull(), onProgress)
            val uploadUrl = "$SUPABASE_URL/storage/v1/object/$BUCKET/$path"

            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .addHeader("Content-Type", mimeType)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "no details"
                    throw IllegalStateException("Upload failed (${response.code}): $errorBody")
                }
            }

            "$SUPABASE_URL/storage/v1/object/public/$BUCKET/$path"
        }
    }

    private class ProgressRequestBody(
        private val bytes: ByteArray,
        private val mediaType: MediaType?,
        private val onProgress: ((Int) -> Unit)?
    ) : RequestBody() {

        override fun contentType(): MediaType? = mediaType

        override fun contentLength(): Long = bytes.size.toLong()

        override fun writeTo(sink: BufferedSink) {
            var offset = 0
            var lastPercent = -1
            val total = bytes.size

            while (offset < total) {
                val length = minOf(8192, total - offset)
                sink.write(bytes, offset, length)
                offset += length

                if (onProgress != null && total > 0) {
                    val percent = (offset * 100L / total).toInt()
                    if (percent != lastPercent) {
                        lastPercent = percent
                        onProgress.invoke(percent)
                    }
                }
            }
        }
    }
}