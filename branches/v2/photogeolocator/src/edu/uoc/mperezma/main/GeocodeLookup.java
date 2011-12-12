/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.main;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author mperezma
 */
public class GeocodeLookup extends AsyncTask<Void, Void, String> {

    private final GugolMap gugolMap;
    private final TextView textView;
    private final GeoPoint geoPoint;

    public GeocodeLookup(GugolMap gugolMap, GeoPoint geoPoint, TextView textView) {
        this.gugolMap = gugolMap;
        this.textView = textView;
        this.geoPoint = geoPoint;
    }

    @Override
    protected void onPreExecute() {
        CharSequence text = textView.getText();
        String suffix = " - " + gugolMap.getText(R.string.retrievingAddress).toString();
        if (!text.toString().endsWith(suffix)) {
            textView.setText(textView.getText() + suffix );
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        String result = "";
        String location = gugolMap.getText(R.string.longitude) + ": " + geoPoint.getLongitudeE6() / 1E6 + "\n" +
                gugolMap.getText(R.string.latitude) + ": " + geoPoint.getLatitudeE6() / 1E6;
        if (textView != null) {
            Geocoder gc = new Geocoder(gugolMap);
            List<Address> addresses = null;
            try {
                addresses = gc.getFromLocation(geoPoint.getLatitudeE6() / 1E6, geoPoint.getLongitudeE6() / 1E6, 1);
            } catch (IOException ex) {
            }
            if (addresses != null && addresses.size() > 0) {
                try {
                    Address address = addresses.get(0);
                    result = new AddressItem(address).toString();
                } catch (Exception e) {
                    result = location;
                }
            } else {
                result = location;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        textView.setText(result);
    }
}
