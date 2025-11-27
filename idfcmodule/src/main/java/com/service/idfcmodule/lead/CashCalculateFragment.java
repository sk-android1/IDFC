package com.service.idfcmodule.lead;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.CashBottomDialogBinding;
import com.service.idfcmodule.databinding.DynamicCounterfeitAddBinding;
import com.service.idfcmodule.databinding.FragmentCashCalculateBinding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.utils.AddressFetcherService;
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


public class CashCalculateFragment extends Fragment {

    String networkStatus = "";

    FragmentCashCalculateBinding binding;

    Activity activity;
    Context context;

    int fiveHundredAmount = 0, twoHundredAmount = 0, hundredAmount = 0, fiftyAmount = 0, twentyAmount = 0, tenAmount = 0, fiveAmount = 0;

    int twentyCoinAmount = 0, tenCoinAmount = 0, fiveCoinAmount = 0, twoCoinAmount = 0, oneCoinAmount = 0;

    double totalAmount = 0.0;
    double deliverAmount = 0.0;
    double totalRupees = 0.0;
    double totalCoin = 0.0;

    String leadId = "";

    String jobId,srNo, jobSubType, amount,customerName = "";

    boolean isFiveHunClicked = true;
    boolean isTwoHunClicked = true;
    boolean isHunClicked = true;
    boolean isFiftyClicked = true;
    boolean isTwentyClicked = true;
    boolean isTenClicked = true;
    boolean isFiveClicked = true;

    int fiveHunSerialNo = 1;
    int twoHunSerialNo = 1;
    int hunSerialNo = 1;
    int fiftySerialNo = 1;
    int twentySerialNo = 1;
    int tenSerialNo = 1;
    int fiveSerialNo = 1;

    // for location
    private Location currentLocation;
    private final int LOCATION_PERMISSION = 101;
    String latitude = "", longitude = "", address = "", pinCode = "";
    boolean isButtonClicked = false;
    ///////////////

    String whichButtonClicked = "";
    String strJsonAmount = "";

    String reAttempt = "0";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCashCalculateBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        if (getArguments() != null) {
            jobId = getArguments().getString(MyConstantKey.JOB_ID,"");
            srNo = getArguments().getString(MyConstantKey.SR_NO,"");
            leadId = getArguments().getString(MyConstantKey.LEAD_ID,"");
            amount = getArguments().getString(MyConstantKey.AMOUNT,"");
            jobSubType = getArguments().getString(MyConstantKey.JOB_SUBTYPE,"");
            customerName = getArguments().getString(MyConstantKey.CUSTOMER_NAME,"");

            reAttempt = getArguments().getString(MyConstantKey.REATTEMPT, "0");
        }

        binding.tvJobId.setText("SR - "+srNo);
        binding.tvAmount.setText(amount);

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        clickEvents();

        return binding.getRoot();

    }

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
            activity.onBackPressed();

        });

        binding.tvUpdateAmt.setOnClickListener(v -> {
            showUpdateAmountDialog();

        });

        binding.imgRupeesUp.setOnClickListener(view -> {
            binding.totalRupeesLy.setVisibility(View.GONE);
            binding.imgRupeesUp.setVisibility(View.GONE);
            binding.imgRupeesDown.setVisibility(View.VISIBLE);
        });

        binding.imgRupeesDown.setOnClickListener(view -> {
            binding.totalRupeesLy.setVisibility(View.VISIBLE);
            binding.imgRupeesUp.setVisibility(View.VISIBLE);
            binding.imgRupeesDown.setVisibility(View.GONE);
            binding.totalCoinLy.setVisibility(View.GONE);
            binding.imgCoinDropDown.setVisibility(View.VISIBLE);
            binding.imgCoinDropUp.setVisibility(View.GONE);
        });

        binding.imgCoinDropUp.setOnClickListener(view -> {
            binding.totalCoinLy.setVisibility(View.GONE);
            binding.imgCoinDropUp.setVisibility(View.GONE);
            binding.imgCoinDropDown.setVisibility(View.VISIBLE);
        });

        binding.imgCoinDropDown.setOnClickListener(view -> {
            binding.totalCoinLy.setVisibility(View.VISIBLE);
            binding.imgCoinDropUp.setVisibility(View.VISIBLE);
            binding.imgCoinDropDown.setVisibility(View.GONE);
            binding.totalRupeesLy.setVisibility(View.GONE);
            binding.imgRupeesDown.setVisibility(View.VISIBLE);
            binding.imgRupeesUp.setVisibility(View.GONE);
        });

        binding.etFiveHundredCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {

                    int currencyCount = Integer.parseInt(s.toString());
                    fiveHundredAmount = currencyCount * 500;

                } else {
                    fiveHundredAmount = 0;
                }

                binding.tvFiveHundredAmount.setText("₹ " + fiveHundredAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etTwoHundredCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());

                    twoHundredAmount = currencyCount * 200;

                } else {
                    twoHundredAmount = 0;
                }

                binding.tvTwoHundredAmount.setText("₹ " + twoHundredAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etHundredCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    hundredAmount = currencyCount * 100;

                } else {
                    hundredAmount = 0;
                }

                binding.tvHundredAmount.setText("₹ " + hundredAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etFiftyCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    fiftyAmount = currencyCount * 50;

                } else {
                    fiftyAmount = 0;

                }

                binding.tvFiftyAmount.setText("₹ " + fiftyAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etTwentyCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    twentyAmount = currencyCount * 20;

                } else {
                    twentyAmount = 0;
                }

                binding.tvTwentyAmount.setText("₹ " + twentyAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etTenCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    tenAmount = currencyCount * 10;

                } else {
                    tenAmount = 0;
                }

                binding.tvTenAmount.setText("₹ " + tenAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etFiveCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    fiveAmount = currencyCount * 5;

                } else {
                    fiveAmount = 0;
                }

                binding.tvFiveAmount.setText("₹ " + fiveAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etTwentyCoinCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    twentyCoinAmount = currencyCount * 20;

                } else {
                    twentyCoinAmount = 0;
                }

                binding.tvTwentyCoinAmount.setText("₹ " + twentyCoinAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etTenCoinCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    tenCoinAmount = currencyCount * 10;

                } else {
                    tenCoinAmount = 0;
                }

                binding.tvTenCoinAmount.setText("₹ " + tenCoinAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);
                setAmountView();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etFiveCoinCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    fiveCoinAmount = currencyCount * 5;

                } else {
                    fiveCoinAmount = 0;
                }

                binding.tvFiveCoinAmount.setText("₹ " + fiveCoinAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);
                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etTwoCoinCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    int currencyCount = Integer.parseInt(s.toString());
                    twoCoinAmount = currencyCount * 2;

                } else {
                    twoCoinAmount = 0;
                }

                binding.tvTwoCoinAmount.setText("₹ " + twoCoinAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);
                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etOneCoinCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equalsIgnoreCase("")) {
                    oneCoinAmount = Integer.parseInt(s.toString());


                } else {
                    oneCoinAmount = 0;
                }

                binding.tvOneCoinAmount.setText("₹ " + oneCoinAmount);

