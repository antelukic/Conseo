package com.lukic.conseo.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.conseo.database.entity.ServiceEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentSingleServiceBinding
import com.lukic.conseo.ui.adapters.SingleServicesRecyclerAdapter
import com.lukic.conseo.utils.OnItemClickListener
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

        viewModel.adapterData.observe(viewLifecycleOwner){ adapterData ->
            binding.FragmentSingleServiceRecyclerView.adapter = SingleServicesRecyclerAdapter(singleServices = adapterData, listener = itemClickListener)
        }

        return binding.root
    }

    private val itemClickListener = object: OnItemClickListener{
        override fun onClick(item: Any) {
            item as ServiceEntity
            if(Firebase.auth.currentUser?.uid.toString() != item.creatorID)
                findNavController().navigate(ServicesFragmentDirections.actionServicesFragmentToMessageFragment(item.creatorID.toString()))
        }
    }

}