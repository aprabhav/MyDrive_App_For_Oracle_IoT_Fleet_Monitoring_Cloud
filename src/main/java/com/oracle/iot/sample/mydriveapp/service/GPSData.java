package com.oracle.iot.sample.mydriveapp.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.oracle.iot.sample.mydriveapp.prefs.Constants;

public class GPSData {
    private MutableLiveData<Location> locationLiveData;
    private FusedLocationProviderClient locationProviderClient = null;
    private LocationCallback locationCallback;
    private Context mAppContext;


    public GPSData(Context appContext){

        mAppContext = appContext;
        locationLiveData = new MutableLiveData<>();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //mLocation = locationResult.getLastLocation();
                locationLiveData.setValue(locationResult.getLastLocation());
            }
        };
        registerForLocationUpdates();
    }

    public LiveData<Location> getLocation(){
        return locationLiveData;
    }

    private void registerForLocationUpdates() {
        try {
            if (locationProviderClient == null) {
                locationProviderClient = LocationServices.getFusedLocationProviderClient(mAppContext);
            }
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            Looper looper = Looper.myLooper();
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, looper);
            //locationProviderClient.getLocationAvailability().

        }catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void removeUpdates() {
        if (locationProviderClient != null) {
            locationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
