package com.lukic.conseo.places.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentAddPlaceBinding
import com.lukic.conseo.places.viewmodels.AddPlaceViewModel
import lv.chi.photopicker.PhotoPickerFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

private const val TAG = "AddServiceFragment"
class AddPlaceFragment : Fragment(),  PhotoPickerFragment.Callback {

    private lateinit var binding: FragmentAddPlaceBinding
    private val viewModel by sharedViewModel<AddPlaceViewModel>()
    private var imageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.fragment = this
        binding.lifecycleOwner = viewLifecycleOwner

        //This works when user returns from map fragment
        val args by navArgs<AddPlaceFragmentArgs>()
        if(args.location.isNotEmpty())
            viewModel.location.value = args.location

        if(viewModel.imageBitmap != null)
            Glide.with(requireContext()).load(viewModel.imageBitmap).into(binding.FragmentAddPlaceImage)


        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.spinner_dropdown,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.FragmentAddPlaceSpinner.adapter = spinnerAdapter

        binding.FragmentAddPlaceLookOnMap.setOnClickListener{
            if(!viewModel.location.value.isNullOrEmpty())
                findNavController().navigate(AddPlaceFragmentDirections.actionAddServiceFragmentToMapsFragment(viewModel.location.value ?: "", getString(
                                    R.string.add_place_fragment)))
            else
                Toast.makeText(requireContext(), "Write the location first!", Toast.LENGTH_LONG).show()
        }

        binding.FragmentAddPlaceAddButton.setOnClickListener {
            if(args.location.isNotEmpty() && viewModel.latlng != null) {
                viewModel.addPlace(location = args.location)
            } else{
                Toast.makeText(requireContext(), "Add the location first", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.proceed.observe(viewLifecycleOwner){ proceed ->
            if(proceed == true) {
                viewModel.deleteValues()
                findNavController().navigate(AddPlaceFragmentDirections.actionAddServiceFragmentToServicesFragment())
            }
            if(proceed == false)
                Toast.makeText(requireContext(), "Something went wrong, please try again later!", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    override fun onImagesPicked(photos: ArrayList<Uri>) {
        imageBitmap = viewModel.getBitmap(photos.first(), requireContext().contentResolver)
        if(imageBitmap != null)
            Glide.with(requireContext()).load(imageBitmap).into(binding.FragmentAddPlaceImage)    }

    fun openPicker() {
        PhotoPickerFragment.newInstance(
            multiple = true,
            allowCamera = true,
            maxSelection = 1,
            theme = lv.chi.photopicker.R.style.ChiliPhotoPicker_Light
        ).show(childFragmentManager, "picker")
    }


}