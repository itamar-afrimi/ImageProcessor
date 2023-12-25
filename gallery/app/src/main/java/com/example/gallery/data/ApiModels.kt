package com.example.gallery.data

import com.example.gallery.domain.ImageUrl
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


sealed class ProcessedImageUrlResults {
    data class Processing(
        val status: ImageUrlProcessingStatus
    ) : ProcessedImageUrlResults()

    data class Ready(
        val url: ImageUrl
    ) : ProcessedImageUrlResults()
}

data class UploadReq(
    val jobId: String,
    val url: String
)

@JsonClass(generateAdapter = true)
data class UploadReqResponse(
    @Json(name = "jobId")
    val jobId: String,
    @Json(name = "url")
    val url: String
)

@JsonClass(generateAdapter = true)
data class FileNameJson(
    @Json(name = "fileName")
    val fileName: String,
    @Json(name = "jobRequest")
    val jobRequest : String
)

@JsonClass(generateAdapter = true)
data class ProcessedImageReqResponse(
    @Json(name = "status")
    val status: ImageUrlProcessingStatus?,
    @Json(name = "url")
    val url: String?
)

@JsonClass(generateAdapter = false)
enum class ImageUrlProcessingStatus {
    @Json(name = "failure")
    Failure,
    @Json(name = "success")
    Success,
    @Json(name = "created")
    Created,
    @Json(name = "processing")
    Processing;
}
