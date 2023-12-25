package com.example.gallery.presentation.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gallery.R

class MenuFragment : Fragment(R.layout.menu_fragment) {

    // buttons
    private lateinit var edgesButton: Button
    private lateinit var blurButton: Button
    private lateinit var blackAndWhiteButton: Button
    private lateinit var negativeButton: Button

//    private var styleMsg = "Style images"
    private var blackAndWhiteMsg = "black and white"
    private var blurMsg = "blur image"
    private var edgesMsg = "edges"
    private var negativeMsg = "negative"

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.edgesButton = view.findViewById(R.id.edges_btn)
        this.blurButton = view.findViewById(R.id.blur_btn)
        this.blackAndWhiteButton = view.findViewById(R.id.bw_btn)
        this.negativeButton = view.findViewById(R.id.negative_btn)
        setClickListeners()
    }

    private fun setClickListeners() {

        edgesButton.setOnClickListener {
            val action = MenuFragmentDirections.actionMenuFragmentToGalleryFragment(edgesMsg)
            findNavController().navigate(action)
        }

        blurButton.setOnClickListener {
            val action = MenuFragmentDirections.actionMenuFragmentToGalleryFragment(blurMsg)
            findNavController().navigate(action)
        }

        blackAndWhiteButton.setOnClickListener {
            val action = MenuFragmentDirections.actionMenuFragmentToGalleryFragment(blackAndWhiteMsg)
            findNavController().navigate(action)
        }
        negativeButton.setOnClickListener{
            val action = MenuFragmentDirections.actionMenuFragmentToGalleryFragment(negativeMsg)
            findNavController().navigate(action)
        }

    }
}