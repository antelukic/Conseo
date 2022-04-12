package com.lukic.conseo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.conseo.database.entity.UserEntity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ItemAllChatsBinding
import com.lukic.conseo.utils.OnItemClickListener

class AllChatsRecyclerAdapter(private val chats: List<UserEntity>, private val listener: OnItemClickListener)
    :RecyclerView.Adapter<AllChatsRecyclerAdapter.AllChatsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AllChatsViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_all_chats,
            parent,
            false,
            null
        )
    )

    override fun onBindViewHolder(holder: AllChatsViewHolder, position: Int) {
        holder.userName.text = chats[position].name
        Glide.with(holder.itemView.context).load(chats[position].image).into(holder.userPhoto)
        holder.cardView.setOnClickListener {
            listener.onClick(item = chats[position])
        }
    }

    override fun getItemCount(): Int = chats.size

    inner class AllChatsViewHolder(val binding: ItemAllChatsBinding): RecyclerView.ViewHolder(binding.root){
        val userPhoto = binding.ItemAllChatsUserPhoto
        val userName = binding.ItemAllChatsUserName
        val cardView = binding.ItemAllChatsCardView
    }
}