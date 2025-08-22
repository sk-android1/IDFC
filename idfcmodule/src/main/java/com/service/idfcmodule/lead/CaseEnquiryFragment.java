package com.service.idfcmodule.lead;

import static com.service.idfcmodule.IdfcMainActivity.comType;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;

import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentCaseEnquiryBinding;
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

public class CaseEnquiryFragment extends Fragment {

    FragmentCaseEnquiryBinding binding;
    Activity activity;
    Context context;

    String leadId = "", jobId = "", srNo, jobSubType = "", count = "", amount = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCaseEnquiryBinding.inflate(inflater);

        if (comType.equalsIgnoreCase("Vidcom")){
            ImageViewCompat.setImageTintList(binding.leadTopLy.imgBack, ColorStateList.valueOf(getResources().getColor(R.color.vidcom_color)));
            binding.imgEnquiry.setImageResource(R.drawable.enquiry2);
        }

        activity = requireActivity();
        context = requireContext();

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        if (getArguments() != null) {
            srNo = getArguments().getString(MyConstantKey.SR_NO, "");
            leadId = getArguments().getString(MyConstantKey.LEAD_ID, "");
            jobId = getArguments().getString(MyConstantKey.JOB_ID, "");
            jobSubType = getArguments().getString(MyConstantKey.JOB_SUBTYPE, "");
            amount = getArguments().getString(MyConstantKey.AMOUNT, "");
            count = getArguments().getString(MyConstantKey.COUNT, "");
        }

        binding.tvCustAcceptance.setText("Customer acceptance for SR NO. " + srNo);

        clickEvents();

        return binding.getRoot();
    }

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
            activity.onBackPressed();
            //  requireActivity().finish();
        });

        binding.tvEnquiry.setOnClickListener(view -> {
            caseEnquiry();
        });

        binding.tvCancelReq.setOnClickListener(view -> {
            getRemarkList(context, activity, leadId, retailerId);
        });

    }

    private void caseEnquiry() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RetrofitClient.getInstance().getApi().caseEnquiry(leadId, retailerId)
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

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();

                                    String reAttempt = responseObject.getString("reattempt");


                                    if (reAttempt.equalsIgnoreCase("0")) {

                                        if (!jobSubType.equalsIgnoreCase("cash")){
                                            Bundle bundle = new Bundle();
                                            bundle.putString(MyConstantKey.LEAD_ID, leadId);
                                            bundle.putString(MyConstantKey.SR_NO, srNo);
                                            ReplaceFragmentUtils.replaceFragment(new DeliveryBranchListFragment(), bundle, (AppCompatActivity) activity);
                                        }
                                        else {
                                            closeSr(leadId);
                                        }

                                    } else {
                                        confirmationDialog(message);
                                    }

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

    @SuppressLint("SetTextI18n")
    private void confirmationDialog(String errorMessage) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.case_enq_confirm_dialog, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
        ImageView ivClose = convertView.findViewById(R.id.iv_close);
        TextView okayBtn = convertView.findViewById(R.id.okayBtn);

        if (jobSubType.equalsIgnoreCase("cheque")) {
            okayBtn.setText("Upload Cheque Again");
        } else if (jobSubType.equalsIgnoreCase("document")) {
            okayBtn.setText("Upload Document Again");
        } else {
            okayBtn.setText("OK");
        }

        ivClose.setOnClickListener(v->{
            alertDialog.dismiss();
        });

        okayBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
            if (jobSubType.equalsIgnoreCase("cheque")) {
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.COUNT, count);
                bundle.putString(MyConstantKey.AMOUNT, amount);
                bundle.putString(MyConstantKey.JOB_ID, jobId);
                bundle.putString(MyConstantKey.SR_NO, srNo);
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                bundle.putString(MyConstantKey.REATTEMPT, "1");
             //   ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragmentNew(), bundle, (AppCompatActivity) activity);
              //  ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragment(), bundle, (AppCompatActivity) activity);
                ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragmentNew2(), bundle, (AppCompatActivity) activity);
            } else if (jobSubType.equalsIgnoreCase("document")) {
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.COUNT, count);
                bundle.putString(MyConstantKey.JOB_ID, jobId);
                bundle.putString(MyConstantKey.SR_NO, srNo);
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                bundle.putString(MyConstantKey.REATTEMPT, "1");
                ReplaceFragmentUtils.replaceFragment(new DeliveredDocumentUploadFragment(), bundle, (AppCompatActivity) activity);
            //    ReplaceFragmentUtils.replaceFragment(new DeliveredDocumentUploadFragmentNew(), bundle, (AppCompatActivity) activity);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.AMOUNT, amount);
                bundle.putString(MyConstantKey.COUNT, count);
                bundle.putString(MyConstantKey.JOB_ID, jobId);
                bundle.putString(MyConstantKey.SR_NO, srNo);
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                bundle.putString(MyConstantKey.REATTEMPT, "1");
                ReplaceFragmentUtils.replaceFragment(new CashCalculateFragment(), bundle, (AppCompatActivity) activity);
            }

        });
        showErrorTT.setText(errorMessage);
        alertDialog.setView(convertView);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void closeSr(String leadId) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

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

                                    Bundle bundle = new Bundle();
                                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
                                    bundle.putString(MyConstantKey.SR_NO, srNo);

                                    //    ReplaceFragmentUtils.replaceFragment(new CloseSRFragment(), new Bundle(), (AppCompatActivity) activity);
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

}