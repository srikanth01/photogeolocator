/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.camera;

import java.io.OutputStream;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author mperezma
 */
public class ImageCaptureCallback implements PictureCallback {

    private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final ImageCapture imageCapture;
    private final String APP_FOLDER = "geolocator";
    private boolean rational = true;
    private final String mapDatum;

    public ImageCaptureCallback(ImageCapture imageCapture, boolean rational, String mapDatum) {
        this.rational = rational;
        this.mapDatum = mapDatum;
        this.imageCapture = imageCapture;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        String fileName = "";
        
        try {
            String filename = timeStampFormat.format(new Date());

            File root = Environment.getExternalStorageDirectory();
            new File(root, APP_FOLDER).mkdir();

            File gpxfile = new File(new File(root, APP_FOLDER), filename + ".jpg");
            fileName = gpxfile.getAbsolutePath();
            OutputStream fileOutputStream = new FileOutputStream(gpxfile);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();

            double longitude = imageCapture.getLongitude();
            double latitude = imageCapture.getLatitude();

            byte longitudeRef = longitude >= 0 ? (byte) 'E' : (byte) 'W';
            byte latitudeRef = latitude >= 0 ? (byte) 'N' : (byte) 'S';
            longitude = longitude < 0 ? longitude * (-1) : longitude;
            latitude = latitude < 0 ? latitude * (-1) : latitude;

            GPSFileWriter.update(gpxfile, longitude, longitudeRef, latitude, latitudeRef, rational, mapDatum);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        MediaScannerConnection.scanFile(imageCapture, new String[]{fileName}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
            }
        });
        
        imageCapture.startPreview();
        imageCapture.takingPicture = false;
    }

}
