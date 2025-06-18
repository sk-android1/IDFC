package com.service.idfcmodule.fragments;

import static android.app.Activity.RESULT_OK;

import static com.service.idfcmodule.IdfcMainActivity.comType;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentDashboardBinding;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;

public class DashboardFragment extends Fragment {

    FragmentDashboardBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater);

        if (comType.equalsIgnoreCase("Vidcom")){
            binding.imgAssign.setBackgroundResource(R.drawable.button_on_dec2);
            binding.imgAssignList.setBackgroundResource(R.drawable.button_on_dec2);
        }

       handleClickEvents();

//       requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                moduleFinishMessage("Back Button Pressed");
//            }
//        });

        return binding.getRoot();

    }

    private void handleClickEvents() {
        binding.doAssignLy.setOnClickListener(v -> {
            ReplaceFragmentUtils.replaceFragment(new AsmRetAssignFragment(), new Bundle(), (AppCompatActivity)requireActivity() );
        });
        binding.assignedLy.setOnClickListener(v -> {
            ReplaceFragmentUtils.replaceFragment(new AsmRetAssignListFragment(), new Bundle(), (AppCompatActivity)requireActivity());
        });
    }

    private void moduleFinishMessage(String message) {
        Intent in = new Intent();
        in.putExtra("message", message);
        requireActivity().setResult(RESULT_OK,in);
        requireActivity().finish();
    }

}