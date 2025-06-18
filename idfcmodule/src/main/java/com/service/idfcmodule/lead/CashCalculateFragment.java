package com.service.idfcmodule.lead;

import static com.service.idfcmodule.IdfcMainActivity.retailerId;
import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentCashCalculateBinding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CashCalculateFragment extends Fragment {

    FragmentCashCalculateBinding binding;

    Activity activity;
    Context context;

    int fiveHundredAmount = 0, twoHundredAmount = 0, hundredAmount = 0, fiftyAmount = 0, twentyAmount = 0, tenAmount = 0;

    double totalAmount = 0.0;
    double deliverAmount = 0.0;

    String leadId = "";

    String jobId,srNo, jobSubType, amount;

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
        }

        binding.tvJobId.setText("SR - "+srNo);
        binding.tvAmount.setText(amount);

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

                binding.tvFiveHundredAmount.setText("₹" + fiveHundredAmount);
                totalAmount = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount;
                binding.tvGrandTotal.setText("₹" + totalAmount);

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

                binding.tvTwoHundredAmount.setText("₹" + twoHundredAmount);
                totalAmount = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount;
                binding.tvGrandTotal.setText("₹" + totalAmount);

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

                binding.tvHundredAmount.setText("₹" + hundredAmount);
                totalAmount = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount;
                binding.tvGrandTotal.setText("₹" + totalAmount);

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

                binding.tvFiftyAmount.setText("₹" + fiftyAmount);
                totalAmount = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount;
                binding.tvGrandTotal.setText("₹" + totalAmount);

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

                binding.tvTwentyAmount.setText("₹" + twentyAmount);
                totalAmount = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount;
                binding.tvGrandTotal.setText("₹" + totalAmount);

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
                totalAmount = fiveHundredAmount + twoHundredAmount + hundredAmount + fiftyAmount + twentyAmount + tenAmount;
                binding.tvGrandTotal.setText("₹ " + totalAmount);

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
                    jsonObject.put("GrandTotal", totalAmount + "");

                    String strData = jsonObject.toString();

                    cashCalculateApi(strData);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            } else {

                MyErrorDialog.nonFinishErrorDialog(context, "Amount mismatched");

            }

        });

        binding.tvCancelReq.setOnClickListener(view -> {
            getRemarkList(context, activity,leadId,retailerId);
        });

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

        AlertDialog pDialog = MyProgressDialog.createAlertDialog(context);

        String strAmt = etAmount.getText().toString().trim();

        RetrofitClient.getInstance().getApi().updateAmount(leadId, retailerId, strAmt, jobSubType)
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

        AlertDialog pDialog = MyProgressDialog.createAlertDialog(context);


        RetrofitClient.getInstance().getApi().cashCalculate(leadId, retailerId,"0", cashAmount)
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

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();

                                    closeSr(leadId);

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
        AlertDialog pDialog = MyProgressDialog.createAlertDialog(context);

        RetrofitClient.getInstance().getApi().closeSr(leadId, retailerId)
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

}
