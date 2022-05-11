package com.lukic.conseo.chat.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.conseo.database.entity.MessageEntity
import com.google.firebase.auth.FirebaseAuth
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ItemReceiveLayoutBinding
import com.lukic.conseo.databinding.ItemSendLayoutBinding

class MessageRecyclerAdapter(private val messages: ArrayList<MessageEntity>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECIEVE = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == 1){
            return RecieveMessageViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_receive_layout,
                    parent,
                    false))
        } else{
            return SentMessageViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_send_layout,
                    parent,
                    false))
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.javaClass == SentMessageViewHolder::class.java){
            val viewHolder = holder as SentMessageViewHolder
            viewHolder.sentMessage.text = messages[position].message
        }
        if(holder.javaClass == RecieveMessageViewHolder::class.java){
            val viewHolder = holder as RecieveMessageViewHolder
            viewHolder.recieveMessage.text = messages[position].message
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messages[position]
        if(currentMessage.senderID == FirebaseAuth.getInstance().currentUser?.uid.toString()){
            return ITEM_SENT
        } else {
            return ITEM_RECIEVE
        }
    }

    inner class SentMessageViewHolder(binding: ItemSendLayoutBinding): RecyclerView.ViewHolder(binding.root){
        val sentMessage = binding.ItemSendSentMessage
    }

    inner class RecieveMessageViewHolder(binding: ItemReceiveLayoutBinding): RecyclerView.ViewHolder(binding.root){
        val recieveMessage = binding.ItemRecieveMessage
    }
}