package com.example.gallery.domain

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import com.example.gallery.presentation.GalleryItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GalleryItemsProvider(
    private val contentResolver: ContentResolver,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    suspend fun getAllGalleryItems(): List<GalleryItem> {
        return withContext(coroutineDispatcher) {
            contentResolver.queryForImages().use { cursor ->
                cursor?.toGalleryItems() ?: emptyList()
            }
        }
    }

    private fun ContentResolver.queryForImages(): Cursor? = query(
        /* uri = */ MediaStore.Files.getContentUri(EXTERNAL_VOLUME_NAME),
        /* projection = */ arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MIME_TYPE),
        /* selection = */ SELECTION,
        /* selectionArgs = */ null,
        /* sortOrder = */ DESCENDING_SORT_ORDER
    )

    private fun Cursor.toGalleryItems(): List<GalleryItem> {
        val idColumnIndex: Int = getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val userAssetsList = mutableListOf<GalleryItem>()
        while (moveToNext()) {
            val uri = ContentUris.withAppendedId(
                MediaStore.Files.getContentUri(EXTERNAL_VOLUME_NAME),
                getInt(idColumnIndex).toLong()
            )
            userAssetsList.add(GalleryItem(uri))
        }
        return userAssetsList
    }

    companion object {
        private const val SELECTION = "(lower(" + MediaStore.Files.FileColumns.MIME_TYPE + ") LIKE 'image/%' )"
        private const val EXTERNAL_VOLUME_NAME = "external"
        private const val DESCENDING_SORT_ORDER = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
    }
}
