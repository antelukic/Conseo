package com.conseo.database.entity

data class UserEntity(
    val id: String? = null,
    val name: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val email: String? = null,
    val password: String? = null,
    var image: String? = null
)