//                totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
//                totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
//                totalAmount = totalRupees+totalCoin;
//                binding.tvTotalNote.setText("₹ " + totalRupees);
//                binding.tvTotalCoin.setText("₹ " + totalCoin);
//                binding.tvGrandTotal.setText("₹ " + totalAmount);

                setAmountView();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        binding.tvSubmit.setOnClickListener(v -> {

            deliverAmount = Double.parseDouble(binding.tvAmount.getText().toString());

            if (deliverAmount == totalAmount) {

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("500", binding.etFiveHundredCount.getText().toString());
                    jsonObject.put("200", binding.etTwoHundredCount.getText().toString());
                    jsonObject.put("100", binding.etHundredCount.getText().toString());
                    jsonObject.put("50", binding.etFiftyCount.getText().toString());
                    jsonObject.put("20", binding.etTwentyCount.getText().toString());
                    jsonObject.put("10", binding.etTenCount.getText().toString());
                    jsonObject.put("5", binding.etFiveCount.getText().toString());
                    jsonObject.put("20coin", binding.etTwentyCoinCount.getText().toString());
                    jsonObject.put("10coin", binding.etTenCoinCount.getText().toString());
                    jsonObject.put("5coin", binding.etFiveCoinCount.getText().toString());
                    jsonObject.put("2coin", binding.etTwoCoinCount.getText().toString());
                    jsonObject.put("1coin", binding.etOneCoinCount.getText().toString());
                    jsonObject.put("GrandTotal", totalAmount + "");

                     strJsonAmount = jsonObject.toString();

                    whichButtonClicked = "submit";

                    if (networkStatus.equalsIgnoreCase("Connected")) {

                        checkPermissionAndGetLocation();

                    } else {
                        MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
                    }


                  //  showBottomSheetDialog();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            } else {
                MyErrorDialog.nonFinishErrorDialog(context, "Amount mismatched");
            }

        });

        binding.tvCancelReq.setOnClickListener(view -> {

            whichButtonClicked = "cancelRequest";

            if (networkStatus.equalsIgnoreCase("Connected")) {
                checkPermissionAndGetLocation();
            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }


        });

    }

    @SuppressLint("SetTextI18n")
    private void setAmountView() {
        totalRupees = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount+fiveAmount;
        totalCoin = twentyCoinAmount+tenCoinAmount+fiveCoinAmount+twoCoinAmount+oneCoinAmount;
        totalAmount = totalRupees+totalCoin;
        binding.tvTotalNote.setText("₹ " + totalRupees);
        binding.tvTotalCoin.setText("₹ " + totalCoin);
        binding.tvGrandTotal.setText("₹ " + totalAmount);
    }

    @SuppressLint("SetTextI18n")
    private void showUpdateAmountDialog() {

        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.update_count_dialog, null);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_back);

        TextView tvChqAmount = convertView.findViewById(R.id.tvChequeUpdate);
        TextView tvUpdateAmount = convertView.findViewById(R.id.tvUpdateCount);
        tvChqAmount.setText("Cash Amount");
        tvUpdateAmount.setText("Update Amount");
        EditText etAmount = convertView.findViewById(R.id.tvCount);
        TextView tvSubmit = convertView.findViewById(R.id.tvSubmit);
        TextView tvCancel = convertView.findViewById(R.id.tvCancel);

        etAmount.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        tvSubmit.setOnClickListener(v -> {

            if (!TextUtils.isEmpty(etAmount.getText().toString())) {
                updateAmount(etAmount);
                alertDialog.dismiss();
            } else {
                etAmount.setError("Required");
            }

        });

        tvCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();

    }

    private void updateAmount(EditText etAmount) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String strAmt = etAmount.getText().toString().trim();

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().updateAmount(leadId, retailerId, strAmt, jobSubType,geoCode)
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

                                    binding.tvAmount.setText(etAmount.getText().toString());

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

    private void cashCalculateApi(String cashAmount) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = latitude+","+longitude;

        RetrofitClient.getInstance().getApi().cashCalculate(leadId, retailerId,reAttempt, cashAmount,geoCode)
                .enqueue(new Callback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                                int statusCode = responseObject.getInt("statuscode");
                                boolean status = responseObject.getBoolean("status");
                                String message = responseObject.getString("message");

                                if (statusCode == 200) {

//                                    Bundle bundle = new Bundle();
//                                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
//                                    ReplaceFragmentUtils.replaceFragment(new DeliveryBranchListFragment(), bundle, (AppCompatActivity) activity);

                                    Bundle bundle = new Bundle();
                                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
                                    bundle.putString(MyConstantKey.JOB_ID, jobId);
                                    bundle.putString(MyConstantKey.SR_NO, srNo);
                                    bundle.putString(MyConstantKey.COUNT, "");
                                    bundle.putString(MyConstantKey.AMOUNT, amount);
                                    bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                                    bundle.putString(MyConstantKey.CUSTOMER_NAME, customerName);

                                    ReplaceFragmentUtils.replaceFragment(new CaseEnquiryFragment(), bundle, (AppCompatActivity) activity);

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();

                                //    closeSr(leadId);

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

                                    ReplaceFragmentUtils.replaceFragment(new CloseSRFragment(), new Bundle(), (AppCompatActivity) activity);

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

    @SuppressLint("SetTextI18n")
    private void showBottomSheetDialog() {


        CashBottomDialogBinding bottomDialogBinding = CashBottomDialogBinding.inflate(getLayoutInflater());

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(bottomDialogBinding.getRoot());

        String fiveHunCount = binding.etFiveHundredCount.getText().toString();
        String twoHunCount = binding.etTwoHundredCount.getText().toString();
        String hunCount = binding.etHundredCount.getText().toString();
        String fiftyCount = binding.etFiftyCount.getText().toString();
        String twentyCount = binding.etTwentyCount.getText().toString();
        String tenCount = binding.etTenCount.getText().toString();
        String fiveCount = binding.etFiveCount.getText().toString();

        bottomDialogBinding.tvFiveHunCount.setText(fiveHunCount);
        bottomDialogBinding.tvTwoHunRupeesCount.setText(twoHunCount);
        bottomDialogBinding.tvHunCount.setText(hunCount);
        bottomDialogBinding.tvFiftyCount.setText(fiftyCount);
        bottomDialogBinding.tvTwentyCount.setText(twentyCount);
        bottomDialogBinding.tvTenCount.setText(tenCount);
        bottomDialogBinding.tvFiveCount.setText(fiveCount);

        bottomDialogBinding.tvFiveHunRupees.setText("₹"+fiveHundredAmount);
        bottomDialogBinding.tvTwoHunRupees.setText("₹"+twoHundredAmount);
        bottomDialogBinding.tvHundred.setText("₹"+hundredAmount);
        bottomDialogBinding.tvFifty.setText("₹"+fiftyAmount);
        bottomDialogBinding.tvTwenty.setText("₹"+twentyAmount);
        bottomDialogBinding.tvten.setText("₹"+tenAmount);
        bottomDialogBinding.tvFive.setText("₹"+fiveAmount);

        bottomDialogBinding.btnNo.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
        });

        bottomDialogBinding.btnYes.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            binding.cashMainLy.setVisibility(View.GONE);
            binding.counterfeitMainLy.setVisibility(View.VISIBLE);

        });

        binding.fiveHunCounterfietDropDown.setOnClickListener(view -> {
            binding.fiveHunCounterfietDropDown.setVisibility(View.GONE);
            binding.fiveHunCounterfietDropUp.setVisibility(View.VISIBLE);
            binding.fiveHunCounterFietAddLy.setVisibility(View.VISIBLE);


            if (isFiveHunClicked){
                createDynamicLy(binding.fiveHunCounterfietLy, String.valueOf(fiveHunSerialNo), false);
                isFiveHunClicked = false;
            }

        });

        binding.fiveHunCounterfietDropUp.setOnClickListener(view -> {
            binding.fiveHunCounterfietDropDown.setVisibility(View.VISIBLE);
            binding.fiveHunCounterfietDropUp.setVisibility(View.GONE);
            binding.fiveHunCounterFietAddLy.setVisibility(View.GONE);
        });

        binding.twoHunCounterfietDropDown.setOnClickListener(view -> {
            binding.twoHunCounterfietDropDown.setVisibility(View.GONE);
            binding.twoHunCounterfietDropUp.setVisibility(View.VISIBLE);
            binding.twoHunCounterFietAddLy.setVisibility(View.VISIBLE);

            if(isTwoHunClicked){
                createDynamicLy(binding.twoHunCounterfietLy,String.valueOf(twoHunSerialNo),false);
                isTwoHunClicked = false;
            }

        });

        binding.twoHunCounterfietDropUp.setOnClickListener(view -> {
            binding.twoHunCounterfietDropDown.setVisibility(View.VISIBLE);
            binding.twoHunCounterfietDropUp.setVisibility(View.GONE);
            binding.twoHunCounterFietAddLy.setVisibility(View.GONE);

        });

        binding.hundredCounterfietDropDown.setOnClickListener(view -> {
            binding.hundredCounterfietDropDown.setVisibility(View.GONE);
            binding.hundredCounterfietDropUp.setVisibility(View.VISIBLE);
            binding.hunCounterFietAddLy.setVisibility(View.VISIBLE);

            if (isHunClicked) {
                createDynamicLy(binding.hunCounterfietLy,String.valueOf(hunSerialNo),false);
                isHunClicked = false;
            }

        });

        binding.hundredCounterfietDropUp.setOnClickListener(view -> {
            binding.hundredCounterfietDropDown.setVisibility(View.VISIBLE);
            binding.hundredCounterfietDropUp.setVisibility(View.GONE);
            binding.hunCounterFietAddLy.setVisibility(View.GONE);
        });

        binding.fiftyCounterfietDropDown.setOnClickListener(view -> {
            binding.fiftyCounterfietDropDown.setVisibility(View.GONE);
            binding.fiftyCounterfietDropUp.setVisibility(View.VISIBLE);
            binding.fiftyCounterFietAddLy.setVisibility(View.VISIBLE);

            if (isFiftyClicked){
            createDynamicLy(binding.fiftyCounterfietLy,String.valueOf(fiftySerialNo),false);
            isFiftyClicked = false;
            }
        });

        binding.fiftyCounterfietDropUp.setOnClickListener(view -> {
            binding.fiftyCounterfietDropDown.setVisibility(View.VISIBLE);
            binding.fiftyCounterfietDropUp.setVisibility(View.GONE);
            binding.fiftyCounterFietAddLy.setVisibility(View.GONE);
        });

        binding.twentyCounterfietDropDown.setOnClickListener(view -> {
            binding.twentyCounterfietDropDown.setVisibility(View.GONE);
            binding.twentyCounterfietDropUp.setVisibility(View.VISIBLE);
            binding.twentyCounterFietAddLy.setVisibility(View.VISIBLE);

            if (isTwentyClicked){
                createDynamicLy(binding.twentyCounterfietLy,String.valueOf(twentySerialNo),false);
                isTwentyClicked = false;
            }

        });
        binding.twentyCounterfietDropUp.setOnClickListener(view -> {
            binding.twentyCounterfietDropDown.setVisibility(View.VISIBLE);
            binding.twentyCounterfietDropUp.setVisibility(View.GONE);
            binding.twentyCounterFietAddLy.setVisibility(View.GONE);
        });

        binding.tenCounterfietDropDown.setOnClickListener(view -> {
            binding.tenCounterfietDropDown.setVisibility(View.GONE);
            binding.tenCounterfietDropUp.setVisibility(View.VISIBLE);
            binding.tenCounterFietAddLy.setVisibility(View.VISIBLE);

            if (isTenClicked){
                createDynamicLy(binding.tenCounterfietLy,String.valueOf(tenSerialNo),false);
                isTenClicked = false;
             }
        });
        binding.tenCounterfietDropUp.setOnClickListener(view -> {
            binding.tenCounterfietDropDown.setVisibility(View.VISIBLE);
            binding.tenCounterfietDropUp.setVisibility(View.GONE);
            binding.tenCounterFietAddLy.setVisibility(View.GONE);
        });

        binding.fiveCounterfietDropDown.setOnClickListener(view -> {
            binding.fiveCounterfietDropDown.setVisibility(View.GONE);
            binding.fiveCounterfietDropUp.setVisibility(View.VISIBLE);
            binding.fiveCounterFietAddLy.setVisibility(View.VISIBLE);

            if (isFiveClicked){
                createDynamicLy(binding.fiveCounterfeitLy,String.valueOf(fiveSerialNo),false);
                isFiveClicked = false;
            }


        });
        binding.fiveCounterfietDropUp.setOnClickListener(view -> {
            binding.fiveCounterfietDropDown.setVisibility(View.VISIBLE);
            binding.fiveCounterfietDropUp.setVisibility(View.GONE);
            binding.fiveCounterFietAddLy.setVisibility(View.GONE);
        });

        binding.fiveHunCounterAddClick.setOnClickListener(view -> {
            fiveHunSerialNo++;
            createDynamicLy(binding.fiveHunCounterfietLy,String.valueOf(fiveHunSerialNo),true);
        });
        binding.twoHunCounterAddClick.setOnClickListener(view -> {
            twoHunSerialNo++;
            createDynamicLy(binding.twoHunCounterfietLy,String.valueOf(twoHunSerialNo),true);
        });
        binding.hunCounterAddClick.setOnClickListener(view -> {
            hunSerialNo++;
            createDynamicLy(binding.hunCounterfietLy,String.valueOf(hunSerialNo),true);
        });
        binding.fiftyCounterAddClick.setOnClickListener(view -> {
            fiftySerialNo++;
            createDynamicLy(binding.fiftyCounterfietLy,String.valueOf(fiftySerialNo),true);
        });
        binding.twentyCounterAddClick.setOnClickListener(view -> {
            twentySerialNo++;
            createDynamicLy(binding.twentyCounterfietLy,String.valueOf(twentySerialNo),true);
        });
        binding.tenCounterAddClick.setOnClickListener(view -> {
            tenSerialNo++;
            createDynamicLy(binding.tenCounterfietLy,String.valueOf(tenSerialNo),true);
        });
        binding.fiveCounterAddClick.setOnClickListener(view -> {
            fiveSerialNo++;
            createDynamicLy(binding.fiveCounterfeitLy,String.valueOf(fiveSerialNo),true);
        });


        bottomSheetDialog.show();
    }

    private void createDynamicLy(LinearLayout dynamicLy, String sNo, boolean isCloseVisible) {
        DynamicCounterfeitAddBinding dynamicCounterfeitAddBinding = DynamicCounterfeitAddBinding.inflate(getLayoutInflater());
        dynamicLy.addView(dynamicCounterfeitAddBinding.getRoot());

        dynamicCounterfeitAddBinding.etSNo.setText(sNo);

        if (isCloseVisible) {
            dynamicCounterfeitAddBinding.imgRemove.setVisibility(View.VISIBLE);
        }

        dynamicCounterfeitAddBinding.imgRemove.setOnClickListener(view -> {
            dynamicLy.removeView(dynamicCounterfeitAddBinding.getRoot());
        });

        dynamicCounterfeitAddBinding.etCurrencyNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    dynamicCounterfeitAddBinding.imgRemove.setVisibility(View.GONE);
                }
                else {
                    if (isCloseVisible) {
                        dynamicCounterfeitAddBinding.imgRemove.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
                        cashCalculateApi(strJsonAmount);
                    }  else if (whichButtonClicked.equalsIgnoreCase("cancelRequest")) {
                        getRemarkList(context, activity,leadId,retailerId,latitude,longitude);
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
        CashCalculateFragment.AddressResultReceiver addressResultReceiver = new CashCalculateFragment.AddressResultReceiver(new Handler());
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
