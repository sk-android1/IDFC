package com.service.idfcmodule.lead;

import static android.app.Activity.RESULT_OK;
import static android.os.Looper.getMainLooper;

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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.DynamicLayoutBinding;

import com.service.idfcmodule.databinding.FragmentDeliveredChequeUploadNew2Binding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.utils.AddressFetcherService;
import com.service.idfcmodule.utils.BitmapUtils;
import com.service.idfcmodule.utils.ConverterUtils;
import com.service.idfcmodule.utils.GPSTracker;
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

public class DeliveredChequeUploadFragmentNew2 extends Fragment {

    FragmentDeliveredChequeUploadNew2Binding binding;
    DynamicLayoutBinding binding2;

    private List<DynamicLayoutBinding> dynamicViewsList = new ArrayList<>();

    Activity activity;
    Context context;

    String currentPhotoPath, networkStatus = "", count = "", amount = "", leadId = "", jobId = "", srNo = "", jobSubType = "",customerName = "", bankName, chequeNo, chequeAmt;
    File filReceiptImage;

    MultipartBody.Part[] uploadImagesArr, uploadBankNameArr, uploadChqNoArr, uploadChqAmtArr;
    //  List<RequestBody>  uploadChqNoArr = new ArrayList<>(), uploadChqAmtArr = new ArrayList<>();

    JSONArray imgJsonArray, bankJsonArray, chqNoJsonArray, chqAmountJsonArray;

    int intCount;

    String reAttempt = "0";

    ArrayList<String> bankList;
    int plusClicked = 0;

    // for location
    private Location currentLocation;
    private final int LOCATION_PERMISSION = 101;
    String latitude = "", longitude = "", address = "", pinCode = "";

    ///////////////

    String whichButtonClicked = "";

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDeliveredChequeUploadNew2Binding.inflate(inflater);

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        activity = requireActivity();
        context = requireContext();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        if (getArguments() != null) {

            jobId = getArguments().getString(MyConstantKey.JOB_ID, "");
            srNo = getArguments().getString(MyConstantKey.SR_NO, "");
            count = getArguments().getString(MyConstantKey.COUNT, "");
            amount = getArguments().getString(MyConstantKey.AMOUNT, "");
            leadId = getArguments().getString(MyConstantKey.LEAD_ID, "");
            jobSubType = getArguments().getString(MyConstantKey.JOB_SUBTYPE, "");
            reAttempt = getArguments().getString(MyConstantKey.REATTEMPT, "0");
            customerName = getArguments().getString(MyConstantKey.CUSTOMER_NAME, "0");

        }

        if (count != null && !count.equalsIgnoreCase("")) {
            intCount = Integer.parseInt(count);
        }

        if (intCount > 5) {
            binding.imgPlus.setVisibility(View.VISIBLE);
        }

        binding.tvChqAmt.setText(amount);
        binding.tvCount.setText(count);
        binding.tvJobId.setText("SR - " + srNo);

        imgJsonArray = new JSONArray();

//        for (int i = 0; i < intCount; i++) {
//
//            createDynamicLayout(i);
//
//        }

        clickEvents();

        getBankList();

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

            whichButtonClicked = "submit";

