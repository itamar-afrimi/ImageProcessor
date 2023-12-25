package com.example.gallery.presentation.fragment

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.presentation.GalleryItem
import com.example.gallery.domain.GalleryItemsProvider
import com.example.gallery.presentation.GalleryRecyclerViewAdapter
import com.example.gallery.R
import com.example.gallery.presentation.viewmodel.GalleryViewModel
import com.example.gallery.presentation.viewmodel.GalleryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers

class GalleryFragment: Fragment(R.layout.gallery_fragment) {
    private lateinit var galleryViewModel: GalleryViewModel
    private val args: GalleryFragmentArgs by navArgs()
    private val readPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryViewModel.refreshGalleryItems()
        } else {
            Snackbar.make(
                requireView(),
                R.string.no_read_permission_message,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private val writePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryViewModel.refreshGalleryItems()
        } else {
            Snackbar.make(
                requireView(),
                R.string.no_write_permission_message,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryViewModel = ViewModelProvider(
            /* owner = */ this,
            /* factory = */ GalleryViewModelFactory(
                galleryItemsProviderFactory = {
                    GalleryItemsProvider(
                        contentResolver = requireContext().applicationContext.contentResolver,
                        coroutineDispatcher = Dispatchers.IO
                    )
                }
            )
        ).get()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.gallery_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), NUMBER_OF_COLUMNS)
        val adapter = GalleryRecyclerViewAdapter(::onItemClick)
        recyclerView.adapter = adapter
        galleryViewModel.getGalleryModel().observe(viewLifecycleOwner) {
            adapter.submitList(it.galleryItems)
        }
    }

    private fun onItemClick(galleryItem: GalleryItem) {
        val action = GalleryFragmentDirections
            .actionGalleryFragmentToGalleryImageFragment(galleryItem, args.optionType)
        findNavController().navigate(action)
    }

    override fun onStart() {
        super.onStart()
        requestPermissionsIfNeeded()
    }


    private fun requestPermissionsIfNeeded() {
        val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
        val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                readPermission
            ) == PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    writePermission
                ) == PERMISSION_GRANTED
            ) {
                // Both permissions are granted
                galleryViewModel.refreshGalleryItems()
                return
            } else {
                // Request write permission
                writePermissionLauncher.launch(writePermission)
                return
            }
        }

        // Request read permission
        readPermissionLauncher.launch(readPermission)
    }







    companion object {
        private const val NUMBER_OF_COLUMNS = 3
    }
}
