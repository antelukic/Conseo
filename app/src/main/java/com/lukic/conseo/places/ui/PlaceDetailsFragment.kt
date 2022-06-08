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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_place_details, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val args by navArgs<PlaceDetailsFragmentArgs>()

        viewModel.getPlaceByID(args.serviceID, args.serviceType)
        viewModel.getCommentsBy(args.serviceID)


        binding.FragmentPlaceDetailsCheckOnMap.setOnClickListener {
            findNavController().navigate(PlaceDetailsFragmentDirections.actionPlaceDetailsFragmentToMapsFragment(location = viewModel.place.value?.location ?: "", getString(
                            R.string.place_details_fragment)))
        }

        binding.FragmentPlaceDetailsSendMessage.setOnClickListener {
            if(Firebase.auth.currentUser?.uid.toString() != viewModel.place.value?.creatorID.toString())
                findNavController().navigate(PlaceDetailsFragmentDirections.actionPlaceDetailsFragmentToMessageFragment(receiverID = viewModel.place.value?.creatorID ?: ""))
        }
        viewModel.place.observe(viewLifecycleOwner){ placeDetails ->
            setupUI(placeDetails)
        }

        viewModel.errorOccurred.observe(viewLifecycleOwner){
            if(it)
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_LONG).show()
        }

        viewModel.comments.observe(viewLifecycleOwner){ comments ->
            Log.d("PlaceDetailsFragment", "onCreateView: comments $comments")
            if(comments.isNullOrEmpty()) {
                binding.FragmentPlaceDetailsComments.visibility = View.GONE
            } else {
                binding.FragmentPlaceDetailsComments.visibility = View.VISIBLE
                binding.FragmentPlaceDetailsComments.adapter = PlaceCommentRecyclerAdapter(comments)
            }
        }
        return binding.root
    }

    private fun setupUI(placeDetails: PlaceEntity?) {
        Glide.with(requireContext()).load(placeDetails?.image).error(R.mipmap.ic_launcher).into(binding.FragmentPlaceDetailsImage)
        placeDetails?.serviceName?.first()?.uppercase()
        binding.FragmentPlaceDetailsName.text = placeDetails?.serviceName.toString() + " " + placeDetails?.name
        if(placeDetails?.date == null)
            binding.FragmentPlaceDetailsDateTime.visibility = View.GONE
        else
            binding.FragmentPlaceDetailsDateTime.text = "Date and Time: " + placeDetails.date + " " + placeDetails.time
    }


}