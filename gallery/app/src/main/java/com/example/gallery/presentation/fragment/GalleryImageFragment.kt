package com.example.gallery.presentation.fragment

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.gallery.R
import com.example.gallery.domain.ImageProcessorRepository
import com.example.gallery.domain.RequestBodyBuilder
import com.example.gallery.presentation.GalleryItem
import com.example.gallery.presentation.viewmodel.ApiCallStatus
import com.example.gallery.presentation.viewmodel.ApiCallStatus.*
import com.example.gallery.presentation.viewmodel.GalleryImageViewModel
import com.example.gallery.presentation.viewmodel.GalleryImageViewModelFactory

class GalleryImageFragment : Fragment(R.layout.gallery_image_fragment) {

    private val args: GalleryImageFragmentArgs by navArgs()
    private lateinit var viewModel: GalleryImageViewModel
    private lateinit var uploadReqBtn: Button
    private lateinit var uploadBtn: Button
    private lateinit var processBtn: Button
    private lateinit var statusString: TextView
    private lateinit var imageView: ImageView
    private lateinit var statusProgressbar: ProgressBar
    private lateinit var rotateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            /* owner = */ this,
            /* factory = */ GalleryImageViewModelFactory(
                requestBodyBuilderFactory = { RequestBodyBuilder(requireContext().applicationContext.contentResolver) },
                imageProcessorRepositoryFactory = ::ImageProcessorRepository
            )
        ).get()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = args.galleryItem
        view.setupViews()
        observeState()
        setClickListeners(item)

        Glide.with(requireContext())
            .load(item.uri)
            .error(R.drawable.ic_launcher_foreground) // placeholder
            .into(imageView)
    }

    private fun setClickListeners(item: GalleryItem) {
//        uploadReqBtn.setOnClickListener {
//
//            viewModel.uploadReq(args.option)
//        }

//        uploadBtn.setOnClickListener {
//            viewModel.uploadImage(item.uri)
//        }

        processBtn.setOnClickListener {
            context?.let { it1 -> viewModel.getProcessedImageUrl(it1) }
        }
        rotateButton.setOnClickListener {
            context?.let { it1 -> viewModel.rotate_image(this.imageView , ROTATE, it1) }
        }

    }



    private fun observeState() {
        viewModel.statusString.observe(viewLifecycleOwner) {
            when (it) {
                WAITING_FOR_RESPONSE -> enableControl(false)
                else -> {
                    enableControl(true)
                    statusString.text = it.toUserMessage()
                }
            }
        }

        viewModel.imageUrl.observe(viewLifecycleOwner) {
            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = IMAGE_PROGRESSBAR_STROKE_WITH
            circularProgressDrawable.centerRadius = IMAGE_PROGRESSBAR_CENTER_RADIUS
            circularProgressDrawable.start()

            Glide.with(requireContext())
                .load(it)
                .placeholder(circularProgressDrawable)
                .error(R.drawable.ic_launcher_background) // placeholder
                .into(imageView)
        }
    }

    private fun View.setupViews() {
//        uploadReqBtn = findViewById(R.id.upload_req)
//        uploadBtn = findViewById(R.id.upload)
        processBtn = findViewById(R.id.processed_image_url_req_btn)
        statusString = findViewById(R.id.req_status)
        imageView = findViewById(R.id.gallery_image_view)
        statusProgressbar = findViewById(R.id.status_progressbar)
        rotateButton = findViewById(R.id.btn_rotate)
    }

    private fun ApiCallStatus.toUserMessage() = when (this) {
        URL_FOR_UPLOAD_FETCHED -> getString(R.string.url_fetched)
        ERROR_URL_FETCH -> getString(R.string.fetch_error)

        IMAGE_UPLOADED -> getString(R.string.image_uploaded)
        ERROR_IMAGE_UPLOAD -> getString(R.string.upload_error)

        STILL_PROCESSING_IMAGE -> getString(R.string.still_processing)
        PROCESSING_IMAGE_FAILURE -> getString(R.string.cant_process_image)
        IMAGE_READY -> getString(R.string.image_ready)
        ERROR_PROCESSING_IMAGE -> getString(R.string.process_error)

        ERROR_NO_URL -> getString(R.string.no_url_message)
        ERROR_NO_JOB_ID -> getString(R.string.no_job_id_message)

        WAITING_FOR_RESPONSE -> error("invalid state at this point")
    }

    private fun enableControl(enable: Boolean) {
        uploadReqBtn.isEnabled = enable
        uploadBtn.isEnabled = enable
        processBtn.isEnabled = enable
        statusString.visibility = if (enable) View.VISIBLE else View.INVISIBLE
        statusProgressbar.visibility = if (enable) View.INVISIBLE else View.VISIBLE
    }

    companion object {
        @Suppress("unused")
        const val TAG = "GalleryImageFragment" // for debug needs
        const val IMAGE_PROGRESSBAR_STROKE_WITH = 5f
        const val IMAGE_PROGRESSBAR_CENTER_RADIUS = 30f
        const val ROTATE = 90f
    }
}
