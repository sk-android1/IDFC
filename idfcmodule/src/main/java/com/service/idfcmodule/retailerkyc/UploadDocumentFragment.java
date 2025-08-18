package com.service.idfcmodule.retailerkyc;

import static android.app.Activity.RESULT_OK;
import static com.service.idfcmodule.IdfcMainActivity.comType;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentUploadDocumentBinding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.utils.FileUtil;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UploadDocumentFragment extends Fragment {

    FragmentUploadDocumentBinding binding;

    Activity activity;
    Context context;

    MultipartBody.Part pvFile, addressFile;

    String whichBtnClicked = "";

    String selectedAddType = "";

    String networkStatus = "";

    ArrayList<String> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUploadDocumentBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        if (comType.equalsIgnoreCase("Vidcom")) {
            binding.tvSubmit.setBackgroundResource(R.drawable.button_on_dec2);
        }

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        getAddProofList();
        clickEvents();

        return binding.getRoot();

    }

    private void chooseFile() {

        String[] mimeTypes = {"application/pdf", "image/*"};
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        //   intent.setType("*/*"); // for choose all type file
        intent.setType("application/pdf image/*"); // for choose only pdf and image file
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        //  intent.addCategory(Intent.CATEGORY_OPENABLE);

        uploadDocumentLauncher.launch(intent);


    }

    private void clickEvents() {

        //         list = new ArrayList<>();
//        list.add("Aadhaar Card ");        // get by api
//        list.add("Pan Card");
//        list.add("Rent Agreement");
//        list.add("Voter ID Card");
//        list.add("Passport");
//        list.add("Electricity Bill");
//        list.add("Driving Licence");
//        list.add("Ration Card");

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, list);
//        binding.autoAdd.setAdapter(adapter);

        binding.autoAdd.setOnClickListener(v1 -> {
            binding.autoAdd.showDropDown();
        });

        binding.autoAdd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedAddType = list.get(position);
                binding.tvUploadLocalAdd.setVisibility(View.VISIBLE);
                binding.tvUploadLocalAdd.setText("Select " + selectedAddType);
                binding.imgAddress.setImageResource(R.drawable.baseline_cloud_upload_24);
            }
        });

        binding.tvUploadPv.setOnClickListener(v -> {
            whichBtnClicked = "pvClicked";

            chooseFile();

        });

        binding.tvUploadLocalAdd.setOnClickListener(v -> {
            whichBtnClicked = "addClicked";

            if (!selectedAddType.equalsIgnoreCase("")) {
                chooseFile();
            } else {
                //   binding.autoAdd.setError("Please select address type");
                Toast.makeText(activity, "Please select address type", Toast.LENGTH_SHORT).show();
            }


        });

        binding.tvSubmit.setOnClickListener(v -> {
            if (networkStatus.equalsIgnoreCase("Connected")) {
                if (checkInput()) {
                    uploadFile(pvFile, addressFile);
                }
            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }

        });
    }

    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<Intent> uploadDocumentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getResultCode() == RESULT_OK) {
            try {
                assert result.getData() != null;
                Uri uri = result.getData().getData();
                File file = FileUtil.from(context, uri);
                RequestBody reqBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);

                if (file.exists()) {
                    assert uri != null;
                    if (whichBtnClicked.equalsIgnoreCase("pvClicked")) {
                        //  binding.tvUploadPv.setText("PV Selected");
                        binding.tvUploadPv.setText(FileUtil.getFileName(context, uri));
                        binding.imgPv.setImageResource(R.drawable.checked_checkbox);
                        pvFile = MultipartBody.Part.createFormData("pv_doc", file.getName(), reqBody);
                    } else {
                        //   binding.tvUploadLocalAdd.setText(selectedAddType + " Selected");
                        binding.tvUploadLocalAdd.setText(FileUtil.getFileName(context, uri));
                        binding.imgAddress.setImageResource(R.drawable.checked_checkbox);
                        addressFile = MultipartBody.Part.createFormData("address_doc", file.getName(), reqBody);
                    }
                } else {
                    Toast.makeText(activity, "file not exist", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    private void getAddProofList() {
        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RetrofitClient.getInstance().getApi().getAddProofList().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                        boolean status = responseObject.getBoolean("status");
                        String message = responseObject.getString("message");

                        if (status) {

                            JSONArray dataArray = responseObject.getJSONArray("data");

                            list = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                String proofType = dataArray.getString(i);
                                list.add(proofType);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, list);
                            binding.autoAdd.setAdapter(adapter);
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

    private void uploadFile(MultipartBody.Part pvDoc, MultipartBody.Part addressDoc) {
        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RequestBody agentId = RequestBody.create(MediaType.parse("text/plain"), retailerId);
        RequestBody addressType = RequestBody.create(MediaType.parse("text/plain"), selectedAddType);
        RequestBody revision = RequestBody.create(MediaType.parse("text/plain"), IdfcMainActivity.revision);

        RetrofitClient.getInstance().getApi().uploadDocument(pvDoc, agentId, addressType, revision, addressDoc)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                                boolean status = responseObject.getBoolean("status");
                                String message = responseObject.getString("message");

                                if (status) {

//                                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//                                    LayoutInflater inflater = LayoutInflater.from(context);
//                                    View convertView = inflater.inflate(R.layout.error_view, null);
//                                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//                                    ConstraintLayout layout = convertView.findViewById(R.id.TitleLayout);
//                                    layout.setBackgroundTintList(context.getResources().getColorStateList(R.color.green));
//
//                                    TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
//                                    ImageView ivClose = convertView.findViewById(R.id.iv_close);
//                                 //   ivClose.setOnClickListener(v -> alertDialog.dismiss());
//                                    TextView okayBtn = convertView.findViewById(R.id.okayBtn);
//                                    okayBtn.setOnClickListener(v -> {
//                                        alertDialog.dismiss();
//                                        ReplaceFragmentUtils.replaceFragment(new FinishFragment(), new Bundle(), (AppCompatActivity) activity);
//                                    });
//                                    showErrorTT.setText(message);
//                                    alertDialog.setView(convertView);
//                                    alertDialog.setCancelable(false);
//                                    alertDialog.show();

                                    Snackbar.make(binding.mainLy, message, Snackbar.LENGTH_LONG).show();
                                    ReplaceFragmentUtils.replaceFragment(new FinishFragment(), new Bundle(), (AppCompatActivity) activity);

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
                                MyErrorDialog.somethingWentWrongDialog(activity);
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

    private boolean checkInput() {

        if (!binding.tvUploadPv.getText().toString().equalsIgnoreCase("Select PV")) {
            if (!binding.autoAdd.getText().toString().equalsIgnoreCase("Address Type")) {
                if (!binding.tvUploadLocalAdd.getText().toString().equalsIgnoreCase("Select " + selectedAddType)) {
                    return true;
                } else {
                    //   MyErrorDialog.nonFinishErrorDialog(context, "Select local address document");
                    Toast.makeText(activity, "Select local address document", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                //   MyErrorDialog.nonFinishErrorDialog(context, "Select address type");
                Toast.makeText(activity, "Select address type", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            //  MyErrorDialog.nonFinishErrorDialog(context, "Select police verification document");
            Toast.makeText(activity, "Select police verification document", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

}