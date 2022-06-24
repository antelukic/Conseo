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
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lukic.conseo.MainActivity
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R

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
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
        ) {

            // Get the geofence that were triggered. A single event can trigger
            // multiple geofence.
            val triggeringGeofence = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            triggeringGeofence?.forEach {

                val geofenceRequestID = it?.requestId

                if (geofenceRequestID != null && Firebase.auth.currentUser != null) {
                    sendGeofenceEnteredNotification(geofenceRequestID)
                }
            }
        } else {
            Log.e(TAG, "Geofence Transition Invalid $geofenceTransition")
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
            val intent = Intent(MyApplication.getAppContext(), MainActivity::class.java)
            intent.putExtra("placeID", place.first().placeID)
            intent.putExtra("placeType", place.first().serviceName)
            val pendingIntent =
                PendingIntent.getActivity(MyApplication.getAppContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

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
