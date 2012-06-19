/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import edu.uoc.mperezma.main.R;

/**
 *
 * @author mperezma
 */
public class Version extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.version);
        EditText mEditor = (EditText) findViewById(R.id.editor);
        mEditor.setClickable(false);
        mEditor.setFocusable(false);
        mEditor.setText("" +
                "Photo Geolocator" +
                "\n" +
                getText(R.string.version) + " 1.7.0 (Build 201206192331)" +
                "\n" +
                "Free GNU license" +
                "\n" +
                "http://code.google.com/p/photogeolocator/" +
                "\n" +
                "Mail: mario@oiram.com" +
                "\n" +
                "\n" +
                getText(R.string.aboutText) +
                "\n" +
                "\n" +
                getText(R.string.betaTestTeam) +
                "\n" +
                "Montxo - HTC Dream" +
                "\n" +
                "Marcello Anselmi Tamburini - LG Optimus One" +
                "\n" +
                "Pablo Carmona - HTC Magic" +
                "\n" +
                "Fritz Endres - HTC Desire" +
                "\n" +
                "Sebastián Ercoli - Samsung Galaxy S" +
                "\n" +
                "Maria del Mar Fontana - Motorola Dext" +
                "\n" +
                "Javier Gallego - HTC Hero" +
                "\n" +
                "Emiliano García - HTC Hero / Tatoo" +
                "\n" +
                "Manel García - HTC Tatoo" +
                "\n" +
                "Ken Gauger - VZW Samsung S Fascinate" +
                "\n" +
                "Javier Gavilán - HTC Hero" +
                "\n" +
                "David Godino - HTC Legend" +
                "\n" +
                "Enric Heredia - HTC Tatoo" +
                "\n" +
                "Masakazu Kawahara - IS05" +
                "\n" +                
                "Jose Antonio López - HTC Dream" +
                "\n" +
                "Ángel Martínez - HTC Hero" +
                "\n" +
                "Juan Manuel Patón - HTC Hero" +
                "\n" +
                "Marco Antonio Pérez - HTC Magic / Nexus One" +
                "\n" +
                "Moisés Regalon - HTC Magic" +
                "\n" +
                "\n" +
                getText(R.string.thisAppUsesLibrary) + " Sanselan Android (http://code.google.com/p/sanselanandroid)");
    }

}
