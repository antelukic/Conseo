package com.lukic.conseo.geofencing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.lukic.conseo.BaseActivity
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R
import com.lukic.conseo.loginregister.ui.LoginRegisterActivity

private const val TAG = "GeofencingBroadcastReceiver"

class GeofencingBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent?.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceRequestID = triggeringGeofences.first()?.requestId

            if (geofenceRequestID != null) {
                Log.i(TAG, geofenceRequestID)
                sendGeofenceEnteredNotification(geofenceRequestID)
            }
        } else {
            Log.e(TAG, "Geofence Transition Invalid")
        }

    }


    private fun sendGeofenceEnteredNotification(requestID: String) {
        val place =
            GeofencingViewModel.allPlaces?.filter { it -> it.placeID == requestID }

        Log.d(TAG, "sendGeofenceEnteredNotification: place $place")

        if (!place.isNullOrEmpty()) {
            val channelId = "ConseoChannel"
            val channel = NotificationChannel(
                channelId,
                "my_notification",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            channel.enableVibration(false)
            val intent = Intent(MyApplication.getAppContext(), LoginRegisterActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(MyApplication.getAppContext(), 0, intent, 0)

            val mNotificationManager =
                MyApplication.getAppContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val builder = NotificationCompat.Builder(MyApplication.getAppContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("You are close to ${place.first().name}")
                .setContentText("Come and visit")
                .setPriority(NotificationCompat.PRIORITY_MAX)
            builder.setContentIntent(pendingIntent).setAutoCancel(true)

            mNotificationManager.createNotificationChannel(channel)//Notice this
            mNotificationManager.notify(123, builder.build())
        }
    }
}
