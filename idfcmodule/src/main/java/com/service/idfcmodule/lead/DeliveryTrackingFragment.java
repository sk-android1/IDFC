package com.service.idfcmodule.lead;

import static com.service.idfcmodule.IdfcMainActivity.retailerId;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.service.idfcmodule.databinding.FragmentDeliveryTrackingBinding;
import com.service.idfcmodule.utils.ConverterUtils;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeliveryTrackingFragment extends Fragment {

    FragmentDeliveryTrackingBinding binding;
    Activity activity;
    Context context;

    String mobileNo = "", leadId = "", srNo = "";

    String delivery = "", branchName = "", ifscCode = "", jobSubType = "", jobType = "", dropAddress = "";

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

        }

        binding.tvDelivery.setText(delivery);
        binding.tvBranchName.setText(branchName);
        binding.tvIfsc.setText(ifscCode);
        binding.tvJobType.setText(ConverterUtils.capitaliseString(jobSubType) + " " + ConverterUtils.capitaliseString(jobType));
        binding.tvDropAddress.setText(dropAddress);

        binding.tvDropTime.setText(ConverterUtils.currentDate());

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

        binding.tvCloseSr.setOnClickListener(v -> {
            closeSr(leadId);
        });

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