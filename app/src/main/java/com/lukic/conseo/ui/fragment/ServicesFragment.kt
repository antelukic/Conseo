package com.lukic.conseo.ui.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lukic.conseo.R
import com.lukic.conseo.viewmodel.ServicesViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ServicesFragment : Fragment() {

    private val viewModel by sharedViewModel<ServicesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.services_fragment, container, false)
    }

}