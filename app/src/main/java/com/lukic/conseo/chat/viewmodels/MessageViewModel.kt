package com.lukic.conseo.chat.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.MessageEntity
import com.conseo.database.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.lukic.conseo.chat.model.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MessageViewModel"

class MessageViewModel(
    private val chatRepository: ChatRepository
    ) : ViewModel() {

    val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid.toString() }
    var receiverID = ""
    var receiversRoom = ""
    var sendersRoom = ""
    val messageText = MutableLiveData<String>()

    val isMessageSent = MutableLiveData<Boolean>()
    val receiver = MutableLiveData<UserEntity>()

    val adapterData = MutableLiveData<ArrayList<MessageEntity>>()
    private var message: MessageEntity? = null

    fun sendMessage() {
        Log.d(TAG, "sendMessage: sendersRoom: $sendersRoom")
        Log.d(TAG, "sendMessage: sendersID: $currentUserId")
        Log.d(TAG, "sendMessage: receiversRoom: $receiversRoom")
        Log.d(TAG, "sendMessage: receiversID: $receiverID")
        viewModelScope.launch(Dispatchers.IO) {
            message = MessageEntity(
                senderID = currentUserId,
                message = messageText.value,
                recieverID = receiverID.trim()
            )
            chatRepository.storeChat(room = receiversRoom, userID = receiverID.trim())
                .addOnCompleteListener { receiverTask ->
                    if (receiverTask.isSuccessful) {
                        storeChatForSender()
                    } else {
                        isMessageSent.postValue(false)
                        Log.e(TAG, receiverTask.exception?.message.toString())
                    }
                }
        }
    }

    private fun storeChatForSender() {
        chatRepository.storeChat(room = sendersRoom, userID = currentUserId.trim())
            .addOnCompleteListener { senderTask ->
                if (senderTask.isSuccessful) {
                    sendMessageForReceiver()
                } else {
                    isMessageSent.postValue(false)
                }
            }

    }

    private fun sendMessageForReceiver() {
        chatRepository.sendMessage(message = message!!,room = receiversRoom)
            .addOnCompleteListener{ taskResult ->
                if(taskResult.isSuccessful){
                    sendMessageForSender()
                }else{
                    Log.e(TAG, taskResult.exception?.message.toString())
                    isMessageSent.postValue(false)
                }
            }
    }

    private fun sendMessageForSender() {
        chatRepository.sendMessage(message = message!!,room = sendersRoom)
            .addOnCompleteListener { taskResult ->
                if(taskResult.isSuccessful){
                    messageText.postValue("")
                    adapterData.value?.add(message!!)
                    isMessageSent.postValue(true)
                } else {
                    Log.d(TAG, taskResult.exception?.message.toString())
                    isMessageSent.postValue(false)
                }
            }
    }

    fun getMessages(){
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "getMessages: sendersRoom: $sendersRoom")
            chatRepository.getMessages(sendersRoom)
                .addOnCompleteListener{ messagesResult ->
                    if(messagesResult.isSuccessful) {
                        val messages = messagesResult.result.toObjects(MessageEntity::class.java)
                        adapterData.postValue(messages as ArrayList<MessageEntity>?)
                        Log.d(TAG, messages.toString())
                    } else {
                        Log.e(TAG, messagesResult.exception?.message.toString())
                    }
                }
        }
    }

    fun getReceiverUser() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getUserById(id = receiverID)
                .addOnCompleteListener { userTaskResult ->
                    if(userTaskResult.isSuccessful){
                        val user = userTaskResult.result.toObject(UserEntity::class.java)
                        receiver.postValue(user)
                    }
                }
        }
    }
}