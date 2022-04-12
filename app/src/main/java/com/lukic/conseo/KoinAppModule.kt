package com.lukic.conseo

import com.lukic.conseo.repository.AppRepository
import com.lukic.conseo.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { AppRepository(usersDao = get(), serviceDao = get(), chatDao = get()) }
}

val viewModelModules = module {
    viewModel { RegisterViewModel(appRepository = get()) }
    viewModel { LoginViewModel(appRepository = get()) }
    viewModel { SingleServiceViewModel(appRepository = get()) }
    viewModel { MapsViewModel(appRepository = get()) }
    viewModel { AddServiceViewModel(appRepository = get()) }
    viewModel { AllChatsViewModel(appRepository = get()) }
    viewModel { MessageViewModel(appRepository = get()) }
}