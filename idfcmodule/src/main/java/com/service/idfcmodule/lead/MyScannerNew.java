package com.service.idfcmodule.lead;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.ActivityMyScannerNewBinding;

public class MyScannerNew extends AppCompatActivity {

    ActivityMyScannerNewBinding binding;

    private CodeScanner codeScanner;

    Activity activity;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //    EdgeToEdge.enable(this);
        binding = ActivityMyScannerNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(getResources().getColor(R.color.sky_blue));

        binding.leadTopLy.custReqLy.setVisibility(View.GONE);

        activity = MyScannerNew.this;
        context = MyScannerNew.this;

        codeScanner = new CodeScanner(this, binding.scannerview);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
           runScanner();
        } else {

            requestPermissionLauncher.launch(Manifest.permission.CAMERA);

        }

        binding.scannerview.setOnClickListener(v -> {
            codeScanner.startPreview();
        });

        binding.tvCancel.setOnClickListener(v -> {
            finish();
        });

    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
           runScanner();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
            }
        }

    });

    private void runScanner() {

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //    Toast.makeText(context, result.getText(), Toast.LENGTH_SHORT).show();

                        String scannedData = result.toString();
                        Intent in = new Intent();
                        in.putExtra("scannedData", scannedData);
                        setResult(RESULT_OK,in);

                        finish();

                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

}