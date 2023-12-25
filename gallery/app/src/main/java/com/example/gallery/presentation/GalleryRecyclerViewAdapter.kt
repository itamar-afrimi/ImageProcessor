package com.example.gallery.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallery.R

class GalleryRecyclerViewAdapter(val onItemClick: (GalleryItem) -> Unit):
    ListAdapter<GalleryItem, GalleryRecyclerViewAdapter.GalleryRecyclerViewHolder>(GalleryItemDiffCallBack()) {

    inner class GalleryRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val galleryImageView: ImageView = itemView.findViewById(R.id.gallery_item_image)

        fun onBind(item: GalleryItem) {
            Glide.with(galleryImageView.context)
                .load(item.uri)
                .into(galleryImageView)
            galleryImageView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryRecyclerViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
        return GalleryRecyclerViewHolder(v)
    }

    override fun onBindViewHolder(holder: GalleryRecyclerViewHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item)
    }

    class GalleryItemDiffCallBack : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem == newItem
        }
    }
}
