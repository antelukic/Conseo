package com.lukic.restapi.firebase.models

data class PushNotification(
    val data: NotificationData,
    val to: String
)
