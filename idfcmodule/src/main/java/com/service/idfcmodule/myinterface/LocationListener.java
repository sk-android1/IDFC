package com.service.idfcmodule.myinterface;

public interface LocationListener {
    void onLocationFetched(String latitude, String longitude, boolean isGpsEnabled);
}
