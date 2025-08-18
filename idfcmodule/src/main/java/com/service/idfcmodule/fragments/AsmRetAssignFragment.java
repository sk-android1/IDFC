package com.service.idfcmodule.fragments;

import static com.service.idfcmodule.IdfcMainActivity.comType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.adaptors.UserListAdaptor;
import com.service.idfcmodule.databinding.FragmentAsmRetAssignBinding;
import com.service.idfcmodule.models.UserModel;
import com.service.idfcmodule.utils.MyConstantKey;
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

public class AsmRetAssignFragment extends Fragment {

    FragmentAsmRetAssignBinding binding;

    ArrayList<UserModel> retailerList, assignedList, nonAssignedList;
    ArrayList<UserModel> selectedList;

    UserListAdaptor adaptor;

    Activity activity;
    Context context;

    String networkStatus = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);

        selectedList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAsmRetAssignBinding.inflate(inflater);

        if (comType.equalsIgnoreCase("Vidcom")) {

            binding.tvSubmit.setBackgroundResource(R.drawable.button_on_dec2);
        }

        activity = requireActivity();
        context = requireContext();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        retailerList = new ArrayList<>();

        try {
            getSetData();
            setAdaptor(nonAssignedList);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (networkStatus.equalsIgnoreCase("Connected")) {
            getAgentAssignedList();
        }
        else {
            MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
        }

        binding.tvSubmit.setOnClickListener(v -> {
             selectedList = adaptor.selectedList;
            Gson gson = new GsonBuilder().create();
            JsonArray jsonArray = gson.toJsonTree(selectedList).getAsJsonArray();
            String strRtlList = jsonArray.toString();

          if (networkStatus.equalsIgnoreCase("Connected")) {
              if (!selectedList.isEmpty()) {
                  saveAgentByAsm(strRtlList);
              } else {
                  MyErrorDialog.nonFinishErrorDialog(context, "Please select user");
              }
          }
          else {
              MyErrorDialog.activityFinishErrorDialog(context,activity, networkStatus);
          }

        });

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

    private void filter(String text) {
        ArrayList<UserModel> newFilterList = new ArrayList<>();
        for (UserModel item : retailerList) {
            if (item.getUsername().toLowerCase().contains(text.toLowerCase()) || item.getPhone().toLowerCase().contains(text.toLowerCase()) || item.getName().toLowerCase().contains(text.toLowerCase())) {
                newFilterList.add(item);
            }

        }

       setAdaptor(newFilterList);

    }

    private void saveAgentByAsm(String assignRetList) {
        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);

        RetrofitClient.getInstance().getApi().saveAgentByAsm(IdfcMainActivity.asmId, IdfcMainActivity.bankId, assignRetList)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            try {

                                JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                                int statusCode = responseObject.getInt("statuscode");

                                String message = responseObject.getString("message");

                                if (statusCode == 200) {
                                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                    LayoutInflater inflater = LayoutInflater.from(context);
                                    View convertView = inflater.inflate(R.layout.error_view, null);
                                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                    ConstraintLayout layout = convertView.findViewById(R.id.TitleLayout);
                                    layout.setBackgroundTintList(context.getResources().getColorStateList(R.color.black));

                                    TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
                                    ImageView ivClose = convertView.findViewById(R.id.iv_close);
                                    //   ivClose.setOnClickListener(v -> alertDialog.dismiss());
                                    TextView okayBtn = convertView.findViewById(R.id.okayBtn);
                                    okayBtn.setOnClickListener(v -> {
                                        alertDialog.dismiss();
                                        getAgentAssignedList();
                                        binding.etSearch.setText("");
                                    });
                                    showErrorTT.setText(message);
                                    alertDialog.setView(convertView);
                                    alertDialog.setCancelable(false);
                                    alertDialog.show();
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

    private void getAgentAssignedList() {
        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        RetrofitClient.getInstance().getApi().agentAssignedList(IdfcMainActivity.asmId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {

                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {
                            JSONArray dataArray = responseObject.getJSONArray("data");
                            assignedList = new ArrayList<>();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObject = dataArray.getJSONObject(i);
                                String agentId = dataObject.getString("agent_id");
                                String name = dataObject.optString("name");
                                String mobileNo = dataObject.getString("mobile");
                                String pan = dataObject.getString("pan");
                                String email = dataObject.getString("email");

                                for (int j = 0; j < nonAssignedList.size(); j++) {
                                    if (agentId.equalsIgnoreCase(nonAssignedList.get(j).getUsername())) {
                                     //   retailerList.set(j, new UserModel(retailerList.get(j).getName(), retailerList.get(j).getUserName(), retailerList.get(j).getMobileNo(), retailerList.get(j).getPanNo(),retailerList.get(j).getEmail(), "Assigned"));
                                        nonAssignedList.remove(j);
                                    }
                                }
                                assignedList.add(new UserModel(name, agentId, mobileNo, pan, email, "Assigned"));

                            }

                            assignedList.addAll(nonAssignedList);
                            retailerList = assignedList;

                            setAdaptor(retailerList);
                            pd.dismiss();
                        } else {
                            MyErrorDialog.activityFinishErrorDialog(context,activity, message);
                         //   setAdaptor();
                            pd.dismiss();
                        }

                    } catch (JSONException e) {
                        pd.dismiss();
                        MyErrorDialog.activityFinishErrorDialog(context,activity, "Something went wrong");
                      //  setAdaptor();
                    }
                } else {
                    pd.dismiss();
                    MyErrorDialog.activityFinishErrorDialog(context,activity, "Something went wrong");
                 //   setAdaptor();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pd.dismiss();
                MyErrorDialog.activityFinishErrorDialog(context,activity, "Something went wrong");
            //    setAdaptor();

            }
        });
    }

    public void getSetData() throws JSONException {

        JSONArray jsonArray = new JSONArray(IdfcMainActivity.strJsonArray);

        nonAssignedList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String name = jsonObject.getString(MyConstantKey.NAME);
            String userName = jsonObject.getString(MyConstantKey.USER_NAME);
            String mobileNo = jsonObject.getString(MyConstantKey.MOBILE_NO);
            String panNo = jsonObject.getString(MyConstantKey.PAN_NO);
            String email = jsonObject.getString(MyConstantKey.EMAIL);

        //    retailerList.add(new UserModel(name, userName, mobileNo, panNo, email));
            nonAssignedList.add(new UserModel(name, userName, mobileNo, panNo, email));

        }

    }

    private void setAdaptor(ArrayList<UserModel> retailerList) {
        adaptor = new UserListAdaptor(retailerList, context, false, selectedList);
        LinearLayoutManager manager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.recyclerView.setAdapter(adaptor);
        binding.recyclerView.setLayoutManager(manager);
    }

}