package com.lukic.conseo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val TAG = "FirebaseService"
class FirebaseService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: token $token")
        val prefs = getSharedPreferences(
            getString(R.string.token),
            Context.MODE_PRIVATE
        )
        prefs.edit().putString(
            getString(R.string.token_key), token
        ).apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived: message ${message.data}")
        remoteMessage.postValue(message)
        //SendNotificationUpdateChat
        sendNotification(message)
    }
    private fun sendNotification(message: RemoteMessage){
        val channelId = "ConseoChannel"
        val channel = NotificationChannel(
            channelId,
            "my_notification",
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.enableLights(true)
        channel.lightColor = Color.GREEN
        channel.enableVibration(false)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("receiverID", message.data["senderID"])
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setPriority(NotificationCompat.PRIORITY_MAX)
        builder.setContentIntent(pendingIntent).setAutoCancel(true)

        mNotificationManager.createNotificationChannel(channel)//Notice this
        mNotificationManager.notify(123, builder.build())
    }


    companion object{
        val remoteMessage = MutableLiveData<RemoteMessage>()
    }
}