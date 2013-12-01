package be.fbousson.sensortag.services;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by fbousson on 01/12/13.
 */
public class LightUtil {


    private static final String TAG = LightUtil.class.getSimpleName();
    private boolean lightOn;

    private Camera mCamera;
    private PowerManager.WakeLock wakeLock;
    private static final String WAKE_LOCK_TAG = "BLESNAPPER_WAKE_LOCK";

    private Activity _activity;

    public LightUtil(Activity activity) {
        _activity = activity;
        getCamera();
    }

    private void startWakeLock() {
        if (wakeLock == null) {
            Log.d(TAG, "wakeLock is null, getting a new WakeLock");
            PowerManager pm = (PowerManager) _activity.getSystemService(Context.POWER_SERVICE);
            Log.d(TAG, "PowerManager acquired");
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
            Log.d(TAG, "WakeLock set");
        }
        wakeLock.acquire();
        Log.d(TAG, "WakeLock acquired");
    }

    private void stopWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            Log.d(TAG, "WakeLock released");
        }
    }

    private void getCamera() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            } catch (RuntimeException e) {
                Log.i(TAG, "Camera.open() failed: " + e.getMessage());
            }
        }
    }


    public void turnLightOn() {
        if (mCamera == null) {
            Toast.makeText(_activity, "Camera not found", Toast.LENGTH_LONG);
            // Use the screen as a flashlight (next best thing)
            return;
        }
        lightOn = true;
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        Log.i(TAG, "Flash mode: " + flashMode);
        Log.i(TAG, "Flash modes: " + flashModes);
        if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                startWakeLock();
            } else {
                Toast.makeText(_activity, "Flash mode (torch) not supported",
                        Toast.LENGTH_LONG);
                // Use the screen as a flashlight (next best thing)
                Log.e(TAG, "FLASH_MODE_TORCH not supported");
            }
        }
    }

    public void turnLightOff() {
        if (lightOn) {
            // set the background to dark
            lightOn = false;
            if (mCamera == null) {
                return;
            }
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters == null) {
                return;
            }
            List<String> flashModes = parameters.getSupportedFlashModes();
            String flashMode = parameters.getFlashMode();
            // Check if camera flash exists
            if (flashModes == null) {
                return;
            }
            Log.i(TAG, "Flash mode: " + flashMode);
            Log.i(TAG, "Flash modes: " + flashModes);
            if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                // Turn off the flash
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    stopWakeLock();
                } else {
                    Log.e(TAG, "FLASH_MODE_OFF not supported");
                }
            }
        }
    }
}
