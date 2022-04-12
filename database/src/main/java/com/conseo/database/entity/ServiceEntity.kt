package com.conseo.database.entity

data class ServiceEntity(
    val creatorID: String? = null,
    val name: String? = null,
    val location: String? = null,
    var image: String? = null,
    val info: String? = null,
    val serviceName: String? = null,
    val date: String? = null,
    val time: String? = null
)