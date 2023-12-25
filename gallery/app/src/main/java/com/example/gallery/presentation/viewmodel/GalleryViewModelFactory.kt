package com.example.gallery.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gallery.domain.GalleryItemsProvider

class GalleryViewModelFactory(
    private val galleryItemsProviderFactory: () -> GalleryItemsProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GalleryViewModel(galleryItemsProviderFactory()) as T
    }
}
