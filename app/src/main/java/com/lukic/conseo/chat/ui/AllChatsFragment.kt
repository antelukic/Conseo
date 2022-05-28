package com.lukic.conseo.chat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.conseo.database.entity.UserEntity
import com.lukic.conseo.R
import com.lukic.conseo.chat.ui.adapters.AllChatsRecyclerAdapter
import com.lukic.conseo.chat.viewmodels.AllChatsViewModel
import com.lukic.conseo.databinding.FragmentAllChatsBinding
import com.lukic.conseo.utils.OnItemClickListener
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AllChatsFragment : Fragment() {

    private lateinit var binding: FragmentAllChatsBinding
    private val viewModel by sharedViewModel<AllChatsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_chats, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.getAllChats()
        viewModel.adapterData.observe(viewLifecycleOwner){ adapterData ->
            if(adapterData != null)
                binding.FragmentChatsFragmentRecyclerView.adapter = AllChatsRecyclerAdapter(
                    chats = adapterData,
                    listener = itemClickListener
                )

        }

        return binding.root
    }

    private val itemClickListener = object: OnItemClickListener{
        override fun onClick(item: Any) {
            item as UserEntity
            findNavController().navigate(AllChatsFragmentDirections.actionAllChatsFragmentToMessageFragment2(receiverID = item.id.toString()))
        }
    }
}