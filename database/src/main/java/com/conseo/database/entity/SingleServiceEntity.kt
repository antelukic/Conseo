package com.conseo.database.entity

import android.graphics.Bitmap

data class SingleServiceEntity(
    val image: Bitmap? = null,
    val name: String? = null,
    val location: String? = null,
    val rating: Double? = null
)
