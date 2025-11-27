package com.service.idfcmodule.lead;

import static android.app.Activity.RESULT_OK;

import static com.service.idfcmodule.IdfcMainActivity.retailerId;

import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;
import static com.service.idfcmodule.utils.ConverterUtils.removeFromJsonArray;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.DynamicLayoutBinding;
import com.service.idfcmodule.databinding.FragmentDeliveredChequeUploadBinding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.utils.BitmapUtils;
import com.service.idfcmodule.utils.ConverterUtils;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveredChequeUploadFragment extends Fragment {

    FragmentDeliveredChequeUploadBinding binding;
    DynamicLayoutBinding binding2;

    private List<DynamicLayoutBinding> dynamicViewsList = new ArrayList<>();

    Activity activity;
    Context context;

    String currentPhotoPath, networkStatus = "", count = "", amount = "", leadId = "", jobId = "",srNo = "", jobSubType = "", bankName, chequeNo, chequeAmt;
    File filReceiptImage;

    MultipartBody.Part[] uploadImagesArr, uploadBankNameArr, uploadChqNoArr, uploadChqAmtArr;
    //  List<RequestBody>  uploadChqNoArr = new ArrayList<>(), uploadChqAmtArr = new ArrayList<>();

    JSONArray imgJsonArray, bankJsonArray, chqNoJsonArray, chqAmountJsonArray;

    int intCount;

    String reAttempt = "0";

    ArrayList<String> bankList;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDeliveredChequeUploadBinding.inflate(inflater);

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        activity = requireActivity();
        context = requireContext();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        if (getArguments() != null) {

            jobId = getArguments().getString(MyConstantKey.JOB_ID, "");
            srNo = getArguments().getString(MyConstantKey.SR_NO, "");
            count = getArguments().getString(MyConstantKey.COUNT,"");
            amount = getArguments().getString(MyConstantKey.AMOUNT,"");
            leadId = getArguments().getString(MyConstantKey.LEAD_ID,"");
            jobSubType = getArguments().getString(MyConstantKey.JOB_SUBTYPE,"");
            reAttempt = getArguments().getString(MyConstantKey.REATTEMPT, "0");

        }

        if (count != null && !count.equalsIgnoreCase("")) {
            intCount = Integer.parseInt(count);
        }

        binding.tvChqAmt.setText(amount);
        binding.tvCount.setText(count);
        binding.tvJobId.setText("SR - "+srNo);

//        uploadBankNameArr = new MultipartBody.Part[intCount];
//        uploadChqNoArr = new MultipartBody.Part[intCount];
//        uploadChqAmtArr = new MultipartBody.Part[intCount];

        imgJsonArray = new JSONArray();
     //   bankJsonArray = new JSONArray();
     //   chqNoJsonArray = new JSONArray();
      //  chqAmountJsonArray = new JSONArray();

        getBankList();

//        for (int i = 0; i < intCount; i++) {
//
//            createDynamicLayout(i);
//
//        }

        clickEvents();

        return binding.getRoot();

    }

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
            activity.onBackPressed();
        });

        binding.tvUpdateCount.setOnClickListener(v -> {
            showUpdateCountDialog();
        });

        binding.tvSubmit.setOnClickListener(v -> {


                submitDocument();

        });

        binding.tvCancelReq.setOnClickListener(view -> {
            getRemarkList(context, activity, leadId, retailerId,"","");
        });

    }

    @SuppressLint("SetTextI18n")
    public void createDynamicLayout(int number) {

        DynamicLayoutBinding binding1 = DynamicLayoutBinding.inflate(getLayoutInflater());
        binding.viewFinal.addView(binding1.getRoot());
        dynamicViewsList.add(binding1);

        binding1.tvNumbering.setText((number + 1) + "");
        binding1.tvUploadImage.setText("Upload Cheque " + (number + 1));


        binding1.layout1.setOnClickListener(view -> {

            binding1.layout1.setVisibility(View.GONE);
            binding1.imgDropdown.setVisibility(View.INVISIBLE);
            binding1.layout2.setVisibility(View.VISIBLE);
            binding1.imgDropdown.setVisibility(View.VISIBLE);

        });

        binding1.layout2.setOnClickListener(view -> {


               binding1.layout1.setVisibility(View.VISIBLE);
               binding1.imgDropdown.setVisibility(View.INVISIBLE);
               binding1.imgDropUp.setVisibility(View.VISIBLE);
               binding1.imgDropdown.setVisibility(View.INVISIBLE);


        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, bankList);
        binding1.etBankName.setAdapter(adapter);
        binding1.etBankName.setThreshold(1);

        binding1.etBankName.setOnClickListener(view -> {
            binding1.etBankName.showDropDown();
        });
        binding1.etBankName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int position = bankList.indexOf(binding1.etBankName.getText().toString());

                bankName = bankList.get(position);
              //  Toast.makeText(activity, bankName, Toast.LENGTH_SHORT).show();

                if (!TextUtils.isEmpty(binding1.etChequeNo.getText().toString()) && !TextUtils.isEmpty(binding1.etChequeAmount.getText().toString())) {
                    binding1.tvBrowse.setBackgroundResource(R.drawable.rounded_green_back);
                    binding1.tvBrowse.setClickable(true);
                    binding1.tvBrowse.setEnabled(true);
                } else {
                    binding1.tvBrowse.setBackgroundResource(R.drawable.gray_back);
                    binding1.tvBrowse.setClickable(false);
                    binding1.tvBrowse.setEnabled(false);
                }

            }
        });

        binding1.etChequeNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (!TextUtils.isEmpty(binding1.etBankName.getText().toString()) && !TextUtils.isEmpty(binding1.etChequeAmount.getText().toString()) && charSequence.length() > 0) {
                    binding1.tvBrowse.setBackgroundResource(R.drawable.rounded_green_back);
                    binding1.tvBrowse.setClickable(true);
                    binding1.tvBrowse.setEnabled(true);
                } else {
                    binding1.tvBrowse.setBackgroundResource(R.drawable.gray_back);
                    binding1.tvBrowse.setClickable(false);
                    binding1.tvBrowse.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding1.etChequeAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(binding1.etBankName.getText().toString()) && !TextUtils.isEmpty(binding1.etChequeNo.getText().toString()) && charSequence.length() > 0) {
                    binding1.tvBrowse.setBackgroundResource(R.drawable.rounded_green_back);
                    binding1.tvBrowse.setClickable(true);
                    binding1.tvBrowse.setEnabled(true);
                } else {
                    binding1.tvBrowse.setBackgroundResource(R.drawable.gray_back);
                    binding1.tvBrowse.setClickable(false);
                    binding1.tvBrowse.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding1.tvBrowse.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (checkInput(binding1)) {
                    openCamera();
                    binding1.tvUploadImage.setId(number);

                    binding2 = binding1;
                }

            } else {

                requestPermissionLauncher.launch(Manifest.permission.CAMERA);

            }
        });

        binding1.tvCancel.setOnClickListener(v -> {

            binding1.tvUploadImage.setText("Upload Cheque " + (number + 1));
            binding1.tvUploadImage.setTextColor(getResources().getColor(R.color.dark_grey_idfc));
            binding1.tvCancel.setVisibility(View.GONE);
            binding1.tvBrowse.setVisibility(View.VISIBLE);

            binding1.etBankName.setEnabled(true);
            binding1.etChequeNo.setEnabled(true);
            binding1.etChequeAmount.setEnabled(true);


               binding1.layout1.setVisibility(View.VISIBLE);
               binding1.imgDropdown.setVisibility(View.INVISIBLE);
               binding1.imgDropUp.setVisibility(View.VISIBLE);


            int id = binding1.tvUploadImage.getId();

//            try {
            //    imgJsonArray.remove(id);

            imgJsonArray = removeFromJsonArray(imgJsonArray, id);

            String s = imgJsonArray.toString();
            //   Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
//                imgJsonArray.put(id, null);
//                bankJsonArray.put(id, null);
//                chqNoJsonArray.put(id, null);
//                chqAmountJsonArray.put(id, null);
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }

        });

    }

    private boolean checkInput(DynamicLayoutBinding binding) {


            if (!TextUtils.isEmpty(binding.etBankName.getText().toString())) {
                if (!TextUtils.isEmpty(binding.etChequeNo.getText().toString())) {
                    if (!TextUtils.isEmpty(binding.etChequeAmount.getText().toString())) {
                        if (!binding.etChequeAmount.getText().toString().equalsIgnoreCase("0")) {
                            binding.tvBrowse.setBackgroundResource(R.drawable.rounded_green_back);
                            return true;
                        } else {
                            binding.etChequeAmount.setError("Invalid Amount");
                            return false;
                        }

                    } else {
                        binding.etChequeAmount.setError("Required");
                        return false;
                    }
                } else {
                    binding.etChequeNo.setError("Required");
                    return false;
                }
            } else {
                binding.etBankName.setError("Required");
                return false;
            }
        }

    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (photoFile != null) {
            Uri photoURI;

            if (IdfcMainActivity.loginType.equalsIgnoreCase("RelipaySDK"))
                photoURI = FileProvider.getUriForFile(context, MyConstantKey.PROVIDER_RELIPAY, photoFile);
            else if (IdfcMainActivity.loginType.equalsIgnoreCase("RelipayPartnerSDK"))
                photoURI = FileProvider.getUriForFile(context, MyConstantKey.PROVIDER_RELIPAY_PARTNER, photoFile);
            else if (IdfcMainActivity.loginType.equalsIgnoreCase("VidcomSDK"))
                photoURI = FileProvider.getUriForFile(context, MyConstantKey.PROVIDER_VIDCOM, photoFile);
            else
                photoURI = FileProvider.getUriForFile(context, MyConstantKey.PROVIDER_APP, photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 0);
            uploadImageByCamera.launch(takePictureIntent);
        }

    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<Intent> uploadImageByCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getResultCode() == RESULT_OK) {
            try {
                Uri uri = Uri.fromFile(new File(currentPhotoPath));
                filReceiptImage = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "cheque" + System.currentTimeMillis() + ".jpeg");
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap = BitmapUtils.rotateImageIfRequired(context, bitmap, uri);
                // bitmap = BitmapUtils.waterMark(bitmap, "23.08888", "22.9766777");
                Bitmap resizedBitmap = BitmapUtils.getResizedBitmap(bitmap, 1500);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bytes = baos.toByteArray();

                String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);

                FileOutputStream fileOutputStream = new FileOutputStream(filReceiptImage);
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
                fileOutputStream.close();
                baos.close();

                binding2.tvUploadImage.setText(filReceiptImage.getName());
                binding2.tvUploadImage.setTextColor(getResources().getColor(R.color.black));
                binding2.tvCancel.setVisibility(View.VISIBLE);
                binding2.tvBrowse.setVisibility(View.GONE);

                int btnPosition = binding2.tvUploadImage.getId();

                binding2.etBankName.setEnabled(false);
                binding2.etChequeNo.setEnabled(false);
                binding2.etChequeAmount.setEnabled(false);

                binding2.layout1.setVisibility(View.GONE);

                if (intCount < 6){
                    binding2.imgDropdown.setVisibility(View.VISIBLE);
                }
                binding2.imgDropUp.setVisibility(View.GONE);

                bankName = binding2.etBankName.getText().toString();
                chequeNo = binding2.etChequeNo.getText().toString();
                chequeAmt = binding2.etChequeAmount.getText().toString();

                try {
                    JSONObject chq_obj = new JSONObject();

                    if (intCount<6)
                    {
                        chq_obj.put("bank_name", bankName);
                        chq_obj.put("chq_no", chequeNo);
                        chq_obj.put("chq_amt", chequeAmt);
                        //  chq_obj.put("image_name",filReceiptImage.getName() );
                        chq_obj.put("image", base64String);
                    }
                    else {
                        chq_obj.put("image", base64String);
                    }



                    imgJsonArray.put(chq_obj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //    imgJsonArray.put(btnPosition, filReceiptImage);
//                bankJsonArray.put(btnPosition, bankName);
//                chqNoJsonArray.put(btnPosition, chequeNo);
//                chqAmountJsonArray.put(btnPosition, chequeAmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            openCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
        }

    });

    private void getBankList() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RetrofitClient.getInstance().getApi().bankList( retailerId,"")
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

                                    JSONArray dataArray = responseObject.getJSONArray("data");

                                    bankList = new ArrayList<>();

                                    for (int i=0; i<dataArray.length(); i++){
                                        JSONObject dataObject = dataArray.getJSONObject(i);
                                        String bankName = dataObject.getString("name");
                                        String bankId = dataObject.getString("id");

                                        bankList.add(bankName);
                                    }
                                    for (int i = 0; i < intCount; i++) {

                                        createDynamicLayout(i);

                                    }

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

    private void uploadDeliveredDocument(String leadId, String amount) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

