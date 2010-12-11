/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import edu.uoc.mperezma.main.R;

/**
 *
 * @author mperezma
 */
public class MainActivity extends ListActivity implements OnItemClickListener {

    static final String[] NUCLEOS = new String[]{
        "Locator", "Camera", "Maps", "FileDialog", "Version"};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, NUCLEOS));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(this);

    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 == 0) {
            Intent intent = new Intent(this, edu.uoc.mperezma.location.LocationActivity.class);
            startActivity(intent);
        }
        if (arg2 == 1) {
            Intent intent = new Intent(this, edu.uoc.mperezma.camera.ImageCapture.class);
            startActivity(intent);
        }
        if (arg2 == 2) {
            Intent intent = new Intent(this, edu.uoc.mperezma.main.GugolMap.class);
            startActivity(intent);
        }
        if (arg2 == 3) {
            Intent intent = new Intent(this, edu.uoc.mperezma.FileDialog.class);
            startActivityForResult(intent, 0);
        }
        if (arg2 == 4) {
            Intent intent = new Intent(this, edu.uoc.mperezma.Version.class);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (result != null) {
            final String fileName = result.getExtras().getString("fileName");
            if (fileName != null) {
                Toast.makeText(this, "" + fileName, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
