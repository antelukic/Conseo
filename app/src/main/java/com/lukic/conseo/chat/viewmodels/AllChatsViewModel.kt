package com.lukic.conseo.chat.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.Chat
import com.conseo.database.entity.MessageEntity
import com.conseo.database.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.lukic.conseo.chat.model.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AllChatsViewModel"
class AllChatsViewModel(
    private val chatRepository: ChatRepository
): ViewModel() {

    val adapterData = MutableLiveData<List<UserEntity>?>()
    val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid.toString() }

    fun getAllChats(){
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getChats(currentUserId)
                .addOnCompleteListener{ taskResult ->
                    if(taskResult.isSuccessful){
                        val chats = taskResult.result.toObjects(Chat::class.java)
                        Log.d(TAG, "chats $chats")
                        getMessages(chats = chats)
                    } else {
                        adapterData.postValue(null)
                        Log.e(TAG, taskResult.exception?.message.toString())
                    }
                }
        }
    }

    private fun getMessages(chats: List<Chat>){
        val data = mutableListOf<List<MessageEntity>>()
        chats.forEachIndexed{index, chat ->
            chatRepository.getMessages(chat.messageID!!)
                .addOnCompleteListener { taskResult ->
                    if(taskResult.isSuccessful){
                        val message = taskResult.result.toObjects(MessageEntity::class.java)
                        Log.d(TAG, "message $message")
                        data.add(message)

                        if(index < chats.size)
                            getUsers(chats = data)
                    } else{
                        Log.e(TAG, taskResult.exception?.message.toString())
                        adapterData.postValue(null)
                    }
                }

        }
    }

    private fun getUsers(chats: List<List<MessageEntity>>) {
        val ids = mutableListOf<String>()
        chats.forEach { chat ->
            chat.forEach { message ->
                Log.d(TAG, "getUsers: chat-$chat message-$message")
                if (message.recieverID.toString() != currentUserId) {
                    if (!ids.contains(message.recieverID.toString().trim()))
                        ids.add(message.recieverID.toString().trim())
                }
                if(message.senderID.toString() != currentUserId) {
                    if (!ids.contains(message.senderID.toString().trim()))
                        ids.add(message.senderID.toString().trim())
                }
            }
        }
        Log.d(TAG, "ids $ids")
        if(ids.isNotEmpty()) {
            chatRepository.getAllUsers()
                .addOnCompleteListener { taskResult ->
                    if (taskResult.isSuccessful) {
                        val allUsers = taskResult.result.toObjects(UserEntity::class.java) as List<UserEntity>
                        Log.d(TAG, "all users $allUsers")
                        getChatUsers(ids, allUsers)
                    } else {
                        Log.e(TAG, taskResult.exception?.message.toString())
                    }
                }
        }
    }

    private fun getChatUsers(ids: MutableList<String>, allUsers: List<UserEntity>) {
        val data = mutableListOf<UserEntity>()
        ids.forEach{ id->
            allUsers.forEach{ user->
                if(id == user.id){
                    data.add(user)
                }
            }
        }
        adapterData.postValue(data)
    }
}