/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.main;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author mperezma
 */
public class AddressLookup extends AsyncTask<Void, Void, String> {

    private final GugolMap gugolMap;
    private final AutoCompleteTextView textView;
    
    private List<Address> addresses = new ArrayList<Address>();   
    private static final String RESULT_OK = "ok";
    private static final String RESULT_FAIL = "fail";

    public AddressLookup(GugolMap gugolMap, AutoCompleteTextView textView) {
        this.gugolMap = gugolMap;
        this.textView = textView;
    }

    @Override
    protected String doInBackground(Void... params) {
        String location = textView.getText().toString();
        if (textView != null) {
            try {
                Geocoder gc = new Geocoder(gugolMap);
                addresses = gc.getFromLocationName(location, 5);
                return addresses.size() > 0 ? RESULT_OK : RESULT_FAIL;
            } catch (IOException ex) {
                return RESULT_FAIL;
            }
        }
        return RESULT_OK;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null ? RESULT_OK == null : result.equals(RESULT_OK)) {
            List<AddressItem> items = new ArrayList<AddressItem>();
            for (Address address : addresses) {
                items.add(new AddressItem(address));
            }
            ArrayAdapter adapter = new ArrayAdapter(gugolMap,
                    android.R.layout.simple_dropdown_item_1line, items);
            textView.setAdapter(adapter);
            textView.showDropDown();
        }
    }
}
