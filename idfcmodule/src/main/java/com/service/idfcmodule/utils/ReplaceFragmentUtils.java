package com.service.idfcmodule.utils;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.service.idfcmodule.R;

public class ReplaceFragmentUtils {
    public static void replaceFragment(Fragment fragment, Bundle bundle, AppCompatActivity activity) {
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.replaceLayout, fragment);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
