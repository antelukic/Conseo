package com.lukic.conseo

import com.lukic.conseo.repository.AppRepository
import com.lukic.conseo.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { AppRepository(get(), get()) }
}

val viewModelModules = module {
    viewModel { RegisterViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SingleServiceViewModel(get()) }
    viewModel { MapsViewModel(get()) }
    viewModel { AddServiceViewModel(get()) }
}