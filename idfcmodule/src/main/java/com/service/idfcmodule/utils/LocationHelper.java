package com.service.idfcmodule.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.service.idfcmodule.R;
import com.service.idfcmodule.myinterface.LocationListener;

public class LocationHelper {

    private final Context context;
    private final Activity activity;

    private static final int LOCATION_PERMISSION = 101;
    private FusedLocationProviderClient fusedClient;

    public String latitude = "";
    public String longitude = "";
    public Location currentLocation;

    private LocationListener listener;

    boolean isGPSEnabled = false;

    public void setLocationListener(LocationListener listener) {
        this.listener = listener;
    }

    public LocationHelper(Context context, Activity activity ) {
        this.context = context;
        this.activity = activity;
        fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // ------------------------------------
    // 1 Main entry
    // ------------------------------------
    public void checkPermissionAndGetLocation() {
        if (checkLocationPermission()) {
            if (latitude.equals("") || longitude.equals("") || latitude.equals("0.0") || longitude.equals("0.0")) {

           //     Toast.makeText(context, "Location not found. Try again.", Toast.LENGTH_SHORT).show();
                getLocationLatLang();
            } else {
                startGettingLocation();
            }
        }
    }

    // ------------------------------------
    // 2 Start Location Updates
    // ------------------------------------
    @SuppressLint("MissingPermission")
    public void startGettingLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.i("TAG", "Location result available");
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability availability) {
                super.onLocationAvailability(availability);
                Log.i("TAG", availability.isLocationAvailable() ? "Location available" : "Location unavailable");
            }
        };

        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        fusedClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currentLocation = location;
                        if (location != null) {
                            latitude = String.valueOf(location.getLatitude());
                            longitude = String.valueOf(location.getLongitude());
                            Log.d("TAG", "Lat: " + latitude + " Lng: " + longitude);

                            if (listener != null) {
                                listener.onLocationFetched(latitude, longitude,isGPSEnabled);
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "Location fetch failed: " + e.getMessage());
                    }
                });
    }

    // ------------------------------------
    // 3 Permission Check
    // ------------------------------------
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION);
            return false;
        } else {
            getLocationLatLang();
            return true;
        }
    }


    // ------------------------------------
    // 4 Handle Permission Result
    // ------------------------------------
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION) {
            for (int grant : grantResults) {
                if (grant == PackageManager.PERMISSION_DENIED) {
                    checkLocationPermission();
                    return;
                }
            }
            getLocationLatLang();
        }
    }


    // ------------------------------------
    // 5 Get Lat & Long from GPS
    // ------------------------------------
    public void getLocationLatLang() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            showGPSDialog();
        } else {
            GPSTracker gps = new GPSTracker(context);
            if (gps.canGetLocation()) {
                latitude = String.valueOf(gps.getLatitude());
                longitude = String.valueOf(gps.getLongitude());
            }
        }
    }
    // ------------------------------------
    // 6 Ask User to Enable GPS
    // ------------------------------------
    private void showGPSDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        View convertView = LayoutInflater.from(context).inflate(R.layout.dsb_device_not_connected, null);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.white_back);
        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);
        alertDialog.show();

        TextView tag_line = convertView.findViewById(R.id.tag_line);
        TextView device_name = convertView.findViewById(R.id.device_name);
        Button done_btn = convertView.findViewById(R.id.done_btn);
        ImageView image_set = convertView.findViewById(R.id.image_set);
        ImageView imgClose = convertView.findViewById(R.id.close);

        tag_line.setText("Location Sharing is Off!");
        device_name.setText("You need to turn your location sharing on");
        done_btn.setText("Turn Location On");

        image_set.setOnClickListener(view -> alertDialog.dismiss());
        imgClose.setOnClickListener(view -> {
            alertDialog.dismiss();

        });
        done_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

        });
    }



}


