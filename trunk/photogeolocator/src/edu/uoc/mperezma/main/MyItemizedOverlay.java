/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 *
 * @author mperezma
 */
public class MyItemizedOverlay extends Overlay {

    private static final int MARKER_X_OFFSET = 7;
    private static final int MARKER_Y_OFFSET = 72;

    private TextView textView = null;
    private GeoPoint geoPoint = null;
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    private final MapView mapView;
    private final CheckBox checkBox;
    private final GugolMap gugolMap;
    private GeocodeLookup gl;

    public MyItemizedOverlay(GugolMap gugolMap) {
        geoPoint = new GeoPoint(41388580, 2112457);
        paint2.setARGB(255, 255, 255, 255);
        this.gugolMap = gugolMap;
        this.textView = gugolMap.textView;
        this.mapView = gugolMap.mapView;
        this.checkBox = gugolMap.checkBox;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean b) {
        super.draw(canvas, mapView, b);

        Bitmap bmp = BitmapFactory.decodeResource(
                mapView.getResources(), R.drawable.androidmarker);

        Point screenPts = new Point();
        mapView.getProjection().toPixels(geoPoint, screenPts);

        canvas.drawBitmap(bmp, screenPts.x - MARKER_X_OFFSET, screenPts.y - MARKER_Y_OFFSET, null);

    }

    @Override
    public boolean onTap(GeoPoint geoPoint, MapView mapView) {
        if (!super.onTap(geoPoint, mapView)) {
            setGeoPoint(geoPoint);
            mapView.getZoomButtonsController().setVisible(false);
            checkBox.setChecked(false);
            gugolMap.setLongitude(geoPoint.getLongitudeE6() / 1E6);
            gugolMap.setLatitude(geoPoint.getLatitudeE6() / 1E6);
            mapView.invalidate();
            return true;
        }
        return false;
    }

    public void center() {
        mapView.getController().setCenter(geoPoint);
    }

    public void setGeoPoint(double longitude, double latitude) {
        setGeoPoint(new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6)));
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
        try {
            gl.cancel(true);
        } catch (Exception e) {
        }

        gl = new GeocodeLookup(gugolMap, geoPoint, textView);
        gl.execute();
    }

}
