/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mperezma
 */
public class LocationHelper implements LocationListener {

    private LocationHelperListener listener;
    private final LocationManager locationManager;
    private final List<String> providers = new ArrayList<String>();
    private double longitude;
    private double latitude;
    private boolean hasData;

    public LocationHelper(LocationHelperListener listener, LocationManager locationManager) {
        this.listener = listener;
        this.locationManager = locationManager;
        updateGPSInfo();
    }

    public boolean hasData() {
        updateGPSInfo();
        return hasData;
    }
    
    private void updateGPSInfo() {
        if (gotLocationInfo(LocationManager.GPS_PROVIDER)) {
            hasData = true;
            return;
        }
        if (gotLocationInfo(LocationManager.NETWORK_PROVIDER)) {
            hasData = true;
            return;
        }
        hasData = false;
    }

    private boolean gotLocationInfo(String provider) {
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            if (!providers.contains(provider)) {
                locationManager.requestLocationUpdates(provider, 0, 0, this);
                providers.add(provider);
            }
            return true;
        }
        return false;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void onLocationChanged(Location arg0) {
        updateGPSInfo();
        listener.onlocationChanged();
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        updateGPSInfo();
        listener.onlocationChanged();
    }

    public void onProviderEnabled(String arg0) {
        updateGPSInfo();
        listener.onlocationChanged();
    }

    public void onProviderDisabled(String arg0) {
        listener.onlocationChanged();
    }

    public void disable() {
        locationManager.removeUpdates(this);
    }

    public void enable() {
        updateGPSInfo();
    }


}
