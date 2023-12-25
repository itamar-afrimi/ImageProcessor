package com.example.gallery.domain

import com.example.gallery.data.*
import okhttp3.RequestBody

open class ImageProcessorRepository(
    private val processImageApi: ProcessImageApi
) {

    open suspend fun uploadReq(fileName: String, jonRequest: String): Result<UploadReq> {
        val file = FileNameJson(fileName, jonRequest)
        print(file)
        val response = processImageApi.uploadReq(file)
        return when {
            response.isSuccessful && response.body() != null -> {
                val uploadReqResponse = response.body()!!
                Result.success(
                    UploadReq(
                        jobId = uploadReqResponse.jobId,
                        url = uploadReqResponse.url
                    )
                )
            }
            else -> Result.failure(UploadImageError("Error: Could not request upload"))
        }
    }

    open suspend fun uploadImage(url: String, body: RequestBody): Result<Unit> {
        val response = processImageApi.uploadImage(url, body)
        return when {
            response.isSuccessful -> Result.success(Unit)
            else -> Result.failure(UploadImageError("Error: Could not upload image"))
        }
    }

    suspend fun getProcessedImageUrl(jobId: String): Result<ProcessedImageUrlResults> {
        val response = processImageApi.getProcessedImageUrl(jobId = jobId)
        return when {
            response.isSuccessful && response.body() != null -> {
                val body: ProcessedImageReqResponse = response.body()!!
                print(body)
                when (body.url) {
                    null -> when (body.status) {
                        null -> Result.failure(UploadImageError("Error: can't get image processing status"))
                        else -> Result.success(ProcessedImageUrlResults.Processing(status = body.status))
                    }
                    else -> Result.success(ProcessedImageUrlResults.Ready(url = body.url))
                }
            }
            else -> {
                when {
                    response.code() == 404 -> Result.failure(UploadImageError("JobId does not exist"))
                    else -> Result.failure(UploadImageError("Error: can't get processed image url"))
                }
            }
        }
    }
}

class UploadImageError(error: String) : Exception(error)
