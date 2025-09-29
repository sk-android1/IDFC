package com.service.idfcmodule.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.service.idfcmodule.R;

public class NetworkUtils {

    public static String getConnectivityStatusString(Context context) {
        String status = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            status = "Connected";
        } else {
            status = "No internet is available";
        }
        return status;
    }

    public static void addressToMap(String address, Context context) {
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                // Fallback: open in browser
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "unable to open google map", Toast.LENGTH_SHORT).show();
        }
    }

    public static void gpsCoOrdinatesToMap(String latitude, String longitude, Context context) {
        try {
            String geoUri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(My Location)";

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.parse(geoUri));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "unable to open google map", Toast.LENGTH_SHORT).show();
        }
    }

}