//        String bn = uploadBankNameArr.toString();
//        String cn = uploadChqNoArr.toString();
//        String a = uploadChqAmtArr.toString();
//
//        String jbn = bankJsonArray.toString();
//        String jcn = chqNoJsonArray.toString();
//        String ja = chqAmountJsonArray.toString();

        RequestBody rbAgentId = RequestBody.create(MediaType.parse("text/plain"), retailerId);
        RequestBody rbLeadId = RequestBody.create(MediaType.parse("text/plain"), leadId);
        RequestBody rbReattempt = RequestBody.create(MediaType.parse("text/plain"), reAttempt);
        RequestBody rbCount = RequestBody.create(MediaType.parse("text/plain"), count);
        RequestBody rbAmount = RequestBody.create(MediaType.parse("text/plain"), amount);

        String geoCode = "lat" + "," + "longi";
        RequestBody rbGeoCode = RequestBody.create(MediaType.parse("text/plain"), geoCode);

//     //   RequestBody rbBankArray = RequestBody.create(MediaType.parse("text/plain"), jbn);
//        RequestBody rbBankArray =RequestBody.create(MediaType.parse("application/json; charset=utf-8"),jbn);
//        RequestBody rbChqNoArray = RequestBody.create(MediaType.parse("text/plain"), jcn);
//        RequestBody rbChqAmtArray = RequestBody.create(MediaType.parse("text/plain"), ja);
//
//        Toast.makeText(activity, bn+"\n"+cn+"\n"+a, Toast.LENGTH_SHORT).show();
//        Toast.makeText(activity, jbn+"\n"+jcn+"\n"+ja, Toast.LENGTH_SHORT).show();

        //   RetrofitClient.getInstance().getApi().uploadDeliveredDocument(rbLeadId, rbAgentId, uploadBankNameArr,uploadChqNoArr,uploadChqAmtArr,uploadImagesArr)
        RetrofitClient.getInstance().getApi().uploadDeliveredCheque(rbLeadId, rbAgentId, rbReattempt, rbCount, rbAmount,rbGeoCode, uploadImagesArr)
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
                                    Bundle bundle = new Bundle();
                                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
                                    bundle.putString(MyConstantKey.JOB_ID, jobId);
                                    bundle.putString(MyConstantKey.SR_NO, srNo);
                                    bundle.putString(MyConstantKey.COUNT, count);
                                    bundle.putString(MyConstantKey.AMOUNT, amount);
                                    bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                                    //    ReplaceFragmentUtils.replaceFragment(new DeliveryBranchListFragment(), bundle, (AppCompatActivity) activity);
                                    ReplaceFragmentUtils.replaceFragment(new CaseEnquiryFragment(), bundle, (AppCompatActivity) activity);
                                    pDialog.dismiss();

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();
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

    private void showUpdateCountDialog() {

        androidx.appcompat.app.AlertDialog updateCountDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.update_count_dialog, null);
        updateCountDialog.getWindow().setBackgroundDrawableResource(R.drawable.border_edit2);

        EditText etCount = convertView.findViewById(R.id.tvCount);
        EditText etAmount = convertView.findViewById(R.id.etAmount);
        TextView tvSubmit = convertView.findViewById(R.id.tvSubmit);
        TextView tvCancel = convertView.findViewById(R.id.tvCancel);

        tvSubmit.setOnClickListener(v -> {

            if (!TextUtils.isEmpty(etCount.getText().toString())) {
                //  if (!TextUtils.isEmpty(etAmount.getText().toString())){
                updateCount(etCount, etAmount);
                updateCountDialog.dismiss();
//                }else {
//                    etAmount.setError("Required");
//                }
            } else {
                etCount.setError("Required");
            }

        });

        tvCancel.setOnClickListener(v -> {
            updateCountDialog.dismiss();
        });

        updateCountDialog.setCancelable(false);
        updateCountDialog.setView(convertView);

        updateCountDialog.show();
    }

    private void updateCount(EditText etCount, EditText etAmount) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        count = etCount.getText().toString().trim();
        String strAmount = etAmount.getText().toString().trim();

        //    RetrofitClient.getInstance().getApi().updateCount(leadId, retailerId, strCount, strAmount, jobSubType)
        RetrofitClient.getInstance().getApi().updateCount(leadId, retailerId, count, jobSubType,"")
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

                                    updateDynamicLayout(count);

                                    pDialog.dismiss();

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();
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

    private void submitDocument() {

        count = binding.tvCount.getText().toString();
        intCount = Integer.parseInt(count);
        uploadImagesArr = new MultipartBody.Part[intCount];

        double iAmount2 = 0.0;
        String strTtlChqAmt = "";

        boolean isAllFileSelected = true;

        if (networkStatus.equalsIgnoreCase("Connected")) {

            for (int i = 0; i < imgJsonArray.length(); i++) {
                try {

                    JSONObject jsonObject = imgJsonArray.getJSONObject(i);

                    //   String s = imgJsonArray.getString(i);

                    //     filReceiptImage = new File(s);

                    //    bankName = bankJsonArray.getString(i);
                    //   chequeNo = chqNoJsonArray.getString(i);
                    chequeAmt = jsonObject.getString("chq_amt");

                    double iAmount = Double.parseDouble(chequeAmt);
                    iAmount2 = iAmount + iAmount2;

                    DecimalFormat df = new DecimalFormat("#");        // for remove long value scientific format
                    strTtlChqAmt = (df.format(iAmount2));
                    //      Toast.makeText(activity, strTtlChqAmt, Toast.LENGTH_SHORT).show();

//                    RequestBody bankNameBody = RequestBody.create(MediaType.parse("text/plain"), bankName);
//                    RequestBody chequeNoBody = RequestBody.create(MediaType.parse("text/plain"), chequeNo);
                    //     RequestBody chequeAmtBody = RequestBody.create(MediaType.parse("text/plain"), chequeAmt);
                    //    RequestBody devicePhotoBody = RequestBody.create(MediaType.parse("image/*"), filReceiptImage);
                    RequestBody devicePhotoBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), String.valueOf(jsonObject));
                    //   MultipartBody.Part imagePart = MultipartBody.Part.createFormData("data[]", filReceiptImage.getName(), devicePhotoBody);
                    MultipartBody.Part imagePart = MultipartBody.Part.createFormData("data[]", null, devicePhotoBody);
//                    MultipartBody.Part bankNamePart = MultipartBody.Part.createFormData("bank_name[]",null, bankNameBody);
//                    MultipartBody.Part chqNoPart = MultipartBody.Part.createFormData("cheque_no[]",null, chequeNoBody);
//                    MultipartBody.Part chqAmtPart = MultipartBody.Part.createFormData("cheque_amt[]",null, chequeAmtBody);

                    uploadImagesArr[i] = imagePart;
//                    uploadBankNameArr[i] = bankNamePart;
//                    uploadChqNoArr[i] = chqNoPart;
//                    uploadChqAmtArr[i] = chqAmtPart;


                    //  uploadChqNoArr.add(chequeNoBody);
                    //  uploadChqAmtArr.add(chequeAmtBody);

                } catch (Exception e) {
                    //   Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                    isAllFileSelected = false;
                }

            }

            int arrayLength = imgJsonArray.length();

            if (isAllFileSelected && arrayLength == intCount) {

                uploadDeliveredDocument(leadId, strTtlChqAmt);

//                if (actualAmount == iAmount2) {
//                    uploadDeliveredDocument(leadId);
//                } else {
//                    MyErrorDialog.nonFinishErrorDialog(context, "Amount must be same");
//                }

            } else {
                Toast.makeText(activity, "Please upload all Cheque", Toast.LENGTH_SHORT).show();
            }

        } else {
            MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
        }

    }

    private void updateDynamicLayout(String strCount) {

        for (DynamicLayoutBinding binding3 : dynamicViewsList) {
            binding.viewFinal.removeView(binding3.getRoot());
        }

        dynamicViewsList.clear();

        intCount = Integer.parseInt(strCount);

        for (int i = 0; i < intCount; i++) {

            createDynamicLayout(i);

        }

        binding.tvCount.setText(strCount);
        ConverterUtils.clearJSONArray(imgJsonArray);

    }

    }