/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioButton;
import edu.uoc.mperezma.main.R;

/**
 *
 * @author mperezma
 */
public class Config extends Activity {
    private RadioButton radio_red;
    private RadioButton radio_blue;
    private RadioButton radio_siempre;
    private RadioButton radio_nunca;
    private RadioButton radio_auto;
    private RadioButton radio_map;
    private RadioButton radio_satellite;
    private RadioButton radio_rationalEnabled;
    private RadioButton radio_rationalDisabled;
    private RadioButton radio_datumDisabled;
    private RadioButton radio_datumWGS84;
    private RadioButton radio_datumTOKYO;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.config);

        radio_red = (RadioButton) findViewById(R.id.radio_red);
        radio_blue = (RadioButton) findViewById(R.id.radio_blue);
        radio_siempre = (RadioButton) findViewById(R.id.radio_siempre);
        radio_nunca = (RadioButton) findViewById(R.id.radio_nunca);
        radio_auto = (RadioButton) findViewById(R.id.radio_auto);
        radio_map = (RadioButton) findViewById(R.id.radio_map);
        radio_satellite = (RadioButton) findViewById(R.id.radio_satellite);
        radio_rationalEnabled = (RadioButton) findViewById(R.id.radio_rationalEnabled);
        radio_rationalDisabled = (RadioButton) findViewById(R.id.radio_rationalDisabled);
        radio_datumDisabled = (RadioButton) findViewById(R.id.radio_datumDisabled);
        radio_datumWGS84 = (RadioButton) findViewById(R.id.radio_datumWGS84);
        radio_datumTOKYO = (RadioButton) findViewById(R.id.radio_datumTOKYO);
        
        radio_red.setOnClickListener(radioListener);
        radio_blue.setOnClickListener(radioListener);
        radio_siempre.setOnClickListener(radioListener);
        radio_nunca.setOnClickListener(radioListener);
        radio_auto.setOnClickListener(radioListener);
        radio_map.setOnClickListener(radioListener);
        radio_satellite.setOnClickListener(radioListener);
        radio_rationalEnabled.setOnClickListener(radioListener);
        radio_rationalDisabled.setOnClickListener(radioListener);
        radio_datumDisabled.setOnClickListener(radioListener);
        radio_datumWGS84.setOnClickListener(radioListener);
        radio_datumTOKYO.setOnClickListener(radioListener);

        SharedPreferences settings = getSharedPreferences("fileDialog", 0);
        boolean gallery = settings.getBoolean("gallery", true);
        if (gallery) {
            radio_red.setChecked(true);
        } else {
            radio_blue.setChecked(true);
        }
        settings = getSharedPreferences("camera", 0);
        int flash = settings.getInt("flash", 0);
        switch (flash) {
            case 0: radio_nunca.setChecked(true);
            break;
            case 1: radio_siempre.setChecked(true);
            break;
            case 2: radio_auto.setChecked(true);
        }
        settings = getSharedPreferences("map", 0);
        boolean map = !settings.getBoolean("satellite", true);
        if (map) {
            radio_map.setChecked(true);
        } else {
            radio_satellite.setChecked(true);
        }
        settings = getSharedPreferences("rational", 0);
        boolean rational = settings.getBoolean("enabled", true);
        if (rational) {
            radio_rationalEnabled.setChecked(true);
        } else {
            radio_rationalDisabled.setChecked(true);
        }
        
        String mapDatum = settings.getString("mapDatum", null);
        if (mapDatum == null) {
            radio_datumDisabled.setChecked(true);
        } else if (mapDatum.equals("WGS-84")) {
            radio_datumWGS84.setChecked(true);
        } else {
            radio_datumTOKYO.setChecked(true);
        }
    }

    private OnClickListener radioListener = new OnClickListener() {
        public void onClick(View arg0) {
            SharedPreferences settings = getSharedPreferences("fileDialog", 0);
            SharedPreferences.Editor editor = settings.edit();
            if (radio_blue.isChecked()) {
                editor.putBoolean("gallery", false);
            } else {
                editor.putBoolean("gallery", true);
            }
            editor.commit();
            settings = getSharedPreferences("camera", 0);
            editor = settings.edit();
            if (radio_nunca.isChecked()) {
                editor.putInt("flash", 0);
            } else if (radio_siempre.isChecked()) {
                editor.putInt("flash", 1);
            } else {
                editor.putInt("flash", 2);
            }
            editor.commit();
            settings = getSharedPreferences("map", 0);
            editor = settings.edit();
            if (radio_map.isChecked()) {
                editor.putBoolean("satellite", false);
            } else {
                editor.putBoolean("satellite", true);
            }
            editor.commit();
            settings = getSharedPreferences("rational", 0);
            editor = settings.edit();
            if (radio_rationalEnabled.isChecked()) {
                editor.putBoolean("enabled", true);
            } else {
                editor.putBoolean("enabled", false);
            }
            if (radio_datumDisabled.isChecked()) {
                editor.putString("mapDatum", null);
            }
            if (radio_datumWGS84.isChecked()) {
                editor.putString("mapDatum", "WGS-84");
            }
            if (radio_datumTOKYO.isChecked()) {
                editor.putString("mapDatum", "TOKYO");
            }
            editor.commit();
        }
    };
}
