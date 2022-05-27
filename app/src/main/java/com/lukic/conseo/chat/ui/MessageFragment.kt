package com.lukic.conseo.chat.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.lukic.conseo.R
import com.lukic.conseo.chat.ui.adapters.MessageRecyclerAdapter
import com.lukic.conseo.chat.viewmodels.MessageViewModel
import com.lukic.conseo.databinding.FragmentMessageBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.log

private const val TAG = "MessageFragment"
class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private val viewModel by sharedViewModel<MessageViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.getCurrentUser()

        val args by navArgs<MessageFragmentArgs>()
        viewModel.receiverID = args.receiverID
        viewModel.sendersRoom = viewModel.currentUserId + args.receiverID
        viewModel.receiversRoom = args.receiverID + viewModel.currentUserId

        viewModel.getMessages()
        viewModel.getReceiverUser()
        viewModel.adapterData.observe(viewLifecycleOwner) { adapterData ->
            binding.FragmentMessageRecyclerView.adapter = MessageRecyclerAdapter(adapterData)
        }


        viewModel.isMessageSent.observe(viewLifecycleOwner) { isMessageSent ->
            if (isMessageSent == false)
                Toast.makeText(
                    requireContext(),
                    "An error occured, please try again!",
                    Toast.LENGTH_LONG
                ).show()
        }

        viewModel.receiver.observe(viewLifecycleOwner) { user ->
            Glide.with(requireContext()).load(user.image).into(binding.FragmentMessageReceiverImage)
            binding.FragmentMessageReceiverName.text = user.name
        }

        viewModel.currentUser.observe(viewLifecycleOwner){ user ->
            if(user?.id.isNullOrEmpty()){
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        }

        viewModel.remoteMessage.observe(viewLifecycleOwner){
            Log.d(TAG, "onCreateView: remoteMessage ${it.data}")
            viewModel.updateChatWithRemoteMessage()
        }

        binding.FragmentMessageBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        return binding.root
    }



}