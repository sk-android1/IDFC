package com.service.idfcmodule.retailerkyc;

import static android.app.Activity.RESULT_OK;
import static com.service.idfcmodule.IdfcMainActivity.comType;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.service.idfcmodule.databinding.FragmentPhotoUploadBinding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.utils.BitmapUtils;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
import com.service.idfcmodule.utils.PhotoUploadUtils;
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


public class PhotoUploadFragment extends Fragment {

    FragmentPhotoUploadBinding binding;
    Activity activity;
    Context context;

    String currentPhotoPath;
    File filReceiptImage;

    String networkStatus = "";

    PhotoUploadUtils photoUploadUtils;
    //  ActivityResultLauncher<Intent> uploadImageByCamera;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPhotoUploadBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        if (comType.equalsIgnoreCase("Vidcom")) {
            binding.tvOpenCam.setBackgroundResource(R.drawable.button_on_dec2);
            binding.tvNext.setBackgroundResource(R.drawable.button_on_dec2);
        }

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        handleClickEvents();

        //   photoUploadUtils = new PhotoUploadUtils(context,currentPhotoPath, uploadImageByCamera);

        return binding.getRoot();

    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            openCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
        }

    });

    private void handleClickEvents() {
        binding.tvOpenCam.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                openCamera();

            } else {

                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
        binding.imgCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                openCamera();

            } else {

                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
        binding.tvNext.setOnClickListener(v -> {
            if (networkStatus.equalsIgnoreCase("Connected")) {
                uploadFile();
            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
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
            else if (IdfcMainActivity.loginType.equalsIgnoreCase("VidcomSDK"))
                photoURI = FileProvider.getUriForFile(context, MyConstantKey.PROVIDER_VIDCOM, photoFile);
            else
                photoURI = FileProvider.getUriForFile(context, MyConstantKey.PROVIDER_APP, photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            takePictureIntent.putExtra("camerafacing", "front");
            takePictureIntent.putExtra("previous_mode", "Selfie");
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

    ActivityResultLauncher<Intent> uploadImageByCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getResultCode() == RESULT_OK) {
            try {
                Uri uri = Uri.fromFile(new File(currentPhotoPath));
                filReceiptImage = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        , "selfie" + System.currentTimeMillis() + ".jpeg");
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap = BitmapUtils.rotateImageIfRequired(context, bitmap, uri);
                // bitmap = BitmapUtils.waterMark(bitmap, "23.08888", "22.9766777");
                Bitmap resizedBitmap = BitmapUtils.getResizedBitmap(bitmap, 1500);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bytes = baos.toByteArray();
                FileOutputStream fileOutputStream = new FileOutputStream(filReceiptImage);
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
                fileOutputStream.close();
                baos.close();

                binding.imgAgent.setImageBitmap(bitmap);
                binding.tvNext.setVisibility(View.VISIBLE);
                binding.tvOpenCam.setVisibility(View.GONE);
                binding.imgCamera.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    private void uploadFile() {
        AlertDialog pDialog = MyProgressDialog.createAlertDialog(context);

        RequestBody agentId = RequestBody.create(MediaType.parse("text/plain"), retailerId);
        RequestBody revison = RequestBody.create(MediaType.parse("text/plain"), IdfcMainActivity.revision);

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        RequestBody reqFileForReceipt = RequestBody.create(MediaType.parse("image/*"), filReceiptImage);
        MultipartBody.Part receiptFile = MultipartBody.Part.createFormData("photo", timeStamp + filReceiptImage.getName(), reqFileForReceipt);

        RetrofitClient.getInstance().getApi().uploadPhoto(agentId, revison, receiptFile)
                .enqueue(new Callback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                                boolean status = responseObject.getBoolean("status");
                                String message = responseObject.getString("message");

                                if (status) {

                                    Snackbar.make(binding.mainLy, message, Snackbar.LENGTH_LONG).show();

                                    String stage = responseObject.getString("stage");

                                    if (stage.equalsIgnoreCase("3")) {
                                        ReplaceFragmentUtils.replaceFragment(new UploadDocumentFragment(), new Bundle(), (AppCompatActivity) activity);
                                    } else {
                                        ReplaceFragmentUtils.replaceFragment(new FinishFragment(), new Bundle(), (AppCompatActivity) activity);
                                    }

                                    pDialog.dismiss();

                                } else {

                                    MyErrorDialog.nonFinishErrorDialog(context, message);

                                    binding.tvNext.setVisibility(View.GONE);
                                    binding.tvOpenCam.setVisibility(View.VISIBLE);
                                    binding.imgAgent.setImageResource(R.drawable.take_selfie);

                                    pDialog.dismiss();

                                }

                            } catch (JSONException e) {

                                MyErrorDialog.somethingWentWrongDialog(getActivity());

                                binding.tvNext.setVisibility(View.GONE);
                                binding.tvOpenCam.setVisibility(View.VISIBLE);
                                binding.imgAgent.setImageResource(R.drawable.take_selfie);

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

                            binding.tvNext.setVisibility(View.GONE);
                            binding.tvOpenCam.setVisibility(View.VISIBLE);
                            binding.imgAgent.setImageResource(R.drawable.take_selfie);

                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        MyErrorDialog.somethingWentWrongDialog(getActivity());

                        binding.tvNext.setVisibility(View.GONE);
                        binding.tvOpenCam.setVisibility(View.VISIBLE);
                        binding.imgAgent.setImageResource(R.drawable.take_selfie);

                        pDialog.dismiss();
                    }
                });

    }

}
