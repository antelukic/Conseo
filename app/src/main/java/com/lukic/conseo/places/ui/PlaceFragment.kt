package com.lukic.conseo.places.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.conseo.database.entity.PlaceEntity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentPlaceBinding
import com.lukic.conseo.places.ui.adapters.PlacesRecyclerAdapter
import com.lukic.conseo.places.viewmodels.PlaceViewModel
import com.lukic.conseo.utils.OnItemClickListener
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class PlaceFragment : Fragment() {

    private val viewModel by sharedViewModel<PlaceViewModel>()
    private lateinit var binding: FragmentPlaceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_place, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.adapterData.observe(viewLifecycleOwner){
            viewModel.getUserLocation()
        }

        viewModel.userLatLng.observe(viewLifecycleOwner){ latLng ->
            if(latLng == null)
                Toast.makeText(requireContext(), "Turn on your location please", Toast.LENGTH_LONG).show()
            else
                binding.FragmentPlaceRecyclerView.adapter = PlacesRecyclerAdapter(singlePlaces = viewModel.adapterData.value ?: listOf(), listener = itemClickListener)
        }

        return binding.root
    }


    private val itemClickListener = object: OnItemClickListener{
        override fun onClick(item: Any) {
            item as PlaceEntity
            findNavController().navigate(PlacesFragmentDirections.actionPlacesFragmentToPlaceDetailsFragment(item.placeID ?: "", item.serviceName ?: ""))
        }
    }

}