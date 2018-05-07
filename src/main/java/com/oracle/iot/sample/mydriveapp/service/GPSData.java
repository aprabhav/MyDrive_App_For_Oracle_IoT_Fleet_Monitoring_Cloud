package com.oracle.iot.sample.mydriveapp.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.oracle.iot.sample.mydriveapp.prefs.Constants;

public class GPSData {
    private MutableLiveData<Location> locationLiveData;
    private Location previousLocation;
    private FusedLocationProviderClient locationProviderClient = null;
    private LocationCallback locationCallback;
    private Context mAppContext;
    private static final int ONE_MINUTE = 1000 * 60 * 1;


    public GPSData(Context appContext){

        mAppContext = appContext;
        locationLiveData = new MutableLiveData<>();
        locationCallback = new LocationCallback() {
            Location newLocation;
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                newLocation = locationResult.getLastLocation();
                if(isBetterLocation(newLocation, previousLocation)){
                    locationLiveData.setValue(newLocation);
                    previousLocation = newLocation;
                }
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

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
        boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
