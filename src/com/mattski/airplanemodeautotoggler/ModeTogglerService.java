/*
 * ModeTogglerService.java 
 */
package com.mattski.airplanemodeautotoggler;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Background service that toggles Airplane Mode.
 * 
 * @author Mattski
 */
public class ModeTogglerService extends Service implements Runnable {
    private Handler mHandler;
    private long mDelayMillis = 500;
    private Intent mIntent;

    private static long sOnDurationMillis = 0;
    private static long sOffDurationMillis = 0;
    private static boolean sIsAirplaneModeOn = false;
    private static boolean sIsServiceRunning = false;

    /** converts minutes to millis and sets sOnDurationMillis. */
    public static void setOnDurationMillis(long minutes) {
        sOnDurationMillis = Timer.convertMinutesToMillis(minutes);
    }

    /** converts minutes to millis and sets sOffDurationMillis. */
    public static void setOffDurationMillis(long minutes) {
        sOffDurationMillis = Timer.convertMinutesToMillis(minutes);
    }

    /** Accessor */
    public static boolean getIsAirplaneModeOn() {
        return sIsAirplaneModeOn;
    }

    /** Accessor */
    public static boolean getIsServiceRunning() {
        return sIsServiceRunning;
    }

    /** Accessor */
    public static long getOnDurationMillis() {
        return sOnDurationMillis;
    }

    /** Accessor */
    public static long getOffDurationMillis() {
        return sOffDurationMillis;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIntent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!sIsServiceRunning) {
            sIsServiceRunning = true;
            sIsAirplaneModeOn = false;
            mHandler.post(this);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(this);
        setAirplaneModeState(false);
        sIsServiceRunning = false;
        Toast.makeText(this, "Background Service killed.", Toast.LENGTH_SHORT).show();
    }

    // Code to execute in background thread.
    @Override
    public void run() {

        if (sIsAirplaneModeOn) {
            mDelayMillis = sOffDurationMillis;
        }
        else {
            mDelayMillis = sOnDurationMillis;
        }
        setAirplaneModeState(sIsAirplaneModeOn = !sIsAirplaneModeOn);
        mHandler.postDelayed(this, mDelayMillis);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Sets Airplane mode to on or off based on parameter value.
     * 
     * @param isOn
     *            On/off state of Airplane mode.
     */
    private void setAirplaneModeState(boolean isOn) {
        Settings.System.putInt(getContentResolver(),
                               Settings.System.AIRPLANE_MODE_ON,
                               isOn ? 1 : 0);
        mIntent.putExtra("state", isOn);
        sendBroadcast(mIntent);
    }

}
