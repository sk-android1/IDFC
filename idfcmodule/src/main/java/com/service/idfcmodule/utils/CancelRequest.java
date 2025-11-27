package com.service.idfcmodule.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.lead.BankListFragment;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.myinterface.LocationListener;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CancelRequest {

    @SuppressLint("SetTextI18n")
    public static void getRemarkList(Context context,Activity activity, String leadId, String retailerId,String lat, String longi) {
        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = lat+","+longi;

        RetrofitClient.getInstance().getApi().getRemark(retailerId,geoCode).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        boolean status = responseObject.getBoolean("status");
                        String message = responseObject.getString("message");

                        if (status) {

                            JSONArray dataArray = responseObject.getJSONArray("data");

                            ArrayList<String> remarkList = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                String proofType = dataArray.getString(i);
                                remarkList.add(proofType);
                            }

                          showConfirmDialog(context, activity,leadId,retailerId,remarkList,lat,longi);
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
                    MyErrorDialog.somethingWentWrongDialog(activity);
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
    private static void showConfirmDialog(Context context, Activity activity,String leadId, String retailerId, ArrayList<String> remarkList,String lat, String longi) {

        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.cancel_req_dialog, null);
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_back);

        LinearLayout selectLy = convertView.findViewById(R.id.selectRemarkLy);
        LinearLayout remarkLy = convertView.findViewById(R.id.remarkLy);

        AutoCompleteTextView autoTv = convertView.findViewById(R.id.auto_remark);

//        ArrayList<String> remarkList = new ArrayList<>();
//        remarkList.add("Address not found");
//        remarkList.add("Incomplete or incorrect address");
//        remarkList.add("Customer not available at the location");
//        remarkList.add("Customer refused to hand over item");
//        remarkList.add("Customer behaved inappropriately");
//        remarkList.add("Customer gave invalid documents/cheque");
//        remarkList.add("Customer asked to reschedule");
//
     //   ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, remarkList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.remarks_item,R.id.tv_item, remarkList);
        autoTv.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        autoTv.setAdapter(adapter);
        selectLy.setOnClickListener(view -> {
            autoTv.showDropDown();
        });
        autoTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        EditText etRemark = convertView.findViewById(R.id.et_remark);
        ImageView imgClear = convertView.findViewById(R.id.imgClear);
        TextView tvCancel = convertView.findViewById(R.id.tvCancel);
        tvCancel.setOnClickListener(v -> alertDialog.dismiss());
        TextView tvYes = convertView.findViewById(R.id.tvYes);

        imgClear.setOnClickListener(view -> {
            etRemark.setText("");
        });

        etRemark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i > 0) {
                    imgClear.setVisibility(View.VISIBLE);
                } else {
                    imgClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tvYes.setOnClickListener(v -> {

            if (!TextUtils.isEmpty(autoTv.getText().toString())) {
                String remark = autoTv.getText().toString();
                cancelRequest(context, activity, leadId, retailerId,remark,lat,longi);
                alertDialog.dismiss();
            } else {
                etRemark.setError("Required");
            }

        });

        tvCancel.setOnClickListener(view -> {
            alertDialog.dismiss();
        });

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();
    }

    private static void cancelRequest(Context context, Activity activity, String leadId, String retailerId, String remark,String lat, String longi) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = lat+","+longi;

        RetrofitClient.getInstance().getApi().cancelRequest(leadId, retailerId,remark,geoCode)
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
                                    ReplaceFragmentUtils.replaceFragment(new BankListFragment(), new Bundle(), (AppCompatActivity) activity);

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
                            if (response.code() == 400 || response.code() == 404) {
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

}
