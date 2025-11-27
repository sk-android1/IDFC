package com.service.idfcmodule.lead;

import static android.app.Activity.RESULT_OK;
import static android.os.Looper.getMainLooper;
import static com.service.idfcmodule.IdfcMainActivity.retailerId;
import static com.service.idfcmodule.utils.CancelRequest.getRemarkList;

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
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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
import com.service.idfcmodule.databinding.FragmentCloseSrAcceptanceBinding;
import com.service.idfcmodule.models.BadRequestHandle;
import com.service.idfcmodule.utils.AddressFetcherService;
import com.service.idfcmodule.utils.BitmapUtils;
import com.service.idfcmodule.utils.GPSTracker;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
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

    String networkStatus = "";

    FragmentCloseSrAcceptanceBinding binding;
    Activity activity;
    Context context;

    String jobSubtype = "", leadId = "", srNo = "", timer = "", customerName;

    boolean isRunning = false;
    private long timeLeftInMillis = 0;

    File filReceiptImage;
    String currentPhotoPath;

    // for location
    private Location currentLocation;
    private final int LOCATION_PERMISSION = 101;
    String latitude = "", longitude = "", address = "", pinCode = "";

    ///////////////

    String whichButtonClicked = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            customerName = getArguments().getString(MyConstantKey.CUSTOMER_NAME, "");
        }


        binding.tvAcceptance.setText("SR No. " + srNo);

//        if (jobSubtype.equalsIgnoreCase("Cheque")) {

        binding.tvTimeStart.setVisibility(View.VISIBLE);
        binding.tvTimer.setVisibility(View.VISIBLE);
        binding.tvTimerText.setVisibility(View.VISIBLE);

        binding.tvTimer.setText(timer);

        binding.tvCustomerName.setText(customerName);

        timeLeftInMillis = Long.parseLong(timer);

        startTimer();
//        }

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

        clickEvents();

        return binding.getRoot();

    }

    private void clickEvents() {

        binding.leadTopLy.imgBack.setOnClickListener(v -> {
            // activity.onBackPressed();
            //  requireActivity().finish();

            Bundle bundle = new Bundle();
            bundle.putString(MyConstantKey.BANK_ID,LeadListFragment.bankId);
            ReplaceFragmentUtils.replaceFragment(new LeadListFragment(), bundle, (AppCompatActivity) activity);

        });

        binding.tvUploadReceipt.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        binding.tvEnquiry.setOnClickListener(view -> {

            whichButtonClicked = "closeSrEnquiry";

            if (networkStatus.equalsIgnoreCase("Connected")) {
                checkPermissionAndGetLocation();
            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
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

    private void closeSrEnq() {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().closeSrEnq(leadId, retailerId, geoCode)
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

            whichButtonClicked = "uploadReceipt";

            if (networkStatus.equalsIgnoreCase("Connected")) {

                checkPermissionAndGetLocation();

            } else {
                MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
            }

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

        String geoCode = latitude + "," + longitude;
        RequestBody rbGeoCode = RequestBody.create(MediaType.parse("text/plain"), geoCode);

        RetrofitClient.getInstance().getApi().uploadImage(rbAgentId, rbDir, rbGeoCode, chequeImg)
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

        String geoCode = latitude + "," + longitude;

        RetrofitClient.getInstance().getApi().uploadReceipt(leadId, retailerId, imageLink, geoCode)
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

    @Override
    public void onResume() {
        super.onResume();
//        if (networkStatus.equalsIgnoreCase("Connected")) {
//            checkPermissionAndGetLocation();
//        }
//        else {
//            MyErrorDialog.activityFinishErrorDialog(context, activity, networkStatus);
//        }

    }

    ///////////  for location

    public void checkPermissionAndGetLocation() {

        if (checkLocationPermission()) {
            getLocationLatLang();
        } else {
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
                    if (whichButtonClicked.equalsIgnoreCase("closeSrEnquiry")) {
                        closeSrEnq();
                    } else if (whichButtonClicked.equalsIgnoreCase("uploadReceipt")) {
                        uploadImage();
                    } else if (whichButtonClicked.equalsIgnoreCase("cancelRequest")) {
                        getRemarkList(context, activity, leadId, retailerId, latitude, longitude);
                    } else {
                        Snackbar.make(binding.mainLayout, "Other button clicked", Snackbar.LENGTH_LONG).show();
                    }

                }
            }
        } else {
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
        CloseSrAcceptanceFragment.AddressResultReceiver addressResultReceiver = new CloseSrAcceptanceFragment.AddressResultReceiver(new Handler());
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