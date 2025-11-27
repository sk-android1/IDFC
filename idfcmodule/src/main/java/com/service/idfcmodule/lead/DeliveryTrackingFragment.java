package com.service.idfcmodule.lead;

import static android.os.Looper.getMainLooper;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentDeliveryTrackingBinding;
import com.service.idfcmodule.utils.AddressFetcherService;
import com.service.idfcmodule.utils.ConverterUtils;
import com.service.idfcmodule.utils.GPSTracker;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeliveryTrackingFragment extends Fragment {

    String networkStatus = "";

    FragmentDeliveryTrackingBinding binding;
    Activity activity;
    Context context;

    String mobileNo = "", leadId = "", srNo = "";

    String delivery = "", branchName = "", ifscCode = "", jobSubType = "", jobType = "", dropAddress = "",customerName = "";

    // for location
    private Location currentLocation;
    private final int LOCATION_PERMISSION = 101;
    String latitude = "", longitude = "", address = "", pinCode = "";

    ///////////////

    String whichButtonClicked = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDeliveryTrackingBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        if (getArguments() != null) {

            delivery = getArguments().getString(MyConstantKey.DELIVERY, "");
            branchName = getArguments().getString(MyConstantKey.BRANCH_NAME, "");
            ifscCode = getArguments().getString(MyConstantKey.IFSC_CODE, "");
            jobType = getArguments().getString(MyConstantKey.JOB_TYPE, "");
            jobSubType = getArguments().getString(MyConstantKey.JOB_SUBTYPE, "");
            dropAddress = getArguments().getString(MyConstantKey.DROP_ADDRESS, "");
            mobileNo = getArguments().getString(MyConstantKey.MOBILE_NO, "");

            leadId = getArguments().getString(MyConstantKey.LEAD_ID, "");
            srNo = getArguments().getString(MyConstantKey.SR_NO, "");
            customerName = getArguments().getString(MyConstantKey.CUSTOMER_NAME, "");

        }

        binding.tvDelivery.setText(delivery);
        binding.tvBranchName.setText(branchName);
        binding.tvIfsc.setText(ifscCode);
        binding.tvJobType.setText(ConverterUtils.capitaliseString(jobSubType) + " " + ConverterUtils.capitaliseString(jobType));
        binding.tvDropAddress.setText(dropAddress);

        binding.tvDropTime.setText(ConverterUtils.currentDate());

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        clickEvents();

        return binding.getRoot();

    }

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(MyConstantKey.LEAD_ID, leadId);
            bundle.putString(MyConstantKey.SR_NO, srNo);

            ReplaceFragmentUtils.replaceFragment(new DeliveryBranchListFragment(), bundle, (AppCompatActivity) activity);
          //  activity.onBackPressed();
        });

        binding.imgCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + mobileNo));
            startActivity(intent);
        });

        binding.locationLy.setOnClickListener(view -> {
            NetworkUtils.addressToMap(dropAddress, context);
        });

        binding.tvCloseSr.setOnClickListener(v -> {

            whichButtonClicked = "closeSr";

            if (networkStatus.equalsIgnoreCase("Connected")) {

                checkPermissionAndGetLocation();

            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }

        });

    }

    private void closeSr(String leadId) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = latitude+","+longitude;

        RetrofitClient.getInstance().getApi().closeSr(leadId, retailerId,geoCode)
                .enqueue(new Callback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                                boolean status = responseObject.getBoolean("status");
                                int statusCode = responseObject.getInt("statuscode");
                                String message = responseObject.getString("message");

                                if (statusCode == 200) {

                                    JSONObject dataObj = responseObject.getJSONObject("data");

                                    String timer = dataObj.optString("timer");

                                    Bundle bundle = new Bundle();
                                    bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
                                    bundle.putString(MyConstantKey.SR_NO, srNo);
                                    bundle.putString(MyConstantKey.TIMER, timer);
                                    bundle.putString(MyConstantKey.CUSTOMER_NAME, customerName);

                                    ReplaceFragmentUtils.replaceFragment(new CloseSrAcceptanceFragment(), bundle, (AppCompatActivity) activity);

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();

                                    pDialog.dismiss();
                                } else {
                                    MyErrorDialog.nonFinishErrorDialog(context, message);
                                    pDialog.dismiss();
                                }

                            } catch (JSONException e) {
                                MyErrorDialog.somethingWentWrongDialog(getActivity());
                                pDialog.dismiss();
                            }
                        } else {
                            MyErrorDialog.somethingWentWrongDialog(getActivity());
                            pDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        MyErrorDialog.somethingWentWrongDialog(getActivity());
                        pDialog.dismiss();
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();

//        if (networkStatus.equalsIgnoreCase("Connected")) {
//            checkPermissionAndGetLocation();
//        }
//        else {
//            MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
//        }
    }



    ////////////////  for location

    public void checkPermissionAndGetLocation() {

        if (checkLocationPermission()) {
            getLocationLatLang();
        }
        else {
            Snackbar.make(binding.mainLayout, "Please allow location permission", Snackbar.LENGTH_LONG).show();
        }
    }

    public void startGettingLocation() {
        FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.i("TAG", "Location result is available");
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (locationAvailability.isLocationAvailable()) {
                    Log.i("TAG", "Location is available");
                } else {
                    Log.i("TAG", "Location is unavailable");
                }
            }
        };

        if (checkLocationPermission()) {
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Location location) {
                    currentLocation = location;
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    Log.d("TAG", "onSuccess: " + currentLocation);
                    getAddress();
                }
            });

            locationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("tag", e.getMessage());
                }
            });

        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
            return false;
        } else {
            //    getLocationLatLang();
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION) {
            for (int grantRes : grantResults) {
                if (grantRes == PackageManager.PERMISSION_DENIED) {
                    checkLocationPermission();
                    return;
                }
            }

            getLocationLatLang();
        }
    }

    public void getLocationLatLang() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        GPSTracker gps = new GPSTracker(context);
        if (isGPSEnabled) {
            if (gps.canGetLocation()) {
                latitude = String.valueOf(gps.getLatitude());
                longitude = String.valueOf(gps.getLongitude());

                if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
                    checkPermissionAndGetLocation();
                } else {
                    if (whichButtonClicked.equalsIgnoreCase("closeSr")) {
                        closeSr(leadId);
                    }
                    else {
                        Snackbar.make(binding.mainLayout, "Other button clicked", Snackbar.LENGTH_LONG).show();
                    }

                }

            }
        }
        else {
            OnGPS();
        }
    }

    @SuppressLint("SetTextI18n")
    private void OnGPS() {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        View convertView = getLayoutInflater().inflate(R.layout.dsb_device_not_connected, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();

        TextView tag_line = convertView.findViewById(R.id.tag_line);
        TextView device_name = convertView.findViewById(R.id.device_name);
        Button done_btn = convertView.findViewById(R.id.done_btn);
        ImageView image_set = convertView.findViewById(R.id.image_set);

        ImageView image_close = convertView.findViewById(R.id.close);

        image_close.setOnClickListener(view -> {
            alertDialog.dismiss();
        });

        image_set.setOnClickListener(view -> {
            alertDialog.dismiss();
        });

        tag_line.setText("Location Sharing is Off!");
        device_name.setText("You need to turn your location sharing on");
        done_btn.setText("Turn Location On");
        done_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        });

    }

    private void getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(context, "GeoCoder Not Found", Toast.LENGTH_SHORT).show();
        } else {
            startAddressFetcherService();
        }
    }

    private void startAddressFetcherService() {
        Intent intent = new Intent(context, AddressFetcherService.class);
        DeliveryTrackingFragment.AddressResultReceiver addressResultReceiver = new DeliveryTrackingFragment.AddressResultReceiver(new Handler());
        intent.putExtra(MyConstantKey.RECEIVER, addressResultReceiver);
        intent.putExtra(MyConstantKey.LOCATION_DATA_EXTRA, currentLocation);
        context.startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) {
                address = resultData.getString(MyConstantKey.RESULT_DATA_KEY);
                pinCode = resultData.getString(MyConstantKey.PINCODE);

            }

        }

    }

    ////////////////////////////////

}