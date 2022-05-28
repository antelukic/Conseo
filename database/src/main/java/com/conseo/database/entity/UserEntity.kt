package com.conseo.database.entity

data class UserEntity(
    val id: String? = null,
    var name: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val email: String? = null,
    val password: String? = null,
    var image: String? = null,
    var token: String? = null
)
