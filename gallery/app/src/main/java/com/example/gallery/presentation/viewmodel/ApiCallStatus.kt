package com.example.gallery.presentation.viewmodel

enum class ApiCallStatus {
    // General status
    WAITING_FOR_RESPONSE,

    // Upload req status
    URL_FOR_UPLOAD_FETCHED,
    ERROR_URL_FETCH,

    // Upload image status
    IMAGE_UPLOADED,
    ERROR_IMAGE_UPLOAD,
    ERROR_NO_URL,

    // Process image status
    STILL_PROCESSING_IMAGE,
    PROCESSING_IMAGE_FAILURE,
    IMAGE_READY,
    ERROR_PROCESSING_IMAGE,
    ERROR_NO_JOB_ID;
}
