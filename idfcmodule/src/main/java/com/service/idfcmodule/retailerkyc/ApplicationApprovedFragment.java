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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.FragmentApplicationApprovedBinding;
import com.service.idfcmodule.utils.BitmapUtils;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.MyErrorDialog;
import com.service.idfcmodule.utils.MyProgressDialog;
import com.service.idfcmodule.utils.NetworkUtils;
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

public class ApplicationApprovedFragment extends Fragment {

    FragmentApplicationApprovedBinding binding;

    String driveUrl =  "https://drive.google.com/viewerng/viewer?embedded=true&url=";
    String pdfUrl =  "https://projects.ciphersquare.in/icici/uploads/identitycard/REMP00001.pdf";

    Activity activity;
    Context context;
    String currentPhotoPath;
    File filReceiptImage;

    String networkStatus = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (comType.equalsIgnoreCase("Vidcom")) requireActivity().setTheme(R.style.vidcom);
        else requireActivity().setTheme(R.style.relipay);

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentApplicationApprovedBinding.inflate(inflater);

        activity = requireActivity();
        context = requireContext();

        networkStatus = NetworkUtils.getConnectivityStatusString(context);

       getSetData();
       clickEvents();

//        binding.imgRefresh.setOnClickListener(v -> {
//           setupWebViewWithUrl(binding.webView,driveUrl+pdfUrl);
//        });

     //   setupWebViewWithUrl(binding.webView,driveUrl+pdfUrl);

    //    AlertDialog pd = MyProgressDialog.createAlertDialog(requireContext());
//        binding.webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                super.onPageStarted(view, url, favicon);
//                pd.show();
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                pd.dismiss();
//                Toast.makeText(requireContext(), "Failed to load ICard", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
//            }
//
//            @Override
//            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//                Toast.makeText(requireContext(), "loading error", Toast.LENGTH_SHORT).show();
//            }
//        });

//        binding.tvBrowse.setOnClickListener(v -> {
//         //   webViewDialogShow();
//
//            if (pdfUrl.endsWith(".pdf")){
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl)));
//            }
//            else {
//                Toast.makeText(requireContext(), "It is not a pdf file", Toast.LENGTH_SHORT).show();
//            }
//
//        });

        binding.tvHome.setOnClickListener(v -> {
            requireActivity().finish();
        });

        return binding.getRoot();

    }

    // This function configures the WebView to display the PDF.
//    @SuppressLint("SetJavaScriptEnabled")
//    private void setupWebViewWithUrl(WebView webView, String url) {
//        WebSettings settings = webView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        webView.loadUrl(url);
//        webView.clearView();
//        webView.measure(100, 100);
//
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
//        settings.setDomStorageEnabled(true);
//
//        webView.setVerticalScrollBarEnabled(true);
//        webView.getSettings().setSupportZoom(true);
//        webView.setInitialScale(266);
//        settings.setUseWideViewPort(true);
//        settings.setLoadWithOverviewMode(true);
//        webView.scrollTo(940, 205);
//        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//        settings.setAllowContentAccess(true);
//        settings.setLoadsImagesAutomatically(true);
//
//    }

//    private void webViewDialogShow(){
//        Dialog dialog = new Dialog(requireContext());
//        dialog.setContentView(R.layout.webview_layout);
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//        WebView webView = dialog.findViewById(R.id.webView);
//
//        setupWebViewWithUrl(webView,driveUrl+pdfUrl);
//
//        dialog.show();
//
//    }

    @SuppressLint("SetTextI18n")
    private void clickEvents() {

        binding.rbReceived.setOnClickListener(v -> {
            if (binding.rbReceived.isChecked()) {
                binding.tvSubmit.setText("Submit");
            }
        });

        binding.rbDamaged.setOnClickListener(v -> {
            if (binding.rbDamaged.isChecked()) {
                binding.tvSubmit.setText("Upload Image");
            }
        });

        binding.tvSubmit.setOnClickListener(v -> {
           if (binding.rbReceived.isChecked() || binding.rbDamaged.isChecked()) {
               if (binding.tvSubmit.getText().toString().equalsIgnoreCase("Upload Image")){
                   if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                       openCamera();

                   } else {

                       requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                   }
               }
               else if (binding.tvSubmit.getText().toString().equalsIgnoreCase("Submit Image")){
                   if (networkStatus.equalsIgnoreCase("Connected")) {
                       uploadFile("2");
                   }
                   else {
                       MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
                   }
               }
               else {
                   if (networkStatus.equalsIgnoreCase("Connected")) {
                       uploadFile("1");
                   }
                   else {
                       MyErrorDialog.nonFinishErrorDialog(context, networkStatus);
                   }
               }
           }
           else {
               MyErrorDialog.nonFinishErrorDialog(context, "Please select delivery status");
           }
        });

        binding.tvCancel.setOnClickListener(v -> {

            binding.tvSubmit.setText("Upload Image");
            binding.rbReceived.setEnabled(true);
            binding.rbDamaged.setEnabled(true);
            binding.rbDamaged.setChecked(true);
            binding.rbDamaged.setClickable(true);
            binding.tvCancel.setVisibility(View.GONE);

        });

    }

    @SuppressLint("SetTextI18n")
    private void getSetData() {

        assert getArguments() != null;
        pdfUrl = getArguments().getString(MyConstantKey.ICARD_LINK);
        String content = getArguments().getString(MyConstantKey.CONTENT);
        String awb = getArguments().getString(MyConstantKey.AWB);
        String stage = getArguments().getString(MyConstantKey.STAGE);
        String message = "";

        assert awb != null;
        if (awb.equalsIgnoreCase("null") || awb.equalsIgnoreCase("")) {
            message = "\nAWB No. will be updated shortly";
        }

        binding.tvApproved.setText(content+message);
        binding.tvAwb.setText(awb);

        assert stage != null;
        if (stage.equalsIgnoreCase("5") || awb.equalsIgnoreCase("null")) {
            binding.awbLy.setVisibility(View.GONE);
        }

    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            openCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
        }

    });

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
                filReceiptImage = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "selfie" + System.currentTimeMillis() + ".jpeg");
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

                binding.tvSubmit.setText("Submit Image");
                binding.rbReceived.setEnabled(false);
                binding.rbDamaged.setEnabled(false);
                binding.tvCancel.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    private void uploadFile(String deliveryStatus) {

        AlertDialog pDialog = MyProgressDialog.createAlertDialogDsb(context);

        RequestBody agentId = RequestBody.create(MediaType.parse("text/plain"), retailerId);
        RequestBody bodyStatus = RequestBody.create(MediaType.parse("text/plain"), deliveryStatus);

        MultipartBody.Part receiptFile = null;
        
      if (deliveryStatus.equalsIgnoreCase("2")){
          @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
          RequestBody reqFileForReceipt = RequestBody.create(MediaType.parse("image/*"), filReceiptImage);
           receiptFile = MultipartBody.Part.createFormData("file", timeStamp + filReceiptImage.getName(), reqFileForReceipt);
      }

        RetrofitClient.getInstance().getApi().uploadDamagedCard(agentId,bodyStatus, receiptFile)
                .enqueue(new Callback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        binding.rbReceived.setEnabled(true);
                        binding.rbDamaged.setEnabled(true);
                        binding.tvSubmit.setText("Submit");
                        binding.rbReceived.setChecked(true);

                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseObject = new JSONObject(String.valueOf(response.body()));
                                boolean status = responseObject.getBoolean("status");
                                String message = responseObject.getString("message");

                                if (status) {

                                    MyErrorDialog.activityFinishErrorDialog(context,activity, message);
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

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        MyErrorDialog.somethingWentWrongDialog(getActivity());
                        pDialog.dismiss();
                        binding.rbReceived.setEnabled(true);
                        binding.rbDamaged.setEnabled(true);
                        binding.rbReceived.setChecked(true);
                        binding.tvSubmit.setText("Submit");
                    }
                });

    }

}