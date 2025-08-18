package com.service.idfcmodule.retailerkyc;

import static android.app.Activity.RESULT_OK;

import static com.service.idfcmodule.IdfcMainActivity.comType;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentAadharBinding;
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


public class AadharFragment extends Fragment {

    FragmentAadharBinding binding;
    Activity activity;
    Context context;

    String networkStatus = "";
    String pinCode = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        binding = FragmentAadharBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        if (comType.equalsIgnoreCase("Vidcom")) {
            binding.tvNext.setBackgroundResource(R.drawable.button_on_dec2);
        }

        getSetData();

        binding.tvNext.setOnClickListener(v -> {

            if (networkStatus.equalsIgnoreCase("Connected")) {
                verifyAadharApi();
            } else {
                MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
            }


        });

//        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                moduleFinishMessage("Back Button Pressed");
//            }
//        });

        return binding.getRoot();
    }

    private void verifyAadharApi() {
        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        RetrofitClient.getInstance().getApi().verifyAadhaar(retailerId, IdfcMainActivity.revision, pinCode, IdfcMainActivity.latitude+","+IdfcMainActivity.longitude).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {

                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {

//                                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//                                LayoutInflater inflater = LayoutInflater.from(context);
//                                View convertView = inflater.inflate(R.layout.error_view, null);
//                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//                                ConstraintLayout layout = convertView.findViewById(R.id.TitleLayout);
//                                layout.setBackgroundTintList(context.getResources().getColorStateList(R.color.green));
//
//                                TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
//                                ImageView ivClose = convertView.findViewById(R.id.iv_close);
//                              //  ivClose.setOnClickListener(v -> alertDialog.dismiss());
//                                TextView okayBtn  = convertView.findViewById(R.id.okayBtn);
//                                okayBtn.setOnClickListener(v -> {
//                                    alertDialog.dismiss();
//                                    ReplaceFragmentUtils.replaceFragment(new PhotoUploadFragment(), new Bundle(), (AppCompatActivity)activity );
//                                });
//                                showErrorTT.setText(message);
//                                alertDialog.setCancelable(false);
//                                alertDialog.setView(convertView);
//
//                                alertDialog.show();

                            String stage = responseObject.getString("stage");

                            if (stage.equalsIgnoreCase("2")) {
                                ReplaceFragmentUtils.replaceFragment(new PhotoUploadFragment(), new Bundle(), (AppCompatActivity) activity);
                            }
                           else if (stage.equalsIgnoreCase("3")) {
                                ReplaceFragmentUtils.replaceFragment(new UploadDocumentFragment(), new Bundle(), (AppCompatActivity) activity);
                            } else {
                                ReplaceFragmentUtils.replaceFragment(new FinishFragment(), new Bundle(), (AppCompatActivity) activity);
                            }

                            Snackbar.make(binding.mainLy, message, Snackbar.LENGTH_LONG).show();
                        //    ReplaceFragmentUtils.replaceFragment(new PhotoUploadFragment(), new Bundle(), (AppCompatActivity) activity);

                            pd.dismiss();

                        } else {
                            pd.dismiss();
                            MyErrorDialog.nonFinishErrorDialog(context, message);

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


    @SuppressLint("SetTextI18n")
    private void getSetData() {
        assert getArguments() != null;
        String fullName = getArguments().getString(MyConstantKey.FULL_NAME);
        String aadharNo = getArguments().getString(MyConstantKey.AADHAR_NO);
        String dob = getArguments().getString(MyConstantKey.DOB);
        String address = getArguments().getString(MyConstantKey.ADDRESS);
        pinCode = getArguments().getString(MyConstantKey.PINCODE);


        assert aadharNo != null;
        String aadharMask = aadharNo.substring(aadharNo.length() - 4);

        binding.tvName.setText(fullName);
        binding.tvAadharNo.setText("XXXXXXXX" + aadharMask);
        binding.tvDob.setText(dob);
        binding.tvAddress.setText(address);
    }

    private void moduleFinishMessage(String message) {
        Intent in = new Intent();
        in.putExtra("message", message);
        requireActivity().setResult(RESULT_OK, in);
        requireActivity().finish();
    }


}