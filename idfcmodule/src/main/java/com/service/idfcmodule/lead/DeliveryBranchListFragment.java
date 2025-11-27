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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.service.idfcmodule.adaptors.DeliveryBranchListAdapter;
import com.service.idfcmodule.databinding.FragmentDeliveryBranchListBinding;
import com.service.idfcmodule.models.BranchListModel;
import com.service.idfcmodule.myinterface.BranchItemClicked;
import com.service.idfcmodule.utils.AddressFetcherService;
import com.service.idfcmodule.utils.ConverterUtils;
import com.service.idfcmodule.utils.GPSTracker;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeliveryBranchListFragment extends Fragment {

    // for location
    private Location currentLocation;
    private final int LOCATION_PERMISSION = 101;
    String latitude = "", longitude = "", address = "", pinCode = "";

    ///////////////

    String whichButtonClicked = "";
    String strBranchId = "", strBranchName = "";


    String networkStatus = "";

    FragmentDeliveryBranchListBinding binding;

    ArrayList<BranchListModel> arrayList;

    Context context;
    Activity activity;

    String leadId = "", srNo = "", customerName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDeliveryBranchListBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        if (getArguments() != null) {
            leadId = getArguments().getString(MyConstantKey.LEAD_ID, "");
            srNo = getArguments().getString(MyConstantKey.SR_NO, "");
            customerName = getArguments().getString(MyConstantKey.CUSTOMER_NAME, "");
        }

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        getBranchList(leadId);

        clickEvents();

        return binding.getRoot();

    }

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
         //   activity.onBackPressed();

            Bundle bundle = new Bundle();
            bundle.putString(MyConstantKey.BANK_ID,LeadListFragment.bankId);
            ReplaceFragmentUtils.replaceFragment(new LeadListFragment(), bundle, (AppCompatActivity) activity);

        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.imgClear.setVisibility(View.VISIBLE);
                } else {
                    binding.imgClear.setVisibility(View.GONE);
                }
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.imgClear.setOnClickListener(v -> {
            binding.etSearch.setText("");
        });

    }

    private void filter(String text) {

        ArrayList<BranchListModel> newFilterList = new ArrayList<>();
        for (BranchListModel item : arrayList) {
            if (item.getBranchName().toLowerCase().contains(text.toLowerCase()) || item.getBranchCode().toLowerCase().contains(text.toLowerCase()) || item.getBranchAddress().toLowerCase().contains(text.toLowerCase())) {
                newFilterList.add(item);
            }

        }

        setAdapter(newFilterList);

    }

    private void setAdapter(ArrayList<BranchListModel> arrayList) {

        DeliveryBranchListAdapter adapter = new DeliveryBranchListAdapter(arrayList, context, activity);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        adapter.branchListInterface(new BranchItemClicked() {
            @Override
            public void onItemClicked(String branchId, String branchName) {

                strBranchId = branchId;
                strBranchName = branchName;

              showConfirmDialog(strBranchName);
                ConverterUtils.hideKeyboard(activity);


            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void showConfirmDialog( String branchName) {

        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.lead_confirm_dialog, null);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_back);

        TextView textHeading = convertView.findViewById(R.id.textHeading);
        TextView textMessage = convertView.findViewById(R.id.textMessage);
        TextView tvCancel = convertView.findViewById(R.id.tvCancel);

        TextView tvYes = convertView.findViewById(R.id.tvYes);

        textHeading.setText("Do you want to submit on " + branchName );

        tvCancel.setOnClickListener(v -> alertDialog.dismiss());

        tvYes.setOnClickListener(v -> {
            alertDialog.dismiss();

            whichButtonClicked = "submit";

            if (networkStatus.equalsIgnoreCase("Connected")) {

                checkPermissionAndGetLocation();

            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }



        });

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();

    }

    private void getBranchList(String leadId) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RetrofitClient.getInstance().getApi().getBranchList(leadId, retailerId)
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
                                    JSONArray dataArray = responseObject.getJSONArray("data");
                                    arrayList = new ArrayList<>();
                                    for (int i = 0; i < dataArray.length(); i++) {

                                        JSONObject dataObject = dataArray.getJSONObject(i);
                                        String branchId = dataObject.optString("id");
                                        String branchName = dataObject.optString("name");
                                        String ifsc_code = dataObject.optString("ifsc_code");
                                        String address = dataObject.optString("address");

                                        BranchListModel model = new BranchListModel(branchId, branchName, ifsc_code, address);
                                        arrayList.add(model);

                                    }

                                    setAdapter(arrayList);
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

    private void submitBranch(String leadId) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = latitude+","+longitude;

        RetrofitClient.getInstance().getApi().submitBranch(leadId, retailerId, strBranchId,geoCode)
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

                                    JSONObject dataObject = responseObject.getJSONObject("data");
                                    String mobile = dataObject.optString("mobile");
                                    String branch_name = dataObject.optString("branch_name");
                                    String ifsc_code = dataObject.optString("ifsc_code");
                                    String job_type = dataObject.optString("job_type");
                                    String jobSubType = dataObject.optString("job_subtype");
                                    String delivery = dataObject.optString("delivery");
                                    String drop_address = dataObject.optString("drop_address");

                                    Bundle bundle = new Bundle();
                                    bundle.putString(MyConstantKey.MOBILE_NO, mobile);
                                    bundle.putString(MyConstantKey.BRANCH_NAME, branch_name);
                                    bundle.putString(MyConstantKey.IFSC_CODE, ifsc_code);
                                    bundle.putString(MyConstantKey.JOB_TYPE, job_type);
                                    bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                                    bundle.putString(MyConstantKey.DELIVERY, delivery);
                                    bundle.putString(MyConstantKey.DROP_ADDRESS, drop_address);

                                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
                                    bundle.putString(MyConstantKey.SR_NO, srNo);
                                    bundle.putString(MyConstantKey.CUSTOMER_NAME, customerName);

                                    ReplaceFragmentUtils.replaceFragment(new DeliveryTrackingFragment(), bundle, (AppCompatActivity) activity);

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

    ///////////  for location

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
                    if (whichButtonClicked.equalsIgnoreCase("submit")){

                        submitBranch(leadId);

                    }
                    else{
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
        DeliveryBranchListFragment.AddressResultReceiver addressResultReceiver = new DeliveryBranchListFragment.AddressResultReceiver(new Handler());
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