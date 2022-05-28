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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if(viewType == 1){
            ReceiveMessageViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_receive_layout,
                    parent,
                    false))
        } else{
            SentMessageViewHolder(
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
        if(holder.javaClass == ReceiveMessageViewHolder::class.java){
            val viewHolder = holder as ReceiveMessageViewHolder
            viewHolder.receiveMessage.text = messages[position].message
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messages[position]
        return if(currentMessage.senderID == FirebaseAuth.getInstance().currentUser?.uid.toString()){
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    inner class SentMessageViewHolder(binding: ItemSendLayoutBinding): RecyclerView.ViewHolder(binding.root){
        val sentMessage = binding.ItemSendSentMessage
    }

    inner class ReceiveMessageViewHolder(binding: ItemReceiveLayoutBinding): RecyclerView.ViewHolder(binding.root){
        val receiveMessage = binding.ItemRecieveMessage
    }

    private companion object{
        private const val ITEM_RECEIVE = 1
        private const val ITEM_SENT = 2
    }
}