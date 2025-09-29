package com.service.idfcmodule.lead;

import static android.app.Activity.RESULT_OK;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;
import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.service.idfcmodule.databinding.FragmentCloseSrAcceptanceBinding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.utils.BitmapUtils;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.service.idfcmodule.web_services.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CloseSrAcceptanceFragment extends Fragment {

    FragmentCloseSrAcceptanceBinding binding;
    Activity activity;
    Context context;

    String jobSubtype = "", leadId = "",srNo = "", timer = "";

    boolean isRunning = false;
    private long timeLeftInMillis = 0;

    File filReceiptImage;
    String currentPhotoPath;

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

        binding = FragmentCloseSrAcceptanceBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        if (getArguments() != null) {
            jobSubtype = getArguments().getString(MyConstantKey.JOB_SUBTYPE, "");
            leadId = getArguments().getString(MyConstantKey.LEAD_ID, "");
            srNo = getArguments().getString(MyConstantKey.SR_NO, "");
            timer = getArguments().getString(MyConstantKey.TIMER, "");
        }

        binding.tvAcceptance.setText("SR No. "+srNo);

//        if (jobSubtype.equalsIgnoreCase("Cheque")) {

            binding.tvTimeStart.setVisibility(View.VISIBLE);
            binding.tvTimer.setVisibility(View.VISIBLE);
            binding.tvTimerText.setVisibility(View.VISIBLE);

            binding.tvTimer.setText(timer);

            timeLeftInMillis = Long.parseLong(timer);

            startTimer();
//        }

        clickEvents();

        return binding.getRoot();

    }

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
           // activity.onBackPressed();
          //  requireActivity().finish();
            ReplaceFragmentUtils.replaceFragment(new BankListFragment(), new Bundle(), (AppCompatActivity) activity);
        });

        binding.tvUploadReceipt.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        binding.tvEnquiry.setOnClickListener(view -> {
            closeSrEnq();
        });

        binding.tvCancelReq.setOnClickListener(view -> {
            getRemarkList(context, activity, leadId, retailerId);
        });

    }

    private void closeSrEnq() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RetrofitClient.getInstance().getApi().closeSrEnq(leadId, retailerId)
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

                                    ReplaceFragmentUtils.replaceFragment(new CloseSRFragment(), new Bundle(), (AppCompatActivity) activity);

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();

                                    pDialog.dismiss();

                                } else {
                                    MyErrorDialog.nonFinishErrorDialog(context, message);
                                    pDialog.dismiss();
                                }

                            } catch (JSONException e) {
                                MyErrorDialog.somethingWentWrongDialog(activity);
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
                        MyErrorDialog.somethingWentWrongDialog(activity);
                        pDialog.dismiss();
                    }
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

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            openCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
        }

    });

    ActivityResultLauncher<Intent> uploadImageByCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getResultCode() == RESULT_OK) {
            try {
                Uri uri = Uri.fromFile(new File(currentPhotoPath));
                filReceiptImage = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "receipt" + System.currentTimeMillis() + ".jpeg");
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

                showConfirmDialog();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    });

    @SuppressLint("SetTextI18n")
    private void showConfirmDialog() {

        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.lead_confirm_dialog, null);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_back);

        TextView textHeading = convertView.findViewById(R.id.textHeading);
        TextView textMessage = convertView.findViewById(R.id.textMessage);
        TextView tvCancel = convertView.findViewById(R.id.tvCancel);

        TextView tvYes = convertView.findViewById(R.id.tvYes);

            textHeading.setText("Please Confirm!!!");
            textMessage.setVisibility(View.VISIBLE);
            textMessage.setText("Do you want to submit this image?");
            tvYes.setText("Submit");
            tvCancel.setText("Cancel");

        tvCancel.setOnClickListener(v -> alertDialog.dismiss());
        tvYes.setOnClickListener(v -> {
            alertDialog.dismiss();
            uploadImage();

        });

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();
    }

    private void uploadImage() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RequestBody rbAgentId = RequestBody.create(MediaType.parse("text/plain"), retailerId);
        RequestBody rbDir = RequestBody.create(MediaType.parse("text/plain"), "slip");
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        RequestBody reqFileForReceipt = RequestBody.create(MediaType.parse("image/*"), filReceiptImage);
        MultipartBody.Part chequeImg = MultipartBody.Part.createFormData("data", timeStamp + filReceiptImage.getName(), reqFileForReceipt);

        RetrofitClient.getInstance().getApi().uploadImage(rbAgentId,rbDir, chequeImg)
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

                                    String chequeImg = responseObject.getString("data");

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();
                                   uploadReceipt(chequeImg);
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

    private void uploadReceipt(String imageLink) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RetrofitClient.getInstance().getApi().uploadReceipt(leadId,retailerId, imageLink)
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

                                    Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();
                                    ReplaceFragmentUtils.replaceFragment(new CloseSRFragment(), new Bundle(), (AppCompatActivity) activity);
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

    private void startTimer() {
        if (!isRunning) {
             new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateTimerText();
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onFinish() {
                    isRunning = false;
                    binding.tvTimer.setText("00:00:00");
                    binding.tvUploadReceipt.setVisibility(View.VISIBLE);
                }
            }.start();

            isRunning = true;
        }

    }

    private void updateTimerText() {

        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) (timeLeftInMillis / 1000 % 3600) / 60;
        int sec = (int) (timeLeftInMillis / 1000) % 60;

        @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d:%02d", hours, minutes, sec);
        binding.tvTimer.setText(time);

    }

}