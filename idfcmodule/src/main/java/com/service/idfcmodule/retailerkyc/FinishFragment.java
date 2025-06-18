package com.service.idfcmodule.retailerkyc;

import static com.service.idfcmodule.IdfcMainActivity.comType;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentFinishBinding;

public class FinishFragment extends Fragment {
FragmentFinishBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentFinishBinding.inflate(inflater);

        if (comType.equalsIgnoreCase("Vidcom")) {
           binding.tvFinish.setBackgroundResource(R.drawable.button_on_dec2);
        }

       binding.tvFinish.setOnClickListener(v -> {
           requireActivity().finish();
       });

       return binding.getRoot();

    }
}