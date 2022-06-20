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
import com.lukic.conseo.utils.awaitTask
import com.lukic.conseo.utils.loadImage
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
            try {
                val documentSnapshot =
                    chatRepository.getUserById(currentUserId).awaitTask(viewModelScope)
                if (documentSnapshot != null) {
                    val user = documentSnapshot.toObject(UserEntity::class.java)
                    _currentUser.postValue(user)
                } else {
                    Log.e(
                        TAG,
                        "getCurrentUser: ERROR document is null",
                    )
                    _currentUser.postValue(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getCurrentUser: ${e.message.toString()}")
            }
        }
    }


    fun sendMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                if (!messageText.value.isNullOrEmpty()) {
                    canSendMessage.postValue(false)
                    message = MessageEntity(
                        senderID = currentUserId,
                        message = messageText.value,
                        recieverID = receiverID.trim(),
                        time = getCurrentTime()
                    )
                    val document =
                        chatRepository.storeChat(room = receiversRoom, userID = receiverID.trim())
                            .awaitTask(viewModelScope)
                    if (document != null) {
                        storeChatForSender()
                    } else {
                        isMessageSent.postValue(false)
                        canSendMessage.postValue(true)
                        Log.e(TAG, "document reference is null")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage: ${e.message.toString()}")
            }
        }
    }

    private fun getCurrentTime(): Timestamp {
        return Timestamp.now()
    }

    private fun storeChatForSender() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document =
                    chatRepository.storeChat(room = sendersRoom, userID = currentUserId.trim())
                        .awaitTask(viewModelScope)
                if (document != null) {
                    sendMessageForReceiver()
                } else {
                    isMessageSent.postValue(false)
                    canSendMessage.postValue(true)
                    Log.e(TAG, "storeChatForSender: document reference is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "storeChatForSender: ${e.message.toString()}")
            }
        }
    }

    private fun sendMessageForReceiver() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val document = chatRepository.sendMessage(message = message!!, room = receiversRoom)
                .awaitTask(viewModelScope)
            if (document != null) {
                sendMessageForSender()
            } else {
                Log.e(TAG, "document reference is null")
                isMessageSent.postValue(false)
                canSendMessage.postValue(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendMessageForReceiver: ${e.message.toString()}")
        }
    }


    private fun sendMessageForSender() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val document = chatRepository.sendMessage(message = message!!, room = sendersRoom)
                .awaitTask(viewModelScope)
            if (document != null) {
                sendSenderNotification()
            } else {
                Log.d(TAG, "document is null")
                isMessageSent.postValue(false)
                canSendMessage.postValue(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendMessageForSender: ${e.message.toString()}")
        }
    }

    private fun sendSenderNotification() = viewModelScope.launch(Dispatchers.IO) {
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
            Log.e(TAG, "sendSenderNotification: ERROR ${response.raw()}")
            isMessageSent.postValue(false)
            canSendMessage.postValue(true)
        }
    }

    fun getMessages() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val querySnapshot = chatRepository.getMessages(sendersRoom).awaitTask(viewModelScope)
            if (querySnapshot != null) {
                val messages = querySnapshot.toObjects(MessageEntity::class.java)
                adapterData.postValue(messages as ArrayList<MessageEntity>?)
            } else {
                Log.e(TAG, "querySnapshot is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMessages: ${e.message.toString()}")
        }
    }

    fun getReceiverUser() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val documentSnapshot =
                chatRepository.getUserById(id = receiverID).awaitTask(viewModelScope)
            if (documentSnapshot != null) {
                val user = documentSnapshot.toObject(UserEntity::class.java)
                user?.let { receiver.postValue(it) }
            } else {
                Log.e(TAG, "getReceiverUser: documentSnapshot is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getReceiverUser: ${e.message.toString()}")
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