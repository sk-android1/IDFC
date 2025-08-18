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
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.DynamicLayout2Binding;
import com.service.idfcmodule.databinding.FragmentDeliveredDocumentUploadBinding;
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

public class DeliveredDocumentUploadFragment extends Fragment {

    FragmentDeliveredDocumentUploadBinding binding;
    DynamicLayout2Binding binding2;

    private List<DynamicLayout2Binding> dynamicViewsList = new ArrayList<>();

    Activity activity;
    Context context;

    String currentPhotoPath, networkStatus = "", count = "", leadId = "", jobId = "", srNo = "", jobSubType = "", reAttempt = "0";
    File filReceiptImage;

    MultipartBody.Part[] uploadImagesArr;

    JSONArray imgJsonArray;

    int intCount = 0;

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

        binding = FragmentDeliveredDocumentUploadBinding.inflate(inflater);

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        activity = requireActivity();
        context = requireContext();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        if (getArguments() != null) {

            jobId = getArguments().getString(MyConstantKey.JOB_ID, "");
            srNo = getArguments().getString(MyConstantKey.SR_NO, "");
            count = getArguments().getString(MyConstantKey.COUNT, "");
            leadId = getArguments().getString(MyConstantKey.LEAD_ID, "");
            jobSubType = getArguments().getString(MyConstantKey.JOB_SUBTYPE, "");
            reAttempt = getArguments().getString(MyConstantKey.REATTEMPT, "0");

        }

        if (count != null && !count.equalsIgnoreCase("")) {
            intCount = Integer.parseInt(count);
        }

        binding.tvCount.setText(count);
        binding.tvJobId.setText("SR - " + srNo);

        imgJsonArray = new JSONArray();

        for (int i = 0; i < intCount; i++) {

            createDynamicLayout(i);

        }

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
            getRemarkList(context, activity, leadId, retailerId);
        });

    }

    @SuppressLint("SetTextI18n")
    public void createDynamicLayout(int number) {

        DynamicLayout2Binding binding1 = DynamicLayout2Binding.inflate(getLayoutInflater());
        binding.viewFinal.addView(binding1.getRoot());
        dynamicViewsList.add(binding1);

        binding1.tvNumbering.setText((number + 1) + "");
        binding1.tvUploadImage.setText("Upload image " + (number + 1));

        binding1.uploadImgLy.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
                binding1.tvUploadImage.setId(number);

                binding2 = binding1;

            } else {

                requestPermissionLauncher.launch(Manifest.permission.CAMERA);

            }
        });

        binding1.tvCancel.setOnClickListener(v -> {

            binding1.tvUploadImage.setText("Upload image " + (number + 1));
            binding1.tvCancel.setVisibility(View.GONE);
            binding1.tvBrowse.setVisibility(View.VISIBLE);
            binding1.uploadImgLy.setClickable(true);

            int id = binding1.tvUploadImage.getId();

            imgJsonArray = removeFromJsonArray(imgJsonArray, id);

            String s = imgJsonArray.toString();

        });

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
                filReceiptImage = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image" + System.currentTimeMillis() + ".jpeg");
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
                binding2.tvCancel.setVisibility(View.VISIBLE);
                binding2.tvBrowse.setVisibility(View.GONE);
                binding2.uploadImgLy.setClickable(false);

                int btnPosition = binding2.tvUploadImage.getId();

                String fileName = filReceiptImage.getName();

                try {

                    JSONObject chq_obj = new JSONObject();
                    chq_obj.put("image", base64String);
                    chq_obj.put("image_name", fileName);

                    imgJsonArray.put(chq_obj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //    imgJsonArray.put(btnPosition, filReceiptImage);

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

    private void uploadDeliveredDocument(String leadId) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RequestBody rbAgentId = RequestBody.create(MediaType.parse("text/plain"), retailerId);
        RequestBody rbLeadId = RequestBody.create(MediaType.parse("text/plain"), leadId);
        RequestBody rbReattempt = RequestBody.create(MediaType.parse("text/plain"), reAttempt);
        RequestBody rbCount = RequestBody.create(MediaType.parse("text/plain"), count);

        RetrofitClient.getInstance().getApi().uploadDeliveredDocument(rbLeadId, rbAgentId, rbReattempt, rbCount, uploadImagesArr)
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
                                    bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                                    bundle.putString(MyConstantKey.COUNT, count);
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

    @SuppressLint("SetTextI18n")
    private void showUpdateCountDialog() {

        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.update_count_dialog, null);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.border_edit2);

        TextView tvUpdate = convertView.findViewById(R.id.tvChequeUpdate);
        EditText etCount = convertView.findViewById(R.id.tvCount);
        TextView tvUpdateAmount = convertView.findViewById(R.id.tvUpdateAmount);
        EditText etAmount = convertView.findViewById(R.id.etAmount);
        TextView tvSubmit = convertView.findViewById(R.id.tvSubmit);
        TextView tvCancel = convertView.findViewById(R.id.tvCancel);

        tvUpdate.setText("Update Document");
        tvUpdateAmount.setVisibility(View.GONE);
        etAmount.setVisibility(View.GONE);

        tvSubmit.setOnClickListener(v -> {

            if (!TextUtils.isEmpty(etCount.getText().toString())) {
                updateCount(etCount);
            } else {
                etCount.setError("Required");
            }

            alertDialog.dismiss();
        });

        tvCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();

    }

    private void updateCount(EditText etCount) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String strCount = etCount.getText().toString().trim();

        RetrofitClient.getInstance().getApi().updateDocumentCount(leadId, retailerId, strCount, jobSubType)
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

                                    updateDynamicLayout(strCount);

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

        boolean isAllFileSelected = true;

        count = binding.tvCount.getText().toString();
        intCount = Integer.parseInt(count);
        uploadImagesArr = new MultipartBody.Part[intCount];

        if (networkStatus.equalsIgnoreCase("Connected")) {

            for (int i = 0; i < imgJsonArray.length(); i++) {
                try {

                    JSONObject jsonObject = imgJsonArray.getJSONObject(i);

                    RequestBody devicePhotoBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
                    MultipartBody.Part imagePart = MultipartBody.Part.createFormData("data[]", null, devicePhotoBody);

                    uploadImagesArr[i] = imagePart;

                } catch (Exception e) {
                    //   Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                    isAllFileSelected = false;
                }

            }

            int arrayLength = imgJsonArray.length();

            if (isAllFileSelected && arrayLength == intCount) {
                uploadDeliveredDocument(leadId);
            } else {
                Toast.makeText(activity, "Please upload all Cheque", Toast.LENGTH_SHORT).show();
            }

        } else {
            MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
        }

    }

    private void updateDynamicLayout(String strCount) {

        for (DynamicLayout2Binding binding3 : dynamicViewsList) {
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