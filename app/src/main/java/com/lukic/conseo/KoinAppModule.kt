package com.lukic.conseo

import com.lukic.conseo.chat.model.ChatRepository
import com.lukic.conseo.chat.viewmodels.AllChatsViewModel
import com.lukic.conseo.chat.viewmodels.MessageViewModel
import com.lukic.conseo.loginregister.viewmodels.LoginViewModel
import com.lukic.conseo.loginregister.viewmodels.RegisterViewModel
import com.lukic.conseo.model.LoginRegisterRepository
import com.lukic.conseo.places.model.PlacesRepository
import com.lukic.conseo.places.viewmodels.AddPlaceViewModel
import com.lukic.conseo.places.viewmodels.PlaceDetailsViewModel
import com.lukic.conseo.places.viewmodels.PlaceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { PlacesRepository(placesDao = get(), commentsDao = get()) }
    single { ChatRepository(chatDao = get(), get()) }
    single { LoginRegisterRepository(usersDao = get()) }
}

val viewModelModules = module {
    viewModel { RegisterViewModel(get())}
    viewModel { LoginViewModel(get()) }
    viewModel { PlaceViewModel( get()) }
    viewModel { AddPlaceViewModel( get()) }
    viewModel { AllChatsViewModel( get()) }
    viewModel { MessageViewModel( get()) }
    viewModel { PlaceDetailsViewModel(get(), get()) }
}