package com.lukic.conseo.places.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.conseo.database.entity.PlaceEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentPlaceDetailsBinding
import com.lukic.conseo.places.ui.adapters.PlaceCommentRecyclerAdapter
import com.lukic.conseo.places.viewmodels.PlaceDetailsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class PlaceDetailsFragment : Fragment() {

    private val viewModel by sharedViewModel<PlaceDetailsViewModel>()
    private lateinit var binding: FragmentPlaceDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaceDetailsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val args by navArgs<PlaceDetailsFragmentArgs>()

        viewModel.getPlaceByID(args.serviceID, args.serviceType)
        viewModel.getCommentsBy(args.serviceID)

        viewModel.errorOccurred.observe(viewLifecycleOwner){
            if(it)
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_LONG).show()
        }

        viewModel.comments.observe(viewLifecycleOwner){ comments ->
                binding.FragmentPlaceDetailsComments.adapter = PlaceCommentRecyclerAdapter(comments)
        }
        return binding.root
    }

    fun checkOnMapClicked(){
        findNavController().navigate(PlaceDetailsFragmentDirections.actionPlaceDetailsFragmentToMapsFragment(location = viewModel.place.value?.location ?: "", getString(
            R.string.place_details_fragment)))
    }

    fun sendMessageClicked(){
        if(Firebase.auth.currentUser?.uid.toString() != viewModel.place.value?.creatorID.toString())
            findNavController().navigate(PlaceDetailsFragmentDirections.actionPlaceDetailsFragmentToMessageFragment(receiverID = viewModel.place.value?.creatorID ?: ""))
    }
}