package com.service.idfcmodule.lead;

import static android.os.Looper.getMainLooper;
import static com.service.idfcmodule.IdfcMainActivity.appVersion;
import static com.service.idfcmodule.IdfcMainActivity.comType;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;
import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.service.idfcmodule.adaptors.LeadListAdapter;
import com.service.idfcmodule.databinding.FragmentLeadListBinding;
import com.service.idfcmodule.models.LeadModel;
import com.service.idfcmodule.myinterface.LeadItemClicked;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LeadListFragment extends Fragment {

    String networkStatus = "";
    FragmentLeadListBinding binding;

    Context context;
    Activity activity;
    ArrayList<LeadModel> leadList;

    public static String bankId;

    // for location
    private Location currentLocation;
    private final int LOCATION_PERMISSION = 101;
    String latitude = "", longitude = "", address = "", pinCode = "";

    ///////////////

    String whichButtonClicked = "";
    String strLeadId = "", strAmount = "", strCount = "", strQrverify = "", strStage = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLeadListBinding.inflate(inflater);

        if (comType.equalsIgnoreCase("Vidcom")) {
            ImageViewCompat.setImageTintList(binding.leadTopLy.imgBack, ColorStateList.valueOf(getResources().getColor(R.color.vidcom_color)));
        }

        context = requireContext();
        activity = requireActivity();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        //   checkPermissionAndGetLocation();

        if (getArguments() != null) {

            bankId = getArguments().getString(MyConstantKey.BANK_ID, "");

        }

        getLeadList("0");

        clickEvents();

        return binding.getRoot();

    }

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
            activity.onBackPressed();
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

        binding.tvPending.setOnClickListener(v -> {

//            if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//
//            //    MyErrorDialog.nonFinishErrorDialog(context, "Location not found try again.");
//                Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
//                getLocationLatLang();
//            } else {
//
//                getLeadList("0");
//            }

            getLeadList("0");

            binding.tvPending.setBackgroundResource(R.drawable.rounded_green);
            binding.tvPending.setTextColor(getResources().getColor(R.color.white));
            binding.tvAllocated.setBackgroundResource(R.drawable.rounded_border);
            binding.tvAllocated.setTextColor(getResources().getColor(R.color.grey_idfc));
            binding.tvCompleted.setBackgroundResource(R.drawable.rounded_border);
            binding.tvCompleted.setTextColor(getResources().getColor(R.color.grey_idfc));
        });

        binding.tvAllocated.setOnClickListener(v -> {
//            if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//
//            //    MyErrorDialog.nonFinishErrorDialog(context, "Location not found try again.");
//                Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
//               checkPermissionAndGetLocation();
//            } else {
//                getLeadList("1");
//            }

            getLeadList("1");

            binding.tvPending.setBackgroundResource(R.drawable.rounded_border);
            binding.tvPending.setTextColor(getResources().getColor(R.color.grey_idfc));
            binding.tvAllocated.setBackgroundResource(R.drawable.rounded_green);
            binding.tvAllocated.setTextColor(getResources().getColor(R.color.white));
            binding.tvCompleted.setBackgroundResource(R.drawable.rounded_border);
            binding.tvCompleted.setTextColor(getResources().getColor(R.color.grey_idfc));
        });

        binding.tvCompleted.setOnClickListener(v -> {
            getLeadList("2");
            binding.tvPending.setBackgroundResource(R.drawable.rounded_border);
            binding.tvPending.setTextColor(getResources().getColor(R.color.grey_idfc));
            binding.tvAllocated.setBackgroundResource(R.drawable.rounded_border);
            binding.tvAllocated.setTextColor(getResources().getColor(R.color.grey_idfc));
            binding.tvCompleted.setBackgroundResource(R.drawable.rounded_green);
            binding.tvCompleted.setTextColor(getResources().getColor(R.color.white));
        });
    }

    private void filter(String text) {

        ArrayList<LeadModel> newFilterList = new ArrayList<>();
        for (LeadModel item : leadList) {
            if (item.getSrNo().toLowerCase().contains(text.toLowerCase()) || item.getDate().toLowerCase().contains(text.toLowerCase())) {
                newFilterList.add(item);
            }

        }

        setAdapter(newFilterList);

    }

    private void getLeadList(String leadStatus) {

        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().getLeadList(bankId, retailerId, leadStatus).enqueue(new Callback<JsonObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {

                            JSONArray dataArray = responseObject.getJSONArray("data");

                            leadList = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObject = dataArray.getJSONObject(i);
                                String leadId = dataObject.optString("id");
                                String srNo = dataObject.optString("sr_number");
                                String jobId = dataObject.optString("job_id");
                                String jobType = dataObject.optString("job_type");
                                String jobSubType = dataObject.optString("job_subtype");
                                String status = dataObject.optString("status");
                                String count = dataObject.optString("count");
                                String amount = dataObject.optString("amount");
                                String documentType = dataObject.optString("document_type");
                                String stage = dataObject.optString("stage");
                                String mobileNo = dataObject.optString("mobile");

                                JSONObject pickUpObj = dataObject.getJSONObject("pickup");
                                String pickupAddress = pickUpObj.optString("address");
                                String pickupState = pickUpObj.optString("state");
                                String pickupCity = pickUpObj.optString("city");
                                String pickupPincode = pickUpObj.optString("pincode");

                                String latLongi = pickUpObj.optString("gps_coordinates");

                                String lat = "", longi = "";

                                if (!latLongi.equalsIgnoreCase("")) {
                                    String[] latLongArr = latLongi.split(",");
                                    lat = latLongArr[0];
                                    longi = latLongArr[1];
                                }

                                String address = pickupAddress + ", " + pickupCity + "\n" + pickupState + "(" + pickupPincode + ")";
                                //  String address =   "xxxxxxxxxxxxx, " + pickupCity + "\n" + pickupState + "(" + pickupPincode + ")";

                                String date = dataObject.optString("date_of_visit");
                                String timeFrom = dataObject.optString("time_of_visit_from");
                                String timeTo = dataObject.optString("time_of_visit_to");
                                String createdDate = dataObject.optString("created_at");

//                                String[] dateArray = date.split("T");
//                                date = dateArray[0];
//                                String time = dateArray[1];

                                try {
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    Date dt = inputFormat.parse(date);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
                                    date = outputFormat.format(dt);
                                } catch (Exception ignored) {
                                }

                                try {
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm:ss");
                                    Date dt = inputFormat.parse(timeFrom);
                                    Date dt2 = inputFormat.parse(timeTo);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
                                    timeFrom = outputFormat.format(dt);
                                    timeTo = outputFormat.format(dt2);
                                } catch (Exception ignored) {
                                }

                                Date c = Calendar.getInstance().getTime();

                                SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                String currentDate = df.format(c);

                                String branchMobile = dataObject.optString("branch_mobile");
                                String branch_name = dataObject.optString("branch_name");
                                String ifsc_code = dataObject.optString("ifsc_code");
                                String delivery = dataObject.optString("delivery");
                                String drop_address = dataObject.optString("drop_address");
                                String distance = dataObject.optString("distance");
                                String timer = dataObject.optString("timer");

                                String qrVerify = dataObject.optString("qr_verify");
                                String customerName = dataObject.optString("contact_person");

                                LeadModel leadModel = new LeadModel();
                                leadModel.setLeadId(leadId);
                                leadModel.setJobId(jobId);
                                leadModel.setSrNo(srNo);
                                leadModel.setJobType(jobType);
                                leadModel.setJobSubType(jobSubType);
                                leadModel.setPickUpAddress(address);
                                leadModel.setDate(date);
                                leadModel.setTimeFrom(timeFrom);
                                leadModel.setTimeTo(timeTo);
                                leadModel.setCount(count);
                                leadModel.setStatus(status);
                                leadModel.setAmount(amount);
                                leadModel.setDocumentType(documentType);
                                leadModel.setStage(stage);
                                leadModel.setLat(lat);
                                leadModel.setLongi(longi);

                                leadModel.setBranchMobile(branchMobile);
                                leadModel.setBranchName(branch_name);
                                leadModel.setDistance(distance);
                                leadModel.setIfscCode(ifsc_code);
                                leadModel.setDelivery(delivery);
                                leadModel.setDropAddress(drop_address);

                                leadModel.setCurrentDate(currentDate);
                                leadModel.setLeadStatus(status);
                                leadModel.setTimer(timer);
                                leadModel.setMobile(mobileNo);
                                leadModel.setCreatedAt(createdDate);

                                leadModel.setQrVerify(qrVerify);
                                leadModel.setCustomerName(customerName);

                                leadList.add(leadModel);

                            }

                            if (!leadList.isEmpty()) {
                                setAdapter(leadList);
                            } else {
                                binding.recyclerView.setVisibility(View.GONE);
                                binding.noDataFoundLy.setVisibility(View.VISIBLE);
                                binding.tvNoDataFound.setText("No Lead Found");
                            }

                            pd.dismiss();

                        } else {
                            pd.dismiss();
                            MyErrorDialog.nonFinishErrorDialog(context, message);
                            binding.recyclerView.setVisibility(View.GONE);
                            binding.noDataFoundLy.setVisibility(View.VISIBLE);
                            binding.tvNoDataFound.setText("No Lead Found");

                        }

                    } catch (JSONException e) {
                        pd.dismiss();
                        MyErrorDialog.somethingWentWrongDialog(activity);
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.noDataFoundLy.setVisibility(View.VISIBLE);
                        binding.tvNoDataFound.setText("No Lead Found");
                    }
                } else {
                    pd.dismiss();
                    MyErrorDialog.somethingWentWrongDialog(activity);
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.noDataFoundLy.setVisibility(View.VISIBLE);
                    binding.tvNoDataFound.setText("No Lead Found");
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pd.dismiss();
                MyErrorDialog.somethingWentWrongDialog(activity);
                binding.recyclerView.setVisibility(View.GONE);
                binding.noDataFoundLy.setVisibility(View.VISIBLE);
                binding.tvNoDataFound.setText("No Lead Found");
            }
        });

    }

    private void assignLead() {

        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().allocatedLead(strLeadId, retailerId, geoCode).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {

                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {
                            Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();

                            getLeadList("1");
                            binding.tvPending.setBackgroundResource(R.drawable.rounded_border);
                            binding.tvPending.setTextColor(getResources().getColor(R.color.grey_idfc));
                            binding.tvAllocated.setBackgroundResource(R.drawable.rounded_green);
                            binding.tvAllocated.setTextColor(getResources().getColor(R.color.white));
                            binding.tvCompleted.setBackgroundResource(R.drawable.rounded_border);
                            binding.tvCompleted.setTextColor(getResources().getColor(R.color.grey_idfc));

                            pd.dismiss();
                        } else {
                            MyErrorDialog.nonFinishErrorDialog(context, message);
                            pd.dismiss();
                        }

                    } catch (JSONException e) {
                        pd.dismiss();
                        MyErrorDialog.somethingWentWrongDialog(activity);
                    }
                } else {
                    pd.dismiss();
                    MyErrorDialog.somethingWentWrongDialog(activity);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pd.dismiss();
                MyErrorDialog.somethingWentWrongDialog(activity);
            }
        });
    }

    private void startJourney() {

        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().startJourney(strLeadId, retailerId, geoCode).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {

                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {

                            JSONObject dataObject = responseObject.getJSONObject("data");
                            String jobId = dataObject.optString("job_id");
                            String srNo = dataObject.optString("sr_number");
                            String mobile = dataObject.optString("mobile");
                            String customer_name = dataObject.optString("customer_name");
                            String job_type = dataObject.optString("job_type");
                            String jobSubType = dataObject.optString("job_subtype");
                            String delivery = dataObject.optString("delivery");
                            String pickup_address = dataObject.optString("pickup_address");
                            String pickup_time = dataObject.optString("pickup_time");
                            String createdAt = dataObject.optString("created_at");

                            Bundle bundle = new Bundle();
                            bundle.putString(MyConstantKey.JOB_ID, jobId);
                            bundle.putString(MyConstantKey.SR_NO, srNo);
                            bundle.putString(MyConstantKey.MOBILE_NO, mobile);
                            bundle.putString(MyConstantKey.CUSTOMER_NAME, customer_name);
                            bundle.putString(MyConstantKey.JOB_TYPE, job_type);
                            bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                            bundle.putString(MyConstantKey.DELIVERY, delivery);
                            bundle.putString(MyConstantKey.PICKUP_ADDRESS, pickup_address);
                            //   bundle.putString(MyConstantKey.PICKUP_TIME, pickup_time);
                            bundle.putString(MyConstantKey.PICKUP_TIME, createdAt);

                            bundle.putString(MyConstantKey.AMOUNT, strAmount);
                            bundle.putString(MyConstantKey.COUNT, strCount);
                            bundle.putString(MyConstantKey.STAGE, strStage);
                            bundle.putString(MyConstantKey.LEAD_ID, strLeadId);

                            bundle.putString(MyConstantKey.QRVERIFY, strQrverify);

                            ReplaceFragmentUtils.replaceFragment(new PickupTrackingFragment(), bundle, (AppCompatActivity) activity);

                            pd.dismiss();
                            Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();

                        } else {

                            if (message.equalsIgnoreCase("Lead is not Allocated yet.")) {
                                MyErrorDialog.activityFinishErrorDialog(context, activity, message);
                            } else {
                                MyErrorDialog.nonFinishErrorDialog(context, message);
                            }

                            pd.dismiss();
                        }

                    } catch (JSONException e) {
                        pd.dismiss();
                        MyErrorDialog.somethingWentWrongDialog(activity);
                    }
                } else {
                    pd.dismiss();
                    MyErrorDialog.somethingWentWrongDialog(activity);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pd.dismiss();
                MyErrorDialog.somethingWentWrongDialog(activity);
            }
        });

    }

    private void setAdapter(ArrayList<LeadModel> arrayList) {

        binding.noDataFoundLy.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);

        LeadListAdapter adapter = new LeadListAdapter(arrayList, context, activity);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        adapter.MyInterface(new LeadItemClicked() {
            @Override
            public void onItemClicked(String type, String leadId, String stage, String amount, String count, String qrVerify) {

                ConverterUtils.hideKeyboard(activity);

                strLeadId = leadId;
                strAmount = amount;
                strCount = count;
                strQrverify = qrVerify;
                strStage = stage;

                if (type.equalsIgnoreCase("accept")) {
                    whichButtonClicked = "accept";
                    checkPermissionAndGetLocation();

//                    if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//
//                       /// MyErrorDialog.nonFinishErrorDialog(context, "Location not found try again.");
//                        Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
//                        checkPermissionAndGetLocation();
//                    } else {
//                      //  startGettingLocation();
//                        assignLead(leadId);
//                    }

                } else if (type.equalsIgnoreCase("cancelRequest")) {

                    whichButtonClicked = "cancelRequest";
                    checkPermissionAndGetLocation();

//                    if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//                        Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
//                        checkPermissionAndGetLocation();
//                    } else {
//
//                        getRemarkList(context, activity,leadId,retailerId,latitude,longitude);
//                    }


                } else if (type.equalsIgnoreCase("proceedToJourney")) {

                    whichButtonClicked = "proceedToJourney";
                    checkPermissionAndGetLocation();

//                    if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
//                        Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
//                        checkPermissionAndGetLocation();
//                    } else {
//                        startJourney(leadID);
//                    }

                }

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        // if (networkStatus.equalsIgnoreCase("Connected")) {
        //   if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {

        //  MyErrorDialog.nonFinishErrorDialog(context, "Location not found try again.");
        //   getLocationLatLang();
        //  Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
        //
//            } else {
//
//                return;
//
//              //  getLeadList("0");
//            }
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
                    //  Snackbar.make(binding.mainLayout, "Location not found try again.", Snackbar.LENGTH_LONG).show();
                    checkPermissionAndGetLocation();
                } else {

                    if (whichButtonClicked.equalsIgnoreCase("accept")) {
                        assignLead();
                    } else if (whichButtonClicked.equalsIgnoreCase("cancelRequest")) {
                        getRemarkList(context, activity, strLeadId, retailerId, latitude, longitude);
                    } else if (whichButtonClicked.equalsIgnoreCase("proceedToJourney")) {
                        startJourney();
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
        LeadListFragment.AddressResultReceiver addressResultReceiver = new LeadListFragment.AddressResultReceiver(new Handler());
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
