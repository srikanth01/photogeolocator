/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.location;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uoc.mperezma.main.R;

/**
 *
 * @author mperezma
 */
public class LocationActivity extends Activity implements LocationListener {
    
    private EditText mEditor;
    
    public LocationActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skeleton_activity);
        mEditor = (EditText) findViewById(R.id.editor);
        ((Button) findViewById(R.id.back)).setOnClickListener(mBackListener);
        updateGPSInfo();
    }


    private void updateGPSInfo() {
        String locationString = "";

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        for (String provider : lm.getProviders(false)) {
            Location l = lm.getLastKnownLocation(provider);
            locationString += getText(R.string.device) + ": " + provider + "\n";
            if (l == null) {
                locationString += getText(R.string.unavailable) + "\n";
            } else {
                lm.requestLocationUpdates(provider, 0, 0, this);
                locationString += getText(R.string.longitude) + ": " + l.getLongitude() + "\n";
                locationString += getText(R.string.latitude) + ": " + l.getLatitude() + "\n";
                if (l.hasAltitude()) {
                    locationString += getText(R.string.altitude) + ": " + l.getAltitude() + " " + getText(R.string.meters) + "\n";
                }
                if (l.hasSpeed()) {
                    locationString += getText(R.string.speed) + ": " + l.getSpeed() + " m/s\n";
                }
                if (l.hasBearing()) {
                    locationString += getText(R.string.bearing) + ": " + l.getBearing() + " " + getText(R.string.eastDegrees) + "\n";
                }
                if (l.hasAccuracy()) {
                    locationString += getText(R.string.accuracy) + ": " + l.getAccuracy() + " " + getText(R.string.meters) + "\n";
                }
                Bundle b = l.getExtras();
                if (b != null) {
                    for (String key : b.keySet()) {
                        locationString += "[" + key + "]" + ": " + b.get(key) + "\n";
                    }
                }
            }
        }

        mEditor.setText(locationString);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(this);
        super.onDestroy();
    }

    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    public void onLocationChanged(Location arg0) {
        updateGPSInfo();
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        updateGPSInfo();
    }

    public void onProviderEnabled(String arg0) {
        updateGPSInfo();
    }

    public void onProviderDisabled(String arg0) {
        updateGPSInfo();
    }
}
