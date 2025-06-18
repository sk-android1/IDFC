package com.service.idfcmodule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonObject;
import com.service.idfcmodule.databinding.ActivityIdfcMainBinding;
import com.service.idfcmodule.fragments.DashboardFragment;
import com.service.idfcmodule.lead.BankListFragment;
import com.service.idfcmodule.lead.CashCalculateFragment;
import com.service.idfcmodule.models.UserModel;
import com.service.idfcmodule.retailerkyc.AadharFragment;
import com.service.idfcmodule.retailerkyc.ApplicationApprovedFragment;
import com.service.idfcmodule.retailerkyc.FinishFragment;
import com.service.idfcmodule.retailerkyc.PhotoUploadFragment;
import com.service.idfcmodule.retailerkyc.UploadDocumentFragment;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.utils.UserListSingelton;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IdfcMainActivity extends AppCompatActivity {

    ActivityIdfcMainBinding binding;

    Activity activity;
    Context context;

    public static String comType = "";
    public static String appType = "";
    public static String retailerId = "";
    public static String panNo = "";
    public static String strJsonArray = "";
    public static String asmId = "";
    public static String bankId = "";
    public static String latitude = "";
    public static String longitude = "";
    public static String loginType = "";

    String networkStatus = "";

    public static String revision = "";

    String isKyc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //   EdgeToEdge.enable(this);

        binding = ActivityIdfcMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(getResources().getColor(R.color.sky_blue));

        activity = IdfcMainActivity.this;
        context = IdfcMainActivity.this;

        networkStatus = NetworkUtils.getConnectivityStatusString(context);
        getIntentData();

        if (appType != null) {
            if (appType.equalsIgnoreCase("Partner")) {
                ReplaceFragmentUtils.replaceFragment(new DashboardFragment(), new Bundle(), (AppCompatActivity) activity);
            } else {
                if (networkStatus.equalsIgnoreCase("Connected")) {
                    verifyAgentApi();
                    //   ReplaceFragmentUtils.replaceFragment(new PhotoUploadFragment(), new Bundle(), (AppCompatActivity) activity);
                } else {
                    MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
                }
            }
        }

    }

    private void getIntentData() {

        appType = getIntent().getStringExtra(MyConstantKey.APP_TYPE);
        comType = getIntent().getStringExtra(MyConstantKey.COM_TYPE);
        retailerId = getIntent().getStringExtra(MyConstantKey.RETAILER_ID);
        panNo = getIntent().getStringExtra(MyConstantKey.PAN_NO);
     //   strJsonArray = getIntent().getStringExtra(MyConstantKey.USER_LIST);

        strJsonArray= UserListSingelton.userListStr;

        asmId = getIntent().getStringExtra(MyConstantKey.ASM_ID);
        bankId = getIntent().getStringExtra(MyConstantKey.BANK_ID);
        latitude = getIntent().getStringExtra(MyConstantKey.LATITUDE);
        longitude = getIntent().getStringExtra(MyConstantKey.LONGITUDE);
        loginType = getIntent().getStringExtra(MyConstantKey.LOGIN_TYPE);

    }

    private void verifyAgentApi() {

        AlertDialog pd = MyProgressDialog.createAlertDialog(context);
        pd.show();

        RetrofitClient.getInstance().getApi().verifyAgent(retailerId, panNo).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {

                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {

                            isKyc = responseObject.optString("is_kyc");

                            if (isKyc.equalsIgnoreCase("0")) {
                                String stage = responseObject.getString("stage");

                                revision = responseObject.getString("revision");

                                if (stage.equalsIgnoreCase("1")) {

                                    JSONObject dataObject = responseObject.getJSONObject("data");
                                    String fullName = dataObject.optString("full_name");
                                    String aadharNo = dataObject.optString("aadhaar_number");
                                    String dob = dataObject.optString("dob");

                                    JSONObject addObj = dataObject.getJSONObject("address");
                                    String country = addObj.optString("country");
                                    String dist = addObj.optString("dist");
                                    String state = addObj.optString("state");
                                    String po = addObj.optString("po");
                                    String loc = addObj.optString("loc");
                                    String vtc = addObj.optString("vtc");
                                    String subDist = addObj.optString("subdist");
                                    String street = addObj.optString("street");
                                    String house = addObj.optString("house");
                                    String landmark = addObj.optString("landmark");

                                    String pinCode = dataObject.optString("zip");

                                    String address = house + ", " + street + ", " + landmark + ", " + po + ", " + dist + " (" + country + ")\nPincode " + pinCode;

                                    try {
                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        Date dt = inputFormat.parse(dob);
                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy");
                                        dob = outputFormat.format(dt);
                                    } catch (Exception ignored) {
                                    }

                                    Bundle bundle = new Bundle();
                                    bundle.putString(MyConstantKey.FULL_NAME, fullName);
                                    bundle.putString(MyConstantKey.AADHAR_NO, aadharNo);
                                    bundle.putString(MyConstantKey.DOB, dob);
                                    bundle.putString(MyConstantKey.ADDRESS, address);
                                    bundle.putString(MyConstantKey.PINCODE, pinCode);

                                    ReplaceFragmentUtils.replaceFragment(new AadharFragment(), bundle, (AppCompatActivity) activity);

                                } else if (stage.equalsIgnoreCase("2")) {
                                    ReplaceFragmentUtils.replaceFragment(new PhotoUploadFragment(), new Bundle(), (AppCompatActivity) activity);
                                } else if (stage.equalsIgnoreCase("3")) {
                                    ReplaceFragmentUtils.replaceFragment(new UploadDocumentFragment(), new Bundle(), (AppCompatActivity) activity);
                                } else if (stage.equalsIgnoreCase("4")) {
                                    String approvalStatus = responseObject.getString("approval");
                                    //  approvalStatus = "0";

                                    if (approvalStatus.equalsIgnoreCase("0")) {
                                        ReplaceFragmentUtils.replaceFragment(new FinishFragment(), new Bundle(), (AppCompatActivity) activity);
                                    } else {
                                        String iCardLink = responseObject.optString("icardlink");
                                        String content = responseObject.getString("content");
                                        String awb = responseObject.optString("awb");

                                        Bundle bundle = new Bundle();
                                        bundle.putString(MyConstantKey.CONTENT, content);
                                        bundle.putString(MyConstantKey.ICARD_LINK, iCardLink);
                                        bundle.putString(MyConstantKey.AWB, awb);
                                        bundle.putString(MyConstantKey.STAGE, "4");
                                        ReplaceFragmentUtils.replaceFragment(new ApplicationApprovedFragment(), bundle, (AppCompatActivity) activity);
                                    }

                                } else {
                                    String iCardLink = responseObject.optString("icardlink");
                                    String content = responseObject.getString("content");
                                    String awb = responseObject.optString("awb");

                                    Bundle bundle = new Bundle();

                                    bundle.putString(MyConstantKey.CONTENT, content);
                                    bundle.putString(MyConstantKey.STAGE, "5");
                                    bundle.putString(MyConstantKey.AWB, awb);          //  for hide awb layout
                                    ReplaceFragmentUtils.replaceFragment(new ApplicationApprovedFragment(), bundle, (AppCompatActivity) activity);
                                }
                            } else {
                                ReplaceFragmentUtils.replaceFragment(new BankListFragment(), new Bundle(), (AppCompatActivity) activity);
                            }

                            pd.dismiss();

                        } else {
                            pd.dismiss();
                            MyErrorDialog.activityFinishErrorDialog(context, activity, message);
                        }

                    } catch (JSONException e) {
                        pd.dismiss();
                        MyErrorDialog.activityFinishErrorDialog(context, activity, "Something went wrong");
                    }
                } else {
                    pd.dismiss();
                    MyErrorDialog.activityFinishErrorDialog(context, activity, "Something went wrong");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pd.dismiss();
                MyErrorDialog.activityFinishErrorDialog(context, activity, "Something went wrong");
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);


      //  outState.clear();
    }

    @Override
    public void onBackPressed() {

        int stackCount = getSupportFragmentManager().getBackStackEntryCount();

        if (isKyc.equalsIgnoreCase("0")) {
            Toast.makeText(activity, "Don't press back button", Toast.LENGTH_SHORT).show();
        } else {

            if (stackCount == 1) {
                finish();
            } else if (stackCount == 2) {
                super.onBackPressed();

              //  getSupportFragmentManager().popBackStack();
            } else {
                Toast.makeText(activity, "Don't press back button", Toast.LENGTH_SHORT).show();
            }

        }

    }

}