package com.lukic.conseo.geofencing

import com.conseo.database.dao.PlacesDao
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot

class GeofencingRepository(
    private val placesDao: PlacesDao
) {

    fun getAllBars(): Task<QuerySnapshot> {
        return placesDao.getAllBars()
    }

    fun getAllRestaurants(): Task<QuerySnapshot>{
        return placesDao.getAllRestaurants()
    }

    fun getAllEvents(): Task<QuerySnapshot>{
        return placesDao.getAllEvents()
    }

}