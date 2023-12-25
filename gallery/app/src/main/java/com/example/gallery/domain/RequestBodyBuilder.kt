package com.example.gallery.domain

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

public open class RequestBodyBuilder(
     val contentResolver: ContentResolver
) {
    open fun build(uri: Uri?): RequestBody? {
        return object : RequestBody() {
            override fun contentType(): MediaType? =
                uri?.let { contentResolver.getType(it)?.toMediaTypeOrNull() }

            override fun writeTo(sink: BufferedSink) {
                if (uri != null) {
                    contentResolver.openInputStream(uri)?.source()?.use(sink::writeAll)
                }
            }

            override fun contentLength(): Long =
                uri?.let {
                    contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        val sizeColumnIndex: Int = cursor.getColumnIndex(OpenableColumns.SIZE)
                        cursor.moveToFirst()
                        cursor.getLong(sizeColumnIndex)
                    }
                } ?: super.contentLength()
        }
    }
}
