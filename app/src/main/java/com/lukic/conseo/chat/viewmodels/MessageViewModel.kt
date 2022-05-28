package com.lukic.conseo.chat.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.MessageEntity
import com.conseo.database.entity.UserEntity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.lukic.conseo.FirebaseService
import com.lukic.conseo.chat.model.ChatRepository
import com.lukic.restapi.firebase.models.NotificationData
import com.lukic.restapi.firebase.models.PushNotification
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

    private val _currentUser = MutableLiveData<UserEntity?>()
    val currentUser get() = _currentUser as LiveData<UserEntity?>

    val remoteMessage = FirebaseService.remoteMessage
    val canSendMessage = MutableLiveData(true)


    fun getCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getUserById(currentUserId)
                .addOnCompleteListener { getCurrentUserTask ->
                    if (getCurrentUserTask.isSuccessful) {
                        val user = getCurrentUserTask.result.toObject(UserEntity::class.java)
                        _currentUser.postValue(user)
                    } else {
                        Log.e(
                            TAG,
                            "getCurrentUser: ERROR ${getCurrentUserTask.exception?.message}",
                        )
                        _currentUser.postValue(null)
                    }
                }
        }
    }


    fun sendMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!messageText.value.isNullOrEmpty()) {
                canSendMessage.postValue(false)
                message = MessageEntity(
                    senderID = currentUserId,
                    message = messageText.value,
                    recieverID = receiverID.trim(),
                    time = getCurrentTime()
                )
                chatRepository.storeChat(room = receiversRoom, userID = receiverID.trim())
                    .addOnCompleteListener { receiverTask ->
                        if (receiverTask.isSuccessful) {
                            storeChatForSender()
                        } else {
                            isMessageSent.postValue(false)
                            canSendMessage.postValue(true)
                            Log.e(TAG, receiverTask.exception?.message.toString())
                        }
                    }
            }
        }
    }

    private fun getCurrentTime(): Timestamp {
        return Timestamp.now()
    }

    private fun storeChatForSender() {
        chatRepository.storeChat(room = sendersRoom, userID = currentUserId.trim())
            .addOnCompleteListener { senderTask ->
                if (senderTask.isSuccessful) {
                    sendMessageForReceiver()
                } else {
                    isMessageSent.postValue(false)
                    canSendMessage.postValue(true)
                }
            }

    }

    private fun sendMessageForReceiver() {
        chatRepository.sendMessage(message = message!!, room = receiversRoom)
            .addOnCompleteListener { taskResult ->
                if (taskResult.isSuccessful) {
                    sendMessageForSender()
                } else {
                    Log.e(TAG, taskResult.exception?.message.toString())
                    isMessageSent.postValue(false)
                    canSendMessage.postValue(true)
                }
            }
    }

    private fun sendMessageForSender() {
        chatRepository.sendMessage(message = message!!, room = sendersRoom)
            .addOnCompleteListener { taskResult ->
                if (taskResult.isSuccessful) {
                    sendSenderNotification()
                } else {
                    Log.d(TAG, taskResult.exception?.message.toString())
                    isMessageSent.postValue(false)
                    canSendMessage.postValue(true)
                }
            }
    }

    private fun sendSenderNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = chatRepository.sendChatNotification(
                PushNotification(
                    NotificationData(
                        title = currentUser.value!!.name ?: "Name error",
                        message = messageText.value ?: "",
                        senderID = currentUserId
                    ),
                    to = receiver.value?.token ?: "Token error"
                )
            )
            if (response.isSuccessful) {
                messageText.postValue("")
                val tempData = adapterData.value ?: arrayListOf()
                tempData.add(0, message!!)
                adapterData.postValue(tempData)
                isMessageSent.postValue(true)
                canSendMessage.postValue(true)
            } else {
                Log.e(TAG, "sendSenderNotification: ERROR ${response.errorBody()}")
                isMessageSent.postValue(false)
                canSendMessage.postValue(true)
            }
        }
    }

    fun getMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "getMessages: sendersRoom: $sendersRoom")
            chatRepository.getMessages(sendersRoom)
                .addOnCompleteListener { messagesResult ->
                    if (messagesResult.isSuccessful) {
                        val messages = messagesResult.result.toObjects(MessageEntity::class.java)
                        adapterData.postValue(messages as ArrayList<MessageEntity>?)
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
                    if (userTaskResult.isSuccessful) {
                        val user = userTaskResult.result.toObject(UserEntity::class.java)
                        user?.let { receiver.postValue(it) }
                    }
                }
        }
    }

    fun updateChatWithRemoteMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            if (receiverID == remoteMessage.value?.data?.get("senderID").toString()) {
                val remoteMessageText = remoteMessage.value?.data?.get("message").toString()
                messageText.postValue("")
                val tempData = adapterData.value ?: arrayListOf()
                tempData.add(
                    0,
                    MessageEntity(
                        senderID = remoteMessage.value?.data?.get("senderID"),
                        recieverID = currentUserId,
                        message = remoteMessageText,
                        time = getCurrentTime()
                    )
                )
                adapterData.postValue(tempData)
            }
        }
    }


}