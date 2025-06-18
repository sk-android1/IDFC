package com.service.idfcmodule.fragments;

import static com.service.idfcmodule.IdfcMainActivity.comType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.adaptors.UserListAdaptor;
import com.service.idfcmodule.databinding.FragmentAsmRetAssignListBinding;
import com.service.idfcmodule.models.UserModel;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AsmRetAssignListFragment extends Fragment {

    FragmentAsmRetAssignListBinding binding;
    Context context;
    Activity activity;

    UserListAdaptor adaptor;
    ArrayList<UserModel> retailerList;

    String networkStatus = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAsmRetAssignListBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        if (networkStatus.equalsIgnoreCase("Connected")){
            getAgentAssignedList();
        }
        else {
            MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
        }

        binding.imgClear.setOnClickListener(v -> {
            binding.etSearch.setText("");
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >0) {
                    binding.imgClear.setVisibility(View.VISIBLE);
                }
                else {
                    binding.imgClear.setVisibility(View.GONE);
                }
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    private void getAgentAssignedList() {
        AlertDialog pd = MyProgressDialog.createAlertDialog(context);
        pd.show();

        RetrofitClient.getInstance().getApi().agentAssignedList(IdfcMainActivity.asmId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        pd.dismiss();
                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {
                            JSONArray dataArray = responseObject.getJSONArray("data");

                            retailerList = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObject = dataArray.getJSONObject(i);
                                String userName = dataObject.getString("agent_id");
                                String agentName = dataObject.getString("name");
                                String active = dataObject.getString("active");
                                String status = dataObject.getString("status");
                                String adminApproval = dataObject.getString("admin_approval");

                                UserModel model = new UserModel();
                                model.setName(agentName);
                                model.setUsername(userName);
                                model.setActive(active);
                                model.setStatus(status);
                                model.setAdminApproval(adminApproval);

                                retailerList.add(model);

                            }

                            if (!retailerList.isEmpty()){
                                setAdaptor(retailerList);
                            }
                            else {
                                MyErrorDialog.activityFinishErrorDialog(context,activity, message);
                            }

                        } else {
                            MyErrorDialog.activityFinishErrorDialog(context,activity, message);

                        }

                    } catch (JSONException e) {
                        pd.dismiss();
                        MyErrorDialog.activityFinishErrorDialog(context,activity, "Something went wrong");

                    }
                } else {
                    pd.dismiss();
                    MyErrorDialog.activityFinishErrorDialog(context,activity, "Something went wrong");

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pd.dismiss();
                MyErrorDialog.activityFinishErrorDialog(context,activity, "Something went wrong");

            }
        });
    }

    private void filter(String text) {
        ArrayList<UserModel> newFilterList = new ArrayList<>();
        for (UserModel item : retailerList) {
            if (item.getUsername().toLowerCase().contains(text.toLowerCase()) || item.getName().toLowerCase().contains(text.toLowerCase())) {
                newFilterList.add(item);
            }

        }

      setAdaptor(newFilterList);

    }

    private void setAdaptor(ArrayList<UserModel> arrayList) {
        adaptor = new UserListAdaptor(arrayList, context, true, null);
        LinearLayoutManager manager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.recyclerView.setAdapter(adaptor);
        binding.recyclerView.setLayoutManager(manager);
    }

}