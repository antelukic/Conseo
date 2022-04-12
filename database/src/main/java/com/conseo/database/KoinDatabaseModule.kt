package com.conseo.database

import com.conseo.database.dao.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.koin.dsl.module

val databaseModule = module {

    single { UsersDao(firebase = Firebase, database = Firebase.firestore, storage = FirebaseStorage.getInstance()) }
    single { ServiceDao(database = Firebase.firestore, storage = FirebaseStorage.getInstance())}
    single { ChatDao(database = Firebase.firestore) }

}