package com.example.gallery.presentation.viewmodel

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gallery.data.ImageUrlProcessingStatus
import com.example.gallery.data.ProcessedImageUrlResults
import com.example.gallery.domain.ImageProcessorRepository
import com.example.gallery.domain.ImageUrl
import com.example.gallery.domain.RequestBodyBuilder
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class GalleryImageViewModel(

    private val requestBodyBuilder: RequestBodyBuilder,
    private val imageProcessorRepository: ImageProcessorRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var test : Int = 1
    var url: ImageUrl? = null
    var jobId: String? = null


    private val _statusString = MutableLiveData<ApiCallStatus>()
    val statusString: LiveData<ApiCallStatus> = _statusString

    private val _imageUrl = MutableLiveData<ImageUrl>()
    val imageUrl: LiveData<ImageUrl> = _imageUrl


//    fun simpleReq():{
//        viewModelScope.launch {
//            withContext(dispatcher){
//
//            }
//        }
//    }
    fun uploadReq(jobRequest: String) : Unit {
         viewModelScope.launch {
             withContext(dispatcher) {
                 _statusString.postValue(ApiCallStatus.WAITING_FOR_RESPONSE)
                 imageProcessorRepository.uploadReq(FILE_NAME, jobRequest)
                     .onSuccess { uploadRequest ->
                         url = uploadRequest.url
                         jobId = uploadRequest.jobId
                         _statusString.postValue(ApiCallStatus.URL_FOR_UPLOAD_FETCHED)
                     }
                     .onFailure { _statusString.postValue(ApiCallStatus.ERROR_URL_FETCH) }
             }
         }
    }

    fun uploadImage(uri: Uri?) {
        viewModelScope.launch {
            withContext(dispatcher) {
                _statusString.postValue(ApiCallStatus.WAITING_FOR_RESPONSE)
                val requestBody = requestBodyBuilder.build(uri)
                    if (url != null) {

                        if (requestBody != null && test != -1) {
                            imageProcessorRepository.uploadImage(url!!, requestBody)
                                .onSuccess { _statusString.postValue(ApiCallStatus.IMAGE_UPLOADED) }
                                .onFailure { _statusString.postValue(ApiCallStatus.ERROR_IMAGE_UPLOAD) }
                        }
                    } else {
                        _statusString.postValue(ApiCallStatus.ERROR_NO_URL)
                    }
                }


        }
    }

    fun getProcessedImageUrl(context: Context) {
        viewModelScope.launch {
            _statusString.postValue(ApiCallStatus.WAITING_FOR_RESPONSE)
            if (jobId != null) {
                imageProcessorRepository.getProcessedImageUrl(jobId!!)
                    .onSuccess { processedImageUrlResults ->
                        when (processedImageUrlResults) {
                            is ProcessedImageUrlResults.Processing -> handleProcessing(
                                processedImageUrlResults
                            )
                            is ProcessedImageUrlResults.Ready -> handleResult(
                                processedImageUrlResults, context
                            )

                        }
                    }
                    .onFailure { _statusString.postValue(ApiCallStatus.ERROR_PROCESSING_IMAGE) }
            } else {
                _statusString.postValue(ApiCallStatus.ERROR_NO_JOB_ID)
            }
        }
    }

    private fun handleResult(processedImageUrlResults: ProcessedImageUrlResults.Ready, context: Context) {
        _imageUrl.postValue(processedImageUrlResults.url)
        _statusString.postValue(ApiCallStatus.IMAGE_READY)
        downloadImage(processedImageUrlResults.url, context)
    }

    private fun downloadImage(imageUrl: String, context: Context) {
        // Create a dialog to prompt the user for the desired file name
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter file name")

        // Create an input field in the dialog
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Add buttons to the dialog
        builder.setPositiveButton("OK") { _, _ ->
            val fileName = input.text.toString()

            // Create the download request
            val request = DownloadManager.Request(Uri.parse(imageUrl))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            request.setTitle(fileName)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.jpg")

            // Get the download service and enqueue the request
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            // Show a toast message to indicate that the download has started
            Toast.makeText(context, "Downloading $fileName.jpg", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        // Show the dialog to the user
        builder.show()
    }
    fun rotate_image(imageView: ImageView, degrees: Float, context: Context) {
        // Rotate the image
        val rotatedBitmap = rotateBitmap(imageView.drawable.toBitmap(), degrees)

        // Show the rotated image in a dialog
        val rotatedImageView = ImageView(context)
        rotatedImageView.setImageBitmap(rotatedBitmap)

        val dialog = AlertDialog.Builder(context)
            .setView(rotatedImageView)
            .setTitle("Save Rotated Image?")
            .setMessage("Do you want to save the rotated image to Downloads?")
            .setPositiveButton("Yes") { _, _ ->
                // Save the rotated image to Downloads directory
                val fileName = "rotated_image_${System.currentTimeMillis()}.jpg"
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadDir, fileName)
                FileOutputStream(file).use { rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
                MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
                Toast.makeText(context, "Image saved to Downloads directory", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .create()
        dialog.show()
    }



    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(-degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun handleProcessing(processedImageUrlResults: ProcessedImageUrlResults.Processing) {
        when (processedImageUrlResults.status) {
            ImageUrlProcessingStatus.Created ->
                _statusString.postValue(ApiCallStatus.STILL_PROCESSING_IMAGE)
            ImageUrlProcessingStatus.Failure ->
                _statusString.postValue(ApiCallStatus.PROCESSING_IMAGE_FAILURE)
            ImageUrlProcessingStatus.Processing ->
                _statusString.postValue(ApiCallStatus.STILL_PROCESSING_IMAGE)
            ImageUrlProcessingStatus.Success ->
                this.test = 2
//                handleResult(ProcessedImageUrlResults.Ready)
        }
    }


    companion object {
        @Suppress("unused")
        const val TAG = "GalleryImageViewModel" // for debug needs
        const val FILE_NAME = "MyFile.jpg" // can come from the user if needed
    }
}
