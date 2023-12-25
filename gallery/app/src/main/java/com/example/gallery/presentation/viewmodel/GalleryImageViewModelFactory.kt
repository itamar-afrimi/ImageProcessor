package com.example.gallery.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gallery.data.ProcessImageApi
import com.example.gallery.domain.ImageProcessorRepository
import com.example.gallery.domain.RequestBodyBuilder
import kotlinx.coroutines.Dispatchers

class GalleryImageViewModelFactory(
    private val requestBodyBuilderFactory: () -> RequestBodyBuilder,
    private val imageProcessorRepositoryFactory: (ProcessImageApi) -> ImageProcessorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val processImageApi = ProcessImageApi.instance
        return GalleryImageViewModel(
            requestBodyBuilder = requestBodyBuilderFactory.invoke(),
            imageProcessorRepository = imageProcessorRepositoryFactory.invoke(processImageApi),
            dispatcher = Dispatchers.Main
        ) as T
    }
}
