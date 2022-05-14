package com.lukic.conseo.places.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.conseo.database.entity.PlaceEntity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ItemSinglePlaceBinding
import com.lukic.conseo.utils.OnItemClickListener

class PlacesRecyclerAdapter(
    private val singlePlaces: List<PlaceEntity>,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<PlacesRecyclerAdapter.SingleServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleServiceViewHolder =
        SingleServiceViewHolder(
            DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_single_place,
            parent,
                false))

    override fun onBindViewHolder(holder: SingleServiceViewHolder, position: Int) {
        holder.name.text = singlePlaces[position].name
        holder.location.text = singlePlaces[position].location
        Glide.with(holder.itemView.context).load(singlePlaces[position].image).into(holder.image)
        holder.cardView.setBackgroundResource(R.drawable.card_view_border)

        holder.cardView.setOnClickListener {
            listener.onClick(singlePlaces[position])
        }
    }

    override fun getItemCount(): Int = singlePlaces.size

    inner class SingleServiceViewHolder(binding: ItemSinglePlaceBinding): RecyclerView.ViewHolder(binding.root){
        val name = binding.ItemSinglePlaceName
        val location = binding.ItemSinglePlaceLocation
        val image = binding.ItemSinglePlaceImage
        val cardView = binding.ItemSinglePlaceCardView
    }
}