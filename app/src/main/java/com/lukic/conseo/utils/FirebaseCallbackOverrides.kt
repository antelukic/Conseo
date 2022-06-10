package com.lukic.conseo.utils

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await


suspend fun <T> Task<T>.awaitTask(scope: CoroutineScope): T? = scope.async{
    await()
    val e = exception
    if(e != null)
        throw e
    if(isSuccessful)
        return@async result
    else
        return@async null
}.await()