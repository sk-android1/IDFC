package com.service.idfcmodule.lead;

import static com.service.idfcmodule.IdfcMainActivity.appVersion;
import static com.service.idfcmodule.IdfcMainActivity.comType;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.service.idfcmodule.R;
import com.service.idfcmodule.adaptors.BankListAdapter;
import com.service.idfcmodule.databinding.FragmentBankListBinding;
import com.service.idfcmodule.models.BankModel;
import com.service.idfcmodule.myinterface.BankItemClicked;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BankListFragment extends Fragment {

    FragmentBankListBinding binding;
    String networkStatus = "";

    Context context;
    Activity activity;

    String selectedBankId = "";
    ArrayList<BankModel> bankList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().setTheme(comType.equalsIgnoreCase("Vidcom") ? R.style.vidcom : R.style.relipay);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBankListBinding.inflate(inflater);

        if (comType.equalsIgnoreCase("Vidcom")) {
            ImageViewCompat.setImageTintList(binding.leadTopLy.imgBack, ColorStateList.valueOf(getResources().getColor(R.color.vidcom_color)));
        }

        context = requireContext();
        activity = requireActivity();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        if (networkStatus.equalsIgnoreCase("Connected")) {
            getBankList();
            saveAppVersion();
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

        binding.tvNext.setOnClickListener(v -> {

            if (networkStatus.equalsIgnoreCase("Connected")) {
                if (!selectedBankId.equalsIgnoreCase("")) {
                    Bundle bundle = new Bundle();
                    bundle.putString(MyConstantKey.BANK_ID, selectedBankId);
                    ReplaceFragmentUtils.replaceFragment(new LeadListFragment(), bundle, (AppCompatActivity) activity);
                    selectedBankId = "";
                } else {
                    Toast.makeText(context, "Please select Bank", Toast.LENGTH_SHORT).show();
                }
            } else {
                MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
            }

        });

    }

    private void saveAppVersion() {

//        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
//        pd.show();

        RetrofitClient.getInstance().getApi().saveAppVersion(appVersion, retailerId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {

                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {

                            //    pd.dismiss();
                            //    Snackbar.make(binding.mainLayout, "version saved", Snackbar.LENGTH_LONG).show();

                        } else {
                            //  pd.dismiss();
                            Snackbar.make(binding.mainLayout, "version error", Snackbar.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        //  pd.dismiss();
                        Snackbar.make(binding.mainLayout, "version error", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    //  pd.dismiss();

                    Snackbar.make(binding.mainLayout, "version error", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                //   pd.dismiss();
                Snackbar.make(binding.mainLayout, "version error", Snackbar.LENGTH_LONG).show();

            }
        });

    }

    private void getBankList() {

        AlertDialog pd = MyProgressDialog.createAlertDialogDsb(context);
        pd.show();

        RetrofitClient.getInstance().getApi().getBankList().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {

                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        int statusCode = responseObject.getInt("statuscode");

                        String message = responseObject.getString("message");

                        if (statusCode == 200) {

                            JSONArray dataArray = responseObject.getJSONArray("data");

                            bankList = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {

                                JSONObject dataObject = dataArray.getJSONObject(i);
                                String bankName = dataObject.optString("name");
                                //   bankName = "Au Small Finance Bank";
                                String bankId = dataObject.optString("id");
                                //   bankId = "13";
                                String bankLogoUrl = dataObject.optString("image");
                                //    bankLogoUrl = "https://companieslogo.com/img/orig/AUBANK.NS-1d52c885.png?t=1720244490";

                                BankModel bankModel = new BankModel();
                                bankModel.setBankName(bankName);
                                bankModel.setBankId(bankId);
                                bankModel.setLogoUrl(bankLogoUrl);

                                bankList.add(bankModel);

                            }

                            if (!bankList.isEmpty()) {
                                setAdapter();
                            }

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

    private void setAdapter() {

        BankListAdapter adapter = new BankListAdapter(bankList, context, activity);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        adapter.MyInterface(new BankItemClicked() {
            @Override
            public void onItemClicked(String bankId) {
                selectedBankId = bankId;
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.BANK_ID, selectedBankId);
                ReplaceFragmentUtils.replaceFragment(new LeadListFragment(), bundle, (AppCompatActivity) activity);
            }
        });

    }

}