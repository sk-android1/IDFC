package com.service.idfcmodule.lead;

import static android.app.Activity.RESULT_OK;
import static android.os.Looper.getMainLooper;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;
import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;

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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentPickupTrackingBinding;
import com.service.idfcmodule.models.BadRequestHandle;
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

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PickupTrackingFragment extends Fragment {

    FragmentPickupTrackingBinding binding;

    Context context;
    Activity activity;

    String networkStatus = "";

    String amount, count, leadId;

    String jobId = "", sr_no = "";

    String mobile = "", customerName = "";

    String job_type, jobSubType, delivery, pickup_address, pickup_time, qrVerify, stage;

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

        binding = FragmentPickupTrackingBinding.inflate(inflater);

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        context = requireContext();
        activity = requireActivity();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        if (getArguments() != null) {

            jobId = getArguments().getString(MyConstantKey.JOB_ID, "");
            sr_no = getArguments().getString(MyConstantKey.SR_NO, "");
            mobile = getArguments().getString(MyConstantKey.MOBILE_NO, "");
            customerName = getArguments().getString(MyConstantKey.CUSTOMER_NAME, "");

            job_type = getArguments().getString(MyConstantKey.JOB_TYPE, "");
            jobSubType = getArguments().getString(MyConstantKey.JOB_SUBTYPE, "");
            delivery = getArguments().getString(MyConstantKey.DELIVERY, "");
            pickup_address = getArguments().getString(MyConstantKey.PICKUP_ADDRESS, "");
            pickup_time = getArguments().getString(MyConstantKey.PICKUP_TIME, "");

            amount = getArguments().getString(MyConstantKey.AMOUNT, "");
            count = getArguments().getString(MyConstantKey.COUNT, "");
            leadId = getArguments().getString(MyConstantKey.LEAD_ID, "");

            qrVerify = getArguments().getString(MyConstantKey.QRVERIFY, "");
            stage = getArguments().getString(MyConstantKey.STAGE, "");

        }

        binding.tvCustomerName.setText(customerName);
        binding.tvJobId.setText("SR - " + sr_no);
        binding.tvJobType.setText(ConverterUtils.capitaliseString(jobSubType) + " " + ConverterUtils.capitaliseString(job_type));
        binding.tvDelivery.setText(delivery);
        binding.tvAPickupAddress.setText(pickup_address);
        binding.tvTime.setText(pickup_time);

        if (qrVerify.equalsIgnoreCase("0") && stage.equalsIgnoreCase("2")) {
            binding.tvReachedLocation.setVisibility(View.GONE);
            binding.tvScanQR.setVisibility(View.VISIBLE);
            binding.tvResendQR.setVisibility(View.VISIBLE);
        } else {
            binding.tvReachedLocation.setVisibility(View.VISIBLE);
            binding.tvScanQR.setVisibility(View.GONE);
            binding.tvResendQR.setVisibility(View.GONE);
        }

        clickEvents();

        return binding.getRoot();

    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {

                Intent data = result.getData();
                assert data != null;
                String scannedData = data.getStringExtra("scannedData");
                String geoCode = data.getStringExtra("geoCode");

                //    showScannedSuccessDialog(scannedData);

                String strUrl = scannedData + "/" + geoCode;
                getUrlResponse(strUrl);

            }
        }
    });

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
            //   activity.onBackPressed();
            //  requireActivity().finish();

            Bundle bundle = new Bundle();
            bundle.putString(MyConstantKey.BANK_ID, LeadListFragment.bankId);
            ReplaceFragmentUtils.replaceFragment(new LeadListFragment(), bundle, (AppCompatActivity) activity);

        });

        binding.imgCall.setOnClickListener(v -> {

            callLogApi();

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + mobile));
            startActivity(intent);

        });

        binding.locationLy.setOnClickListener(view -> {
            NetworkUtils.addressToMap(pickup_address, context);
        });

        binding.tvScanQR.setOnClickListener(view -> {
            Intent intent = new Intent(context, MyScannerNew.class);
            intent.putExtra(MyConstantKey.LEAD_ID, leadId);
            resultLauncher.launch(intent);
        });

        binding.tvResendQR.setOnClickListener(view -> {

            whichButtonClicked = "resendQr";

            if (networkStatus.equalsIgnoreCase("Connected")) {

                checkPermissionAndGetLocation();

//                    if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//                        Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
//                        checkPermissionAndGetLocation();
//                    } else {
//                       retrySms();
//                    }


            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }
        });

        binding.tvReachedLocation.setOnClickListener(v -> {

            whichButtonClicked = "reachedLocation";

            if (networkStatus.equalsIgnoreCase("Connected")) {

                checkPermissionAndGetLocation();

//                    if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//                        Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
//                        checkPermissionAndGetLocation();
//                    } else {
//                       reachedLocation();
//                    }


            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }
        });

        binding.tvCancelReq.setOnClickListener(view -> {
            whichButtonClicked = "cancelRequest";

            if (networkStatus.equalsIgnoreCase("Connected")) {

                checkPermissionAndGetLocation();

            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }

//                if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//                    Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
//                    checkPermissionAndGetLocation();
//                } else {
//                    getRemarkList(context, activity,leadId,retailerId,latitude,longitude);
//                }

        });

    }

    private void reachedLocation() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().reachedLocation(leadId, retailerId, geoCode, address)
                .enqueue(new Callback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                                boolean status = responseObject.getBoolean("status");
                                String message = responseObject.getString("message");
                                int statusCode = responseObject.getInt("statuscode");

                                if (statusCode == 200) {

                                    Intent intent = new Intent(context, MyScannerNew.class);
                                    intent.putExtra(MyConstantKey.LEAD_ID, leadId);
                                    resultLauncher.launch(intent);

                                    binding.tvReachedLocation.setVisibility(View.GONE);
                                    binding.tvScanQR.setVisibility(View.VISIBLE);
                                    binding.tvResendQR.setVisibility(View.VISIBLE);

                                    //     showScannerDialog();

                                    pDialog.dismiss();

                                } else {

                                    if (message.equalsIgnoreCase("Lead is not Allocated yet.") || message.equalsIgnoreCase("Lead is already Allocated") || message.equalsIgnoreCase("Lead is already cancelled.")) {
                                        MyErrorDialog.activityFinishErrorDialog(context, activity, message);
                                    } else {
                                        MyErrorDialog.nonFinishErrorDialog(context, message);
                                    }

                                    pDialog.dismiss();

                                }

                            } catch (JSONException e) {

                                MyErrorDialog.somethingWentWrongDialog(getActivity());
                                pDialog.dismiss();

                            }

                        } else {
                            if (response.code() == 400) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    BadRequestHandle errorResponse = new Gson().fromJson(errorBody, BadRequestHandle.class);
                                    MyErrorDialog.nonFinishErrorDialog(context, errorResponse.getMessage());

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            } else {
                                MyErrorDialog.somethingWentWrongDialog(getActivity());
                            }

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

    private void retrySms() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().retrySms(leadId, retailerId, geoCode)
                .enqueue(new Callback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                                boolean status = responseObject.getBoolean("status");
                                String message = responseObject.getString("message");
                                int statusCode = responseObject.getInt("statuscode");

                                if (statusCode == 200) {
                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).setAction("Scan SR", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    }).show();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(context, MyScannerNew.class);
                                            intent.putExtra(MyConstantKey.LEAD_ID, leadId);
                                            resultLauncher.launch(intent);
                                        }
                                    }, 1000);

                                    pDialog.dismiss();

                                } else {

                                    MyErrorDialog.nonFinishErrorDialog(context, message);
                                    pDialog.dismiss();

                                }

                            } catch (JSONException e) {
                                MyErrorDialog.somethingWentWrongDialog(activity);
                                pDialog.dismiss();
                            }

                        } else {
                            if (response.code() == 400) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    BadRequestHandle errorResponse = new Gson().fromJson(errorBody, BadRequestHandle.class);
                                    MyErrorDialog.nonFinishErrorDialog(context, errorResponse.getMessage());

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            } else {
                                MyErrorDialog.somethingWentWrongDialog(activity);
                            }

                            pDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        MyErrorDialog.somethingWentWrongDialog(activity);
                        pDialog.dismiss();
                    }
                });

    }

    private void callLogApi() {

//        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
//        pd.show();

        RetrofitClient.getInstance().getApi().callLog(leadId, retailerId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (response.isSuccessful()) {

                    try {
                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));

                        int statusCode = responseObject.getInt("statuscode");
                        String message = responseObject.getString("message");

//                        if (statusCode == 200) {
//
//                            Intent intent = new Intent(Intent.ACTION_DIAL);
//                            intent.setData(Uri.parse("tel:" + mobile));
//                            startActivity(intent);
//
//                            pd.dismiss();
//
//                        } else {
//                            pd.dismiss();
//                            MyErrorDialog.nonFinishErrorDialog(context, message);
//                        }

                    } catch (JSONException e) {
//                        pd.dismiss();
//                        MyErrorDialog.nonFinishErrorDialog(context, "Something went wrong");
                    }

                } else {
//                    pd.dismiss();
//                    MyErrorDialog.nonFinishErrorDialog(context, "Something went wrong");
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
//                pd.dismiss();
//                MyErrorDialog.nonFinishErrorDialog(context, "Something went wrong");

            }
        });

    }

    private void getUrlResponse(String url) {

        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        RetrofitClient.getInstance().getApi().verifyUserByUrl(url).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (response.isSuccessful()) {

                    try {
                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));

                        int statusCode = responseObject.getInt("statuscode");
                        String message = responseObject.getString("message");

                        if (statusCode == 200) {

                            JSONObject dataObject = responseObject.getJSONObject("data");
                            String name = dataObject.optString("name");
                            String type = dataObject.optString("type");
                            //    String count = dataObject.optString("count");

                            showScannedSuccessDialog(type);

//                            if (type.equalsIgnoreCase("cheque")){
//                                Bundle bundle = new Bundle();
//                                bundle.putString("count", count);
//                                bundle.putString("jobId", jobId);
//                                bundle.putString("leadId", leadId);
//                                ReplaceFragmentUtils.replaceFragment(new DeliveredDocumentUploadFragment(), bundle, (AppCompatActivity) activity);
//                            } else if (type.equalsIgnoreCase("cash")) {
//                                Bundle bundle = new Bundle();
//                                bundle.putString("amount", amount);
//                                bundle.putString("leadId", leadId);
//                                ReplaceFragmentUtils.replaceFragment(new CashCalculateFragment(), bundle, (AppCompatActivity) activity);
//                            }

                            pd.dismiss();

                        } else {
                            pd.dismiss();
                            MyErrorDialog.nonFinishErrorDialog(context, message);
                        }

                    } catch (JSONException e) {
                        pd.dismiss();
                        MyErrorDialog.nonFinishErrorDialog(context, "Something went wrong");
                    }

                } else {
                    pd.dismiss();
                    MyErrorDialog.nonFinishErrorDialog(context, "Something went wrong");
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pd.dismiss();
                MyErrorDialog.nonFinishErrorDialog(context, "Something went wrong");
            }
        });

    }

    private void showScannerDialog() {
        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        View view = LayoutInflater.from(context).inflate(R.layout.scanner_dialog, null);
        alertDialog.setView(view);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void showScannedSuccessDialog(String type) {

        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.scanned_success_dialog, null);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_back);

        TextView tvYes = convertView.findViewById(R.id.tvNext);
        tvYes.setOnClickListener(v -> {
            alertDialog.dismiss();

            //    getUrlResponse(scannedData);

            if (type.equalsIgnoreCase("cheque")) {
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.COUNT, count);
                bundle.putString(MyConstantKey.AMOUNT, amount);
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.JOB_ID, jobId);
                bundle.putString(MyConstantKey.SR_NO, sr_no);
                bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                bundle.putString(MyConstantKey.CUSTOMER_NAME, customerName);

                bundle.putString(MyConstantKey.REATTEMPT, "0");
                //   ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragmentNew(), bundle, (AppCompatActivity) activity);
                //   ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragment(), bundle, (AppCompatActivity) activity);
                ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragmentNew2(), bundle, (AppCompatActivity) activity);
            } else if (type.equalsIgnoreCase("cash")) {
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.AMOUNT, amount);
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.JOB_ID, jobId);
                bundle.putString(MyConstantKey.SR_NO, sr_no);
                bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                bundle.putString(MyConstantKey.CUSTOMER_NAME, customerName);
                bundle.putString(MyConstantKey.REATTEMPT, "0");
                ReplaceFragmentUtils.replaceFragment(new CashCalculateFragment(), bundle, (AppCompatActivity) activity);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.COUNT, count);
                bundle.putString(MyConstantKey.AMOUNT, amount);
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.JOB_ID, jobId);
                bundle.putString(MyConstantKey.SR_NO, sr_no);
                bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                bundle.putString(MyConstantKey.CUSTOMER_NAME, customerName);
                bundle.putString(MyConstantKey.REATTEMPT, "0");
                ReplaceFragmentUtils.replaceFragment(new DeliveredDocumentUploadFragment(), bundle, (AppCompatActivity) activity);
                //   ReplaceFragmentUtils.replaceFragment(new DeliveredDocumentUploadFragmentNew(), bundle, (AppCompatActivity) activity);

            }

        });

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();

    }

    @Override
    public void onResume() {
        super.onResume();

//        if (networkStatus.equalsIgnoreCase("Connected")) {
//            checkPermissionAndGetLocation();
//        }
//        else {
//            MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
//        }


    }

    ///////////  for location

    public void checkPermissionAndGetLocation() {

        if (checkLocationPermission()) {

            getLocationLatLang();

//            if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//                //  MyErrorDialog.nonFinishErrorDialog(context, "Location not found try again.");
//                getLocationLatLang();
//            } else {
//                startGettingLocation();
//            }
        } else {
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
                    //   Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
                    checkPermissionAndGetLocation();
                } else {
                    if (whichButtonClicked.equalsIgnoreCase("resendQr")) {
                        retrySms();
                    } else if (whichButtonClicked.equalsIgnoreCase("reachedLocation")) {
                        reachedLocation();
                    } else if (whichButtonClicked.equalsIgnoreCase("cancelRequest")) {
                        getRemarkList(context, activity, leadId, retailerId, latitude, longitude);
                    } else {
                        Snackbar.make(binding.mainLayout, "Other button clicked", Snackbar.LENGTH_LONG).show();
                    }

                }

            }
        } else {
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
        PickupTrackingFragment.AddressResultReceiver addressResultReceiver = new PickupTrackingFragment.AddressResultReceiver(new Handler());
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
