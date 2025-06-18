package com.service.idfcmodule.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.service.idfcmodule.R;

public class MyErrorDialog {

    public static void somethingWentWrongDialog(Activity activity) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        LayoutInflater inflater = activity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.something_went_wrong, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        AppCompatButton refreshButton = convertView.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.setView(convertView);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public static void nonFinishErrorDialog(Context context, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.error_view, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
        ImageView ivClose = convertView.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(v -> alertDialog.dismiss());
        TextView okayBtn = convertView.findViewById(R.id.okayBtn);
        okayBtn.setOnClickListener(v -> alertDialog.dismiss());
        showErrorTT.setText(msg);
        alertDialog.setView(convertView);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public static void activityFinishErrorDialog(Context context, Activity activity,String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.error_view, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
        ImageView ivClose = convertView.findViewById(R.id.iv_close);
        TextView okayBtn = convertView.findViewById(R.id.okayBtn);
        okayBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
            activity.finish();
        });
        showErrorTT.setText(msg);
        alertDialog.setView(convertView);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

}
