package com.service.idfcmodule.lead;

import static com.service.idfcmodule.IdfcMainActivity.comType;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;
import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.adaptors.LeadListAdapter;
import com.service.idfcmodule.databinding.FragmentLeadListBinding;
import com.service.idfcmodule.models.LeadModel;
import com.service.idfcmodule.myinterface.LeadItemClicked;
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

    String bankId;

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

        if (comType.equalsIgnoreCase("Vidcom")){
            ImageViewCompat.setImageTintList(binding.leadTopLy.imgBack, ColorStateList.valueOf(getResources().getColor(R.color.vidcom_color)));
        }

        context = requireContext();
        activity = requireActivity();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        assert getArguments() != null;
        bankId = getArguments().getString(MyConstantKey.BANK_ID,"");    //  from BankList fragment

        if (networkStatus.equalsIgnoreCase("Connected")) {
            getLeadList("0");
        } else {
            MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
        }

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
            getLeadList("0");
            binding.tvPending.setBackgroundResource(R.drawable.rounded_green);
            binding.tvPending.setTextColor(getResources().getColor(R.color.white));
            binding.tvAllocated.setBackgroundResource(R.drawable.rounded_border);
            binding.tvAllocated.setTextColor(getResources().getColor(R.color.grey_idfc));
            binding.tvCompleted.setBackgroundResource(R.drawable.rounded_border);
            binding.tvCompleted.setTextColor(getResources().getColor(R.color.grey_idfc));
        });

        binding.tvAllocated.setOnClickListener(v -> {
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

                                JSONObject pickUpObj = dataObject.getJSONObject("pickup");
                                String pickupAddress = pickUpObj.optString("address");
                                String pickupState = pickUpObj.optString("state");
                                String pickupCity = pickUpObj.optString("city");
                                String pickupPincode = pickUpObj.optString("pincode");

                                String latLongi = pickUpObj.optString("gps_coordinates");

                                String lat = "",longi = "";

                                if (!latLongi.equalsIgnoreCase("")){
                                    String[] latLongArr = latLongi.split(",");
                                     lat = latLongArr[0];
                                     longi = latLongArr[1];
                                }

                            //    String address = pickupAddress + ", " + pickupCity + "\n" + pickupState + "(" + pickupPincode + ")";
                                String address =   "xxxxxxxxxxxxx, " + pickupCity + "\n" + pickupState + "(" + pickupPincode + ")";

                                String date = dataObject.optString("date_of_visit");
                                String timeFrom = dataObject.optString("time_of_visit_from");
                                String timeTo = dataObject.optString("time_of_visit_to");

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
                                System.out.println("Current time => " + c);

                                SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                String currentDate = df.format(c);

                                String branchMobile = dataObject.optString("branch_mobile");
                                String branch_name = dataObject.optString("branch_name");
                                String ifsc_code = dataObject.optString("ifsc_code");
                                String delivery = dataObject.optString("delivery");
                                String drop_address = dataObject.optString("drop_address");

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
                                leadModel.setIfscCode(ifsc_code);
                                leadModel.setDelivery(delivery);
                                leadModel.setDropAddress(drop_address);

                                leadModel.setCurrentDate(currentDate);
                                leadModel.setLeadStatus(status);

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

    private void assignLead(String leadId) {

        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        RetrofitClient.getInstance().getApi().allocatedLead(leadId, retailerId).enqueue(new Callback<JsonObject>() {
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

    private void startJourney(String leadId, String stage, String amount, String count) {

        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        RetrofitClient.getInstance().getApi().startJourney(leadId, retailerId).enqueue(new Callback<JsonObject>() {
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

                            Bundle bundle = new Bundle();
                            bundle.putString(MyConstantKey.JOB_ID, jobId);
                            bundle.putString(MyConstantKey.SR_NO, srNo);
                            bundle.putString(MyConstantKey.MOBILE_NO, mobile);
                            bundle.putString(MyConstantKey.CUSTOMER_NAME, customer_name);
                            bundle.putString(MyConstantKey.JOB_TYPE, job_type);
                            bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                            bundle.putString(MyConstantKey.DELIVERY, delivery);
                            bundle.putString(MyConstantKey.PICKUP_ADDRESS, pickup_address);
                            bundle.putString(MyConstantKey.PICKUP_TIME, pickup_time);

                            bundle.putString(MyConstantKey.AMOUNT, amount);
                            bundle.putString(MyConstantKey.COUNT, count);
                            bundle.putString(MyConstantKey.STAGE, stage);
                            bundle.putString(MyConstantKey.LEAD_ID, leadId);

                            ReplaceFragmentUtils.replaceFragment(new PickupTrackingFragment(), bundle, (AppCompatActivity) activity);

                            pd.dismiss();
                            Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();

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

    private void setAdapter(ArrayList<LeadModel> arrayList) {

        binding.noDataFoundLy.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);

        LeadListAdapter adapter = new LeadListAdapter(arrayList, context, activity);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        adapter.MyInterface(new LeadItemClicked() {
            @Override
            public void onItemClicked(String type, String leadId, String stage, String amount, String count) {
                if (type.equalsIgnoreCase("accept")) {
                    assignLead(leadId);
                } else if (type.equalsIgnoreCase("cancelRequest")) {
                    getRemarkList(context, activity,leadId,retailerId);
                } else if (type.equalsIgnoreCase("proceedToJourney")){
                    startJourney(leadId, stage, amount, count);
                }

            }
        });

    }

}
