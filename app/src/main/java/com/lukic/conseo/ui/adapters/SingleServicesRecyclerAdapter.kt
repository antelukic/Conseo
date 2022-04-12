package com.lukic.conseo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.conseo.database.entity.ServiceEntity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ItemSingleServiceBinding
import com.lukic.conseo.utils.OnItemClickListener

class SingleServicesRecyclerAdapter(
    private val singleServices: List<ServiceEntity>,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<SingleServicesRecyclerAdapter.SingleServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleServiceViewHolder =
        SingleServiceViewHolder(
            DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_single_service,
            parent,
                false))

    override fun onBindViewHolder(holder: SingleServiceViewHolder, position: Int) {
        holder.name.text = singleServices[position].name
        holder.location.text = singleServices[position].location
        Glide.with(holder.itemView.context).load(singleServices[position].image).into(holder.image)
        holder.cardView.setBackgroundResource(R.drawable.card_view_border)

        holder.cardView.setOnClickListener {
            listener.onClick(singleServices[position])
        }
    }

    override fun getItemCount(): Int = singleServices.size

    inner class SingleServiceViewHolder(binding: ItemSingleServiceBinding): RecyclerView.ViewHolder(binding.root){
        val name = binding.ItemSingleServiceName
        val location = binding.ItemSingleServiceLocation
        val image = binding.ItemSingleServiceImage
        val cardView = binding.ItemSingleServiceCardView
    }
}