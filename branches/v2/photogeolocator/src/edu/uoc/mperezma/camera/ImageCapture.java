/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.camera;

import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import edu.uoc.mperezma.location.LocationHelper;
import edu.uoc.mperezma.location.LocationHelperListener;
import edu.uoc.mperezma.main.R;
import java.util.List;

/**
 *
 * @author mperezma
 */
public class ImageCapture extends Activity implements SurfaceHolder.Callback, Camera.AutoFocusCallback, LocationHelperListener {

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean useGps = true;
    private double longitude;
    private double latitude;
    private LocationHelper locationHelper;
    private MediaPlayer autofocusPlayer;
    private MediaPlayer shotPlayer;
    protected boolean takingPicture = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        if (getIntent().getExtras().containsKey("useGps")) {
            useGps = getIntent().getExtras().getBoolean("useGps");
            Object o = getIntent().getExtras().get("useGps");
        }
        if (useGps) {
            locationHelper = new LocationHelper(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));
            onlocationChanged();
        } else {
            longitude = getIntent().getExtras().getDouble("longitude");
            latitude = getIntent().getExtras().getDouble("latitude");
        }
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.image_capture);
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void onlocationChanged() {
        if (useGps && locationHelper.hasData()) {
            longitude = locationHelper.getLongitude();
            latitude = locationHelper.getLatitude();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    Camera.PictureCallback mPictureCallbackRaw = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            Toast.makeText(ImageCapture.this, R.string.pictureTaken, Toast.LENGTH_SHORT).show();
            shotBeep();
        }
    };

    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MENU) && !takingPicture) {
            takingPicture = true;
            Toast.makeText(ImageCapture.this, R.string.takingPicture, Toast.LENGTH_SHORT).show();
            autofocusBeep();
            try {
                final Parameters parameters = camera.getParameters();
                SharedPreferences settings = getSharedPreferences("camera", 0);
                int flash = settings.getInt("flash", 0);
                int resolution = settings.getInt("resolution", 1);
                switch (flash) {
                    case 0:
                        parameters.set("flash-mode", "off");
                        break;
                    case 1:
                        parameters.set("flash-mode", "on");
                        break;
                    case 2:
                        parameters.set("flash-mode", "auto");
                }
                
                List<Size> ls = parameters.getSupportedPictureSizes();
                
                int resolutionIndex = 0;
                int bestResolution = -1;
                
                if (resolution == 1) {
                    for (int i = 0; i < ls.size(); i++) {
                        if (bestResolution == -1 || ls.get(i).height > bestResolution) {
                            resolutionIndex = i;
                            bestResolution = ls.get(i).height;
                        }
                    }
                } else {
                    for (int i = 0; i < ls.size(); i++) {
                        if (bestResolution == -1 || ls.get(i).height < bestResolution) {
                            resolutionIndex = i;
                            bestResolution = ls.get(i).height;
                        }
                    }
                }
                
                parameters.setPictureSize(ls.get(resolutionIndex).width, ls.get(resolutionIndex).height);
                
                camera.setParameters(parameters);
            } catch (Exception e) {
            }
            camera.autoFocus(this);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            try {
                final Parameters parameters = camera.getParameters();
                int zoom = parameters.getInt("taking-picture-zoom");
                zoom += 5;
                zoom = zoom > 40 ? 40 : zoom;
                parameters.set("taking-picture-zoom", zoom);
                camera.setParameters(parameters);
            } catch (Exception e) {
            }
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            try {
                final Parameters parameters = camera.getParameters();
                int zoom = parameters.getInt("taking-picture-zoom");
                zoom -= 5;
                zoom = zoom < 0 ? 0 : zoom;
                parameters.set("taking-picture-zoom", zoom);
                camera.setParameters(parameters);
            } catch (Exception e) {
            }
        }

        return false;
    }

    @Override
    protected void onResume() {
        Log.e(getClass().getSimpleName(), "onResume");
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        Log.e(getClass().getSimpleName(), "onStop");
        super.onStop();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void onAutoFocus(boolean arg0, Camera arg1) {
        try {
            SharedPreferences settings = getSharedPreferences("rational", 0);
            boolean rational = settings.getBoolean("enabled", false);
            String mapDatum = settings.getString("mapDatum", null);

            camera.takePicture(mShutterCallback, mPictureCallbackRaw, new ImageCaptureCallback(this, rational, mapDatum));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(getClass().getSimpleName(), ex.getMessage(), ex);
        }
    }

    void startPreview() {
        camera.startPreview();
    }


    private void autofocusBeep() {
        try {
            autofocusPlayer = MediaPlayer.create(this, R.raw.beep26);
            autofocusPlayer.setLooping(false);
            autofocusPlayer.start();
            autofocusPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer arg0) {
                    releaseAutofocusPlayer();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseAutofocusPlayer() {
        if (autofocusPlayer != null) {
            autofocusPlayer.release();
            autofocusPlayer = null;
        }
    }

    private void releaseShotPlayer() {
        if (shotPlayer != null) {
            shotPlayer.release();
            shotPlayer = null;
        }
    }

    private void shotBeep() {
        try {
            shotPlayer = MediaPlayer.create(this, R.raw.beep29);
            shotPlayer.setLooping(false);
            shotPlayer.start();
            shotPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer arg0) {
                    releaseShotPlayer();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.disable();
        }
        releaseAutofocusPlayer();
        releaseShotPlayer();
    }
}
