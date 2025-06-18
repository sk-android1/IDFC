package com.service.idfcmodule.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConverterUtils {

    public static String waitingTime(String date, String dateFormat) throws ParseException {
        long milliseconds = ConverterUtils.dateToMilliSeconds(date, dateFormat);

        long currentTime = System.currentTimeMillis();

        long waitingTime = milliseconds-currentTime;

        long totalSecond = waitingTime/1000;

        long hours = totalSecond/3600;
        long remainingSecond = totalSecond-(hours*3600);
        long minutes = remainingSecond/60;
        long second = remainingSecond-(minutes*60);

        return hours +" Hr " + minutes + " mins ";
    }

    public static long dateToMilliSeconds(String date, String dateFormat) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat f = new SimpleDateFormat(dateFormat);
        Date parseDate = f.parse(date);
        long milliseconds = parseDate.getTime();

        return milliseconds;
    }

    public static String currentDate(){
        long milliseconds = System.currentTimeMillis(); // Example: January 1, 2021 at midnight

        Date date = new Date(milliseconds);
      //  SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a");
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");


        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    public static String capitaliseString(String value) {
        StringBuilder sb = new StringBuilder(value);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public static void clearJSONArray(JSONArray jsonArray) {
        for (int i = jsonArray.length() - 1; i >= 0; i--) {
            jsonArray.remove(i);
        }
    }

    public static JSONArray removeFromJsonArray(JSONArray originalArray, int removeIndex) {
        JSONArray newArray = new JSONArray();

        for (int i = 0; i < originalArray.length(); i++) {
            if (i == removeIndex) continue; // skip the index to remove
            try {
                newArray.put(originalArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newArray;
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String geoCodeToAddress(Context context, double lat, double longi){
         String addressStr = "";

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, longi, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                 addressStr = address.getAddressLine(0);

            } else {
                addressStr = "No address found";

            }
        } catch (IOException e) {
            e.printStackTrace();
            addressStr = "Geocoder service not available";

        }

         return addressStr;

    }

}
