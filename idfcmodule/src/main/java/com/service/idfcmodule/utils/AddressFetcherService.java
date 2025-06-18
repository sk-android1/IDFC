package com.service.idfcmodule.utils;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressFetcherService extends IntentService {
    private static final String TAG = AddressFetcherService.class.getSimpleName();

    private ResultReceiver resultReceiver;
    private Location location;

    public AddressFetcherService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        resultReceiver = intent.getParcelableExtra(MyConstantKey.RECEIVER);

        if (resultReceiver == null) {
            Log.i(TAG, "Receiver not available");
            return;
        }

        location = intent.getParcelableExtra(MyConstantKey.LOCATION_DATA_EXTRA);

        if (location == null) {
            Log.i(TAG, "Location not available");
            respondWithResult(MyConstantKey.FAILURE_RESULT, "Location Unavailable", "");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList == null && addressList.size() == 0) {
                Log.i(TAG, "Address not available");
                respondWithResult(MyConstantKey.FAILURE_RESULT, "Location Unavailable", "");
            } else {
                StringBuilder addressString = new StringBuilder();

                Address address = addressList.get(0);
                int maxLine = address.getMaxAddressLineIndex();

                if (maxLine > 0) {
                    for (int i = 0; i < maxLine; i++) {
                        addressString.append(address.getAddressLine(i) + "\n");
                    }
                } else {
                    addressString.append(address.getAddressLine(0));
                }

                respondWithResult(MyConstantKey.SUCCESS_RESULT, addressString.toString(), address.getPostalCode());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void respondWithResult(int resultCode, String resultMessage, String pinCode) {
        Bundle bundle = new Bundle();
        bundle.putString(MyConstantKey.RESULT_DATA_KEY, resultMessage);
        bundle.putString(MyConstantKey.PINCODE, pinCode);
        resultReceiver.send(resultCode, bundle);
    }
}
