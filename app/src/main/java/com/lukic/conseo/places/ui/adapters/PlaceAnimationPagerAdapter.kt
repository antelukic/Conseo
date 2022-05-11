package com.lukic.conseo.places.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ItemPlaceAnimationsBinding

class PlaceAnimationPagerAdapter(
    private val animations: List<String>
): RecyclerView.Adapter<PlaceAnimationPagerAdapter.ServicesAnimationsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServicesAnimationsViewHolder =
        ServicesAnimationsViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_place_animations,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ServicesAnimationsViewHolder, position: Int) {
        holder.animation.setAnimation(animations[position])
    }

    override fun getItemCount(): Int = animations.size

    inner class ServicesAnimationsViewHolder(binding: ItemPlaceAnimationsBinding): RecyclerView.ViewHolder(binding.root){
        val animation = binding.ItemServiceAnimationsLottieAnimation
    }
}