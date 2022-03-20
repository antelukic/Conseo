package com.lukic.conseo

import com.lukic.conseo.repository.AppRepository
import com.lukic.conseo.viewmodel.LoginViewModel
import com.lukic.conseo.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { AppRepository(get()) }
}

val viewModelModules = module {
    viewModel { RegisterViewModel(get()) }
    viewModel { LoginViewModel(get()) }
}