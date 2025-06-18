package com.service.idfcmodule.lead;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentCloseSRBinding;


public class CloseSRFragment extends Fragment {
    FragmentCloseSRBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCloseSRBinding.inflate(inflater);

        binding.tvHome.setOnClickListener(v -> {
            requireActivity().finish();
        });

        return binding.getRoot();
    }
}