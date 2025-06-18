package com.service.idfcmodule.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoUploadUtils {

     Context context;
     String currentPhotoPath;
    ActivityResultLauncher<Intent> uploadImageByCamera;

    public PhotoUploadUtils(Context context, String currentPhotoPath,ActivityResultLauncher<Intent> uploadImageByCamera ) {
        this.context = context;
        this.currentPhotoPath = currentPhotoPath;
        this.uploadImageByCamera = uploadImageByCamera;
    }

    public void openCamera(int cameraType) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(context, "com.service.idfc", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", cameraType);
            if (cameraType == 1)
            {
                takePictureIntent.putExtra("camerafacing", "front");
                takePictureIntent.putExtra("previous_mode", "Selfie");
            }

            uploadImageByCamera.launch(takePictureIntent);
        }

    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }



}
