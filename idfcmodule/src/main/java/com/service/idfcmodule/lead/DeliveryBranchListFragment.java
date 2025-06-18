package com.service.idfcmodule.lead;

import static com.service.idfcmodule.IdfcMainActivity.retailerId;
import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.adaptors.DeliveryBranchListAdapter;
import com.service.idfcmodule.adaptors.LeadListAdapter;
import com.service.idfcmodule.databinding.FragmentDeliveryBranchListBinding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.models.BranchListModel;
import com.service.idfcmodule.models.LeadModel;
import com.service.idfcmodule.myinterface.BranchItemClicked;
import com.service.idfcmodule.myinterface.LeadItemClicked;
import com.service.idfcmodule.utils.ConverterUtils;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeliveryBranchListFragment extends Fragment {

    FragmentDeliveryBranchListBinding binding;

    ArrayList<BranchListModel> arrayList;

    Context context;
    Activity activity;

    String leadId = "", srNo = "";

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
        }

        getBranchList(leadId);

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
                showConfirmDialog(leadId, branchId, branchName);
                ConverterUtils.hideKeyboard(activity);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void showConfirmDialog(String leadId, String branchId, String branchName) {

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
            submitBranch(leadId, branchId);
        });

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();

    }

    private void getBranchList(String leadId) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialog(context);

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

    private void submitBranch(String leadId, String branchId) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialog(context);

        RetrofitClient.getInstance().getApi().submitBranch(leadId, retailerId, branchId)
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

}