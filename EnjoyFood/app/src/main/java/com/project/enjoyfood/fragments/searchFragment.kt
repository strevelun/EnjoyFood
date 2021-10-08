package com.project.enjoyfood.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.project.enjoyfood.R
import com.project.enjoyfood.databinding.FragmentAccountBinding
import com.project.enjoyfood.databinding.FragmentSearchBinding

class searchFragment : Fragment() {

    private lateinit var binding : FragmentSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_search, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }
}