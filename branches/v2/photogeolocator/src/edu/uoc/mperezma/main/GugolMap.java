/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import edu.uoc.mperezma.camera.GPSFileWriter;
import edu.uoc.mperezma.location.LocationHelper;
import edu.uoc.mperezma.location.LocationHelperListener;
import java.io.File;
import java.util.List;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.RationalNumber;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

/**
 *
 * @author mperezma
 */
public class GugolMap extends MapActivity implements LocationHelperListener, OnItemClickListener, OnKeyListener, android.widget.CompoundButton.OnCheckedChangeListener {

    private MyItemizedOverlay itemizedoverlay;
    protected MapView mapView;
    protected CheckBox checkBox;
    protected TextView textView;
    protected ImageButton searchButton;
    private LocationHelper locationHelper;
    AutoCompleteTextView autoCompleteTextView;
    private double longitude;
    private double latitude;
    private boolean usarFileDialog = true;
    private AddressLookup grl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gugol_map);

        mapView = (MapView) findViewById(R.id.mapview);
        checkBox = (CheckBox) findViewById(R.id.syncLocation);
        textView = (TextView) findViewById(R.id.locationInfo);

        locationHelper = new LocationHelper(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));

        mapView.setBuiltInZoomControls(true);

        updateMapMode();

        List<Overlay> mapOverlays = mapView.getOverlays();
        itemizedoverlay = new MyItemizedOverlay(this);
        mapOverlays.add(itemizedoverlay);

        checkBox.setOnCheckedChangeListener(this);
        checkBox.setChecked(false);
        checkBox.setChecked(true);
        
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.enterLocation);
        autoCompleteTextView.setOnKeyListener(this);
        autoCompleteTextView.setOnItemClickListener(this);

        searchButton = (ImageButton) findViewById(R.id.button);
        searchButton.setOnClickListener(new OnClickListener()  {

            public void onClick(View v) {
                if (autoCompleteTextView.getVisibility() == AutoCompleteTextView.VISIBLE) {
                    hideAutoCompleteTextView();
                } else {
                    showAutoCompleteTextView();
                }
            }
        });

        hideAutoCompleteTextView();
    }

    @Override
    protected void onResume() {
        updateMapMode();
        super.onResume();
    }

    private void updateMapMode() {
        SharedPreferences settings = getSharedPreferences("map", 0);
        boolean satellite = settings.getBoolean("satellite", true);
        mapView.setSatellite(satellite);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    protected void onDestroy() {
        locationHelper.disable();
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        if (checkBox.isChecked()) {
            if (locationHelper.hasData()) {
                updateItem();
                itemizedoverlay.center();
            } else {
                Toast.makeText(this, R.string.gpsUnavailable, Toast.LENGTH_SHORT).show();
                checkBox.setChecked(false);
            }
        } else {
            locationHelper.disable();
        }
    }

    public boolean onKey(View view, int i, KeyEvent ke) {
        if (ke.getAction() == KeyEvent.ACTION_UP) {
            grl = new AddressLookup(this, autoCompleteTextView);
            grl.execute();
        }
        return false;
    }

    public void onItemClick(AdapterView av, View view, int i, long l) {
        final Object item = av.getItemAtPosition(i);
        if (item instanceof AddressItem) {
            checkBox.setChecked(false);
            AddressItem addressItem = (AddressItem) item;
            setLongitude(addressItem.address.getLongitude());
            setLatitude(addressItem.address.getLatitude());
            itemizedoverlay.setGeoPoint(longitude, latitude);
            mapView.getController().animateTo(new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6)));
            mapView.getZoomButtonsController().setVisible(false);
            mapView.invalidate();
            hideAutoCompleteTextView();
        }
    }
    
    private void showPictureLocation(final String fileName) {
        try {
            IImageMetadata metadata = Sanselan.getMetadata(new File(fileName));
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            TiffImageMetadata exif = jpegMetadata.getExif();
            RationalNumber[] rationalLongitude = (RationalNumber[]) exif.findField(TiffConstants.GPS_TAG_GPS_LONGITUDE).getValue();
            String longitudeRef = exif.findField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF).getStringValue();
            RationalNumber[] rationalLatitude = (RationalNumber[]) exif.findField(TiffConstants.GPS_TAG_GPS_LATITUDE).getValue();
            String latitudeRef = exif.findField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF).getStringValue();
            double readLatitude = rationalLatitude[0].doubleValue() + rationalLatitude[1].doubleValue() / 60d + rationalLatitude[2].doubleValue() / 3600d;
            double readLongitude = rationalLongitude[0].doubleValue() + rationalLongitude[1].doubleValue() / 60d + rationalLongitude[2].doubleValue() / 3600d;
            readLongitude *= longitudeRef.equalsIgnoreCase("W") ? -1 : 1;
            readLatitude *= latitudeRef.equalsIgnoreCase("S") ? -1 : 1;
            GeoPoint geoPoint = new GeoPoint((int) (readLatitude * 1E6), (int) (readLongitude * 1E6));
            itemizedoverlay.onTap(geoPoint, mapView);
            itemizedoverlay.center();
        } catch (Exception e) {
            Toast.makeText(this, R.string.noGpsInfo, Toast.LENGTH_SHORT).show();
        }
    }

    private void tagPicture(final String fileName) {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.confirmOverwrite).setMessage(R.string.willOverwrite).setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte longitudeRef = longitude >= 0 ? (byte) 'E' : (byte) 'W';
                byte latitudeRef = latitude >= 0 ? (byte) 'N' : (byte) 'S';
                double readLongitude = longitude < 0 ? longitude * (-1) : longitude;
                double readLatitude = latitude < 0 ? latitude * (-1) : latitude;

                SharedPreferences settings = getSharedPreferences("rational", 0);
                boolean rational = settings.getBoolean("enabled", true);
                String mapDatum = settings.getString("mapDatum", null);
                
                if (GPSFileWriter.update(new File(fileName), readLongitude, longitudeRef, readLatitude, latitudeRef, rational, mapDatum)) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                } else {
                    Toast.makeText(GugolMap.this, R.string.noDigitalCameraImage, Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton(R.string.cancel, null).show();
    }

    private void updateItem() {
        longitude = locationHelper.getLongitude();
        latitude = locationHelper.getLatitude();
        itemizedoverlay.setGeoPoint(longitude, latitude);
        mapView.invalidate();
    }

    public void onlocationChanged() {
        if (checkBox.isChecked() && locationHelper.hasData()) {
            updateItem();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {

        MenuItem mostrarUbicacion = menu.add(0, 0, 0, R.string.showImageLocation);
        mostrarUbicacion.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences settings = getSharedPreferences("fileDialog", 0);
                usarFileDialog = !settings.getBoolean("gallery", true);
                if (usarFileDialog) {
                    Intent intent = new Intent(GugolMap.this, edu.uoc.mperezma.FileDialog.class);
                    startActivityForResult(intent, 0);
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
                }
                return true;
            }
        });

        MenuItem establecerUbicacion = menu.add(0, 0, 0, R.string.setImageLocation);
        establecerUbicacion.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences settings = getSharedPreferences("fileDialog", 0);
                usarFileDialog = !settings.getBoolean("gallery", true);
                if (usarFileDialog) {
                    Intent intent = new Intent(GugolMap.this, edu.uoc.mperezma.FileDialog.class);
                    startActivityForResult(intent, 1);
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 3);
                }
                return true;
            }
        });

        MenuItem takePicture = menu.add(0, 0, 0, R.string.takePicture);
        takePicture.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(GugolMap.this, edu.uoc.mperezma.camera.ImageCapture.class);

                final boolean useGps = checkBox.isChecked();
                intent.putExtra("useGps", useGps);
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                startActivity(intent);
                return true;
            }
        });

        MenuItem config = menu.add(0, 0, 0, R.string.preferences);
        config.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(GugolMap.this, edu.uoc.mperezma.Config.class);
                startActivity(intent);
                return true;
            }
        });

        MenuItem acercaDe = menu.add(0, 0, 0, R.string.about);
        acercaDe.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(GugolMap.this, edu.uoc.mperezma.Version.class);
                startActivity(intent);
                return true;
            }
        });

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (result != null) {
            if (requestCode == 0 || requestCode == 1) {
                final String fileName = result.getExtras().getString("fileName");
                if (fileName != null) {
                    if (requestCode == 0) {
                        showPictureLocation(fileName);
                    } else {
                        tagPicture(fileName);
                    }
                }
            } else {
                Uri contentUri = result.getData();
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(contentUri, proj, null, null, null);
                if (cursor == null) {
                    Toast.makeText(GugolMap.this, R.string.npeException, Toast.LENGTH_SHORT).show();
                } else {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String fileName = cursor.getString(column_index);
                    if (fileName != null) {
                        if (requestCode == 2) {
                            showPictureLocation(fileName);
                        } else {
                            tagPicture(fileName);
                        }
                    }
                }
            }
        }
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    private void hideAutoCompleteTextView() {
        autoCompleteTextView.setVisibility(AutoCompleteTextView.INVISIBLE);
        searchButton.setImageResource(R.drawable.search);
    }

    private void showAutoCompleteTextView() {
        autoCompleteTextView.setText("");
        autoCompleteTextView.setVisibility(AutoCompleteTextView.VISIBLE);
        searchButton.setImageResource(R.drawable.closesearch);
    }
}
