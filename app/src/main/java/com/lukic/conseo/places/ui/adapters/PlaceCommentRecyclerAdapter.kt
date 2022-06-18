package com.lukic.conseo.places.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.conseo.database.entity.CommentsEntity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ItemPlaceCommentsBinding

class PlaceCommentRecyclerAdapter(
    private val comments: List<CommentsEntity>
): RecyclerView.Adapter<PlaceCommentRecyclerAdapter.PlaceCommentsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceCommentsViewHolder=
        PlaceCommentsViewHolder(
            ItemPlaceCommentsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PlaceCommentsViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(comments[position].image).error(R.mipmap.ic_launcher).into(holder.image)
        holder.title.text = comments[position].title
        holder.body.text = comments[position].body
    }

    override fun getItemCount(): Int = comments.size

    inner class PlaceCommentsViewHolder(binding: ItemPlaceCommentsBinding): RecyclerView.ViewHolder(binding.root){
        val image = binding.ItemPlaceCommentsImage
        val title = binding.ItemPlaceCommentsTitle
        val body = binding.ItemPlaceCommentsBody
    }
}