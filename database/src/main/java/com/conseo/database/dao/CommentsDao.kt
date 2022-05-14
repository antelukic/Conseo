package com.conseo.database.dao

import com.conseo.database.entity.CommentsEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class CommentsDao(
    private val database: FirebaseFirestore,
    ) {

    fun getCommentsForPlace(placeID: String): Task<QuerySnapshot> {
        return database.collection("comments").document(placeID).collection("placeComments").get()
    }

    fun postComment(comment: CommentsEntity): Task<DocumentReference> {
        return database.collection("comments").document(comment.placeID ?: "").collection("placeComments").add(comment)
    }
}