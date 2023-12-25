package com.example.gallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gallery.domain.GalleryItemsProvider
import com.example.gallery.presentation.GalleryModel
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val galleryItemsProvider: GalleryItemsProvider
) : ViewModel() {
    private val galleryModel = MutableLiveData(GalleryModel(listOf()))

    fun getGalleryModel(): LiveData<GalleryModel> = galleryModel

    fun refreshGalleryItems() {
        viewModelScope.launch {
            updateGalleryModel { copy(galleryItems = galleryItemsProvider.getAllGalleryItems()) }
        }
    }

    private inline fun updateGalleryModel(transform: GalleryModel.() -> GalleryModel) {
        val currModel = checkNotNull(galleryModel.value) { "Was not expected to be null." }
        galleryModel.value = transform(currModel)
    }
}