            if (networkStatus.equalsIgnoreCase("Connected")) {

                checkPermissionAndGetLocation();

            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }

        });

        binding.imgPlus.setOnClickListener(view -> {

            plusClicked++;

            if (plusClicked < intCount) {
                createDynamicLayout(plusClicked);
            } else {
                MyErrorDialog.nonFinishErrorDialog(context, "Please update the count to add more images.");
            }

        });

        binding.tvCancelReq.setOnClickListener(view -> {

            whichButtonClicked = "cancelRequest";

            if (networkStatus.equalsIgnoreCase("Connected")) {
                checkPermissionAndGetLocation();
            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }

        });

    }

    @SuppressLint("SetTextI18n")
    public void createDynamicLayout(int number) {

        DynamicLayoutBinding binding1 = DynamicLayoutBinding.inflate(getLayoutInflater());
        binding.viewFinal.addView(binding1.getRoot());
        dynamicViewsList.add(binding1);

        binding1.tvNumbering.setText((number + 1) + "");
        binding1.tvUploadImage.setText("Upload Cheque " + (number + 1));

        if (intCount > 5) {
            binding1.tvBrowse.setBackgroundResource(R.drawable.rounded_green_back);
            binding1.tvBrowse.setClickable(true);
            binding1.tvBrowse.setEnabled(true);
            binding1.imgDropdown.setVisibility(View.GONE);
        }

        binding1.layout1.setOnClickListener(view -> {

            binding1.layout1.setVisibility(View.GONE);
            binding1.imgDropdown.setVisibility(View.INVISIBLE);
            binding1.layout2.setVisibility(View.VISIBLE);
            binding1.imgDropdown.setVisibility(View.VISIBLE);

        });

        binding1.layout2.setOnClickListener(view -> {

            if (intCount < 6) {
                binding1.layout1.setVisibility(View.VISIBLE);
                binding1.imgDropdown.setVisibility(View.INVISIBLE);
                binding1.imgDropUp.setVisibility(View.VISIBLE);
                binding1.imgDropdown.setVisibility(View.INVISIBLE);
            }

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

            if (intCount < 6) {
                binding1.layout1.setVisibility(View.VISIBLE);
                binding1.imgDropdown.setVisibility(View.INVISIBLE);
                binding1.imgDropUp.setVisibility(View.VISIBLE);
            }

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

        if (intCount < 6) {
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
        } else {
            return true;
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

                if (intCount < 6) {
                    binding2.imgDropdown.setVisibility(View.VISIBLE);
                }

                binding2.imgDropUp.setVisibility(View.GONE);

                bankName = binding2.etBankName.getText().toString();
                chequeNo = binding2.etChequeNo.getText().toString();
                chequeAmt = binding2.etChequeAmount.getText().toString();

//                try {
//                    JSONObject chq_obj = new JSONObject();
//
//                    if (intCount<6)
//                    {
//                        chq_obj.put("bank_name", bankName);
//                        chq_obj.put("chq_no", chequeNo);
//                        chq_obj.put("chq_amt", chequeAmt);
//                        //  chq_obj.put("image_name",filReceiptImage.getName() );
//                        chq_obj.put("image", base64String);
//                        imgJsonArray.put(chq_obj);
//                    }
////                    else {
////                        chq_obj.put("image", base64String);
                uploadImage();
//                 //   }
//
//
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

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

    private void uploadImage() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RequestBody rbAgentId = RequestBody.create(MediaType.parse("text/plain"), retailerId);
        RequestBody rbDir = RequestBody.create(MediaType.parse("text/plain"), "lead_data");
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        RequestBody reqFileForReceipt = RequestBody.create(MediaType.parse("image/*"), filReceiptImage);
        MultipartBody.Part chequeImg = MultipartBody.Part.createFormData("data", timeStamp + filReceiptImage.getName(), reqFileForReceipt);

        String geoCode = latitude + "," + longitude;
        RequestBody rbGeoCode = RequestBody.create(MediaType.parse("text/plain"), geoCode);

        RetrofitClient.getInstance().getApi().uploadImage(rbAgentId, rbDir,rbGeoCode, chequeImg)
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

                                    binding2.tvUploadImage.setText(filReceiptImage.getName());
                                    binding2.imgTick.setVisibility(View.VISIBLE);
                                    binding2.tvBrowse.setVisibility(View.GONE);
                                    binding2.tvCancel.setVisibility(View.GONE);
                                    binding2.layout2.setClickable(false);
                                    binding2.layout2.setEnabled(false);

                                    String fileName = filReceiptImage.getName();

                                    String chequeImg = responseObject.getString("data");

                                    try {

                                        JSONObject chq_obj = new JSONObject();
                                        if (intCount > 5) {
                                            chq_obj.put("image", chequeImg);
                                            chq_obj.put("image_name", fileName);
                                        } else {
                                            chq_obj.put("bank_name", bankName);
                                            chq_obj.put("chq_no", chequeNo);
                                            chq_obj.put("chq_amt", chequeAmt);
                                            chq_obj.put("image_name", fileName);
                                            chq_obj.put("image", chequeImg);
                                        }

                                        imgJsonArray.put(chq_obj);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

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

    private void getBankList() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().bankList(retailerId, geoCode)
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

                                    for (int i = 0; i < dataArray.length(); i++) {
                                        JSONObject dataObject = dataArray.getJSONObject(i);
                                        String bankName = dataObject.getString("name");
                                        String bankId = dataObject.getString("id");

                                        bankList.add(bankName);
                                    }
                                    if (intCount < 6) {
                                        binding.imgPlus.setVisibility(View.GONE);
                                        for (int i = 0; i < intCount; i++) {

                                            createDynamicLayout(i);

                                        }
                                    } else {
                                        createDynamicLayout(plusClicked);
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

        RequestBody rbAgentId = RequestBody.create(MediaType.parse("text/plain"), retailerId);
        RequestBody rbLeadId = RequestBody.create(MediaType.parse("text/plain"), leadId);
        RequestBody rbReattempt = RequestBody.create(MediaType.parse("text/plain"), reAttempt);
        RequestBody rbCount = RequestBody.create(MediaType.parse("text/plain"), count);
        RequestBody rbAmount = RequestBody.create(MediaType.parse("text/plain"), amount);

        String geoCode = latitude + "," + longitude;
        RequestBody rbGeoCode = RequestBody.create(MediaType.parse("text/plain"), geoCode);


        RetrofitClient.getInstance().getApi().uploadDeliveredCheque(rbLeadId, rbAgentId, rbReattempt, rbCount, rbAmount, rbGeoCode, uploadImagesArr)
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
                                    bundle.putString(MyConstantKey.CUSTOMER_NAME, customerName);
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

            if (!TextUtils.isEmpty(etCount.getText().toString()) && !etCount.getText().toString().equalsIgnoreCase("0")) {
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

        String geoCode = latitude + "," + longitude;

        //    RetrofitClient.getInstance().getApi().updateCount(leadId, retailerId, strCount, strAmount, jobSubType)
        RetrofitClient.getInstance().getApi().updateCount(leadId, retailerId, count, jobSubType, geoCode)
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

                    chequeAmt = jsonObject.getString("chq_amt");

                    double iAmount = Double.parseDouble(chequeAmt);
                    iAmount2 = iAmount + iAmount2;

                    DecimalFormat df = new DecimalFormat("#");        // for remove long value scientific format
                    strTtlChqAmt = (df.format(iAmount2));

                    RequestBody devicePhotoBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), String.valueOf(jsonObject));

                    MultipartBody.Part imagePart = MultipartBody.Part.createFormData("data[]", null, devicePhotoBody);

                    uploadImagesArr[i] = imagePart;

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

    private void submitDocumentWithoutChqDetails() {
        count = binding.tvCount.getText().toString();
        intCount = Integer.parseInt(count);
        uploadImagesArr = new MultipartBody.Part[intCount];

        boolean isAllFileSelected = true;

        if (networkStatus.equalsIgnoreCase("Connected")) {

            for (int i = 0; i < imgJsonArray.length(); i++) {
                try {

                    JSONObject jsonObject = imgJsonArray.getJSONObject(i);

                    RequestBody devicePhotoBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), String.valueOf(jsonObject));

                    MultipartBody.Part imagePart = MultipartBody.Part.createFormData("data[]", null, devicePhotoBody);

                    uploadImagesArr[i] = imagePart;

                } catch (Exception e) {
                    //   Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                    isAllFileSelected = false;
                }

            }

            int arrayLength = imgJsonArray.length();

            if (isAllFileSelected) {

                uploadDeliveredDocument(leadId, "");

            } else {
                Toast.makeText(activity, "Please upload all Cheque", Toast.LENGTH_SHORT).show();
            }

        } else {
            MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
        }

    }

    private void updateDynamicLayout(String strCount) {
        plusClicked = 0;

        for (DynamicLayoutBinding binding3 : dynamicViewsList) {
            binding.viewFinal.removeView(binding3.getRoot());
        }

        dynamicViewsList.clear();

        intCount = Integer.parseInt(strCount);

        if (intCount < 6) {
            for (int i = 0; i < intCount; i++) {

                createDynamicLayout(i);

            }
        } else {
            createDynamicLayout(plusClicked);
        }

        binding.tvCount.setText(strCount);
        ConverterUtils.clearJSONArray(imgJsonArray);

        if (intCount > 5) {
            binding.imgPlus.setVisibility(View.VISIBLE);
        } else {
            binding.imgPlus.setVisibility(View.GONE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

//        if (networkStatus.equalsIgnoreCase("Connected")) {
//            checkPermissionAndGetLocation();
//        } else {
//            MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
//        }

    }


    ///////////  for location

    public void checkPermissionAndGetLocation() {

        if (checkLocationPermission()) {
            getLocationLatLang();
        }
        else {
            Snackbar.make(binding.mainLayout, "Please allow location permission", Snackbar.LENGTH_LONG).show();
        }
    }

    public void startGettingLocation() {
        FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.i("TAG", "Location result is available");
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (locationAvailability.isLocationAvailable()) {
                    Log.i("TAG", "Location is available");
                } else {
                    Log.i("TAG", "Location is unavailable");
                }
            }
        };

        if (checkLocationPermission()) {
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Location location) {
                    currentLocation = location;
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    Log.d("TAG", "onSuccess: " + currentLocation);
                    getAddress();
                }
            });

            locationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("tag", e.getMessage());
                }
            });

        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
            return false;
        } else {
            //    getLocationLatLang();
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION) {
            for (int grantRes : grantResults) {
                if (grantRes == PackageManager.PERMISSION_DENIED) {
                    checkLocationPermission();
                    return;
                }
            }

            getLocationLatLang();
        }
    }

    public void getLocationLatLang() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        GPSTracker gps = new GPSTracker(context);
        if (isGPSEnabled) {
            if (gps.canGetLocation()) {
                latitude = String.valueOf(gps.getLatitude());
                longitude = String.valueOf(gps.getLongitude());

                if (latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("") || latitude.equalsIgnoreCase("0.0") || longitude.equalsIgnoreCase("0.0")) {
                    checkPermissionAndGetLocation();
                } else {
                    if (whichButtonClicked.equalsIgnoreCase("submit")){
                        if (intCount < 6) {
                            submitDocument();
                        } else {
                            submitDocumentWithoutChqDetails();
                        }
                    }  else if (whichButtonClicked.equalsIgnoreCase("cancelRequest")) {
                        getRemarkList(context, activity, leadId, retailerId, latitude, latitude);
                    }
                    else{
                        Snackbar.make(binding.mainLayout, "Other button clicked", Snackbar.LENGTH_LONG).show();
                    }

                }

            }
        }
        else {
            OnGPS();
        }
    }

    @SuppressLint("SetTextI18n")
    private void OnGPS() {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        View convertView = getLayoutInflater().inflate(R.layout.dsb_device_not_connected, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();

        TextView tag_line = convertView.findViewById(R.id.tag_line);
        TextView device_name = convertView.findViewById(R.id.device_name);
        Button done_btn = convertView.findViewById(R.id.done_btn);
        ImageView image_set = convertView.findViewById(R.id.image_set);

        ImageView image_close = convertView.findViewById(R.id.close);

        image_close.setOnClickListener(view -> {
            alertDialog.dismiss();
        });

        image_set.setOnClickListener(view -> {
            alertDialog.dismiss();
        });

        tag_line.setText("Location Sharing is Off!");
        device_name.setText("You need to turn your location sharing on");
        done_btn.setText("Turn Location On");
        done_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        });

    }

    private void getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(context, "GeoCoder Not Found", Toast.LENGTH_SHORT).show();
        } else {
            startAddressFetcherService();
        }
    }

    private void startAddressFetcherService() {
        Intent intent = new Intent(context, AddressFetcherService.class);
        DeliveredChequeUploadFragmentNew2.AddressResultReceiver addressResultReceiver = new DeliveredChequeUploadFragmentNew2.AddressResultReceiver(new Handler());
        intent.putExtra(MyConstantKey.RECEIVER, addressResultReceiver);
        intent.putExtra(MyConstantKey.LOCATION_DATA_EXTRA, currentLocation);
        context.startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) {
                address = resultData.getString(MyConstantKey.RESULT_DATA_KEY);
                pinCode = resultData.getString(MyConstantKey.PINCODE);
            }

        }

    }

    ////////////////////////////////

}