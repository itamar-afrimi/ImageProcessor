package com.example.gallery.presentation

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Model class that represents the gallery app.
 * [galleryItems] - list of all the gallery items to be presented
 */
data class GalleryModel(
    val galleryItems: List<GalleryItem>,
)

/**
 * The class representing a gallery item.
 */
@Parcelize
data class GalleryItem(val uri: Uri): Parcelable
