package com.lukic.conseo.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentSingleServiceBinding
import com.lukic.conseo.viewmodel.SingleServiceViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SingleServiceFragment : Fragment() {

    private val viewModel by sharedViewModel<SingleServiceViewModel>()
    private lateinit var binding: FragmentSingleServiceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_single_service, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

//        binding.FragmentSingleServiceRecyclerView.adapter = MyServicesRecyclerAdapter()

        return binding.root
    }

}