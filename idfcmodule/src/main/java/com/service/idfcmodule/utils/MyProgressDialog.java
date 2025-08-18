package com.service.idfcmodule.utils;



import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.service.idfcmodule.R;


public class MyProgressDialog {

    public static AlertDialog createAlertDialogDsb(Context context) {

        AlertDialog pDialog = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.custom_progress_dialog_dsb, null);
        pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pDialog.setView(convertView);
        pDialog.setCancelable(false);

        ImageView gifView = convertView.findViewById(R.id.img_gif);

        Glide.with(gifView)
                .asGif()
                .load(R.drawable.loding2) // your GIF file in res/drawable
                .into(gifView);

        pDialog.show();

        return pDialog;
    }
}
