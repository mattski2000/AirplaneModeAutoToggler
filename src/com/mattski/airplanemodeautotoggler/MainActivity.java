/*
 * MainActivity.java
 */
package com.mattski.airplanemodeautotoggler;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.ToggleButton;

//TODO:Remember settings when you reload activity while service is running
//TODO:Could use a restart Button
//TODO:Use 1 Handler instead of 2
//TODO:Needs a way to deal with a notification closing the service while the activity is
//     still running.  Perhaps a progress dialog for 3-5 seconds.  Have it trigger on
//     ToggleButton check state.


/**
 * Main Activity for Airplane Mode Toggler that toggles the on/off state of
 * Airplane mode to preserve battery life. The duration of each state is set by
 * the user.<br>
 * 
 * @author Mattski
 */
public class MainActivity extends Activity {
    private static final int MIN_VALUE = 1;
    //private final static int KILL_NOTIFICATION_ID = 0;
    private static final String AIRPLANE_MODE = "Airplane Mode ";
    // widgets
    private ToggleButton mToggleButton;
    private SeekBar mOnDurationSeekBar;
    private SeekBar mOffDurationSeekBar;
    private TextView mOnValueTextView;
    private TextView mOffValueTextView;
    private TextView mStatusTextView;
    private TextView mCurrentTimeTextView;
    // listeners
    private OnSeekBarChangeListener mSeekBarListener;
    private OnCheckedChangeListener mToggleButtonListener;
    // Timer related
    private Timer mTimer;
    private Handler mHandler;
    private Runnable mRunnable;

    private Intent mServiceIntent;
    private ContextThemeWrapper darkTheme;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInners();
        // set Widgets
        mToggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        mOnDurationSeekBar = (SeekBar) findViewById(R.id.onDurationSeekBar);
        mOffDurationSeekBar = (SeekBar) findViewById(R.id.offDurationSeekBar);
        mOnValueTextView = (TextView) findViewById(R.id.onValueTextView);
        mOffValueTextView = (TextView) findViewById(R.id.offValueTextView);
        mCurrentTimeTextView = (TextView) findViewById(R.id.currentTimeTextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
        // set Listeners
        mToggleButton.setOnCheckedChangeListener(mToggleButtonListener);
        mOnDurationSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
        mOffDurationSeekBar.setOnSeekBarChangeListener(mSeekBarListener);

        mTimer = new Timer();
        mHandler = new Handler();

        mServiceIntent = new Intent(this, ModeTogglerService.class);
        darkTheme = new ContextThemeWrapper(this, R.style.DarkDialogTheme);
    }

    @Override
    protected void onStart() {
        if (ModeTogglerService.getIsServiceRunning()) {
            // set values on restart except for timer progress
            mToggleButton.setChecked(true);
            int onStart = (int) (ModeTogglerService.getOnDurationMillis() / (60 * 1000))
                    - MIN_VALUE;
            int offStart = (int) (ModeTogglerService.getOffDurationMillis() / (60 * 1000))
                    - MIN_VALUE;
            mOnDurationSeekBar.setProgress(onStart);
            mOffDurationSeekBar.setProgress(offStart);
        }
        else {
            // set Starting values
            int onStart = mOnDurationSeekBar.getProgress() + MIN_VALUE;
            int offStart = mOffDurationSeekBar.getProgress() + MIN_VALUE;
            mOnValueTextView.setText(String.valueOf(onStart));
            mOffValueTextView.setText(String.valueOf(offStart));
            ModeTogglerService.setOnDurationMillis(onStart);
            ModeTogglerService.setOffDurationMillis(offStart);
        }
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                // shows an AlertDialog for the about menu option.
                AlertDialog.Builder builder = new AlertDialog.Builder(darkTheme);
                builder.setTitle(getResources().getString(R.string.menu_about))
                        .setInverseBackgroundForced(true)
                        .setMessage(getResources().getString(R.string.menu_about_message))
                        .setInverseBackgroundForced(true)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.cancel();
                            }
                        }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Initializes inner classes. */
    private void initInners() {
        mSeekBarListener = new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getId() == R.id.onDurationSeekBar) {
                    ModeTogglerService.setOnDurationMillis(progress + MIN_VALUE);
                    mOnValueTextView.setText(String.valueOf(progress + MIN_VALUE));
                }
                else if (seekBar.getId() == R.id.offDurationSeekBar) {
                    ModeTogglerService.setOffDurationMillis(progress + MIN_VALUE);
                    mOffValueTextView.setText(String.valueOf(progress + MIN_VALUE));
                }
            }
        };

        mToggleButtonListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mToggleButton.isChecked()) {
                    startService(mServiceIntent);
                    mHandler.postDelayed(mRunnable, 500);
                }
                else {
                    stopService(mServiceIntent);
                    mHandler.removeCallbacks(mRunnable);
                    mTimer.stopTimer();
                }
            }
        };

        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (ModeTogglerService.getIsAirplaneModeOn()) {
                    mStatusTextView.setText(AIRPLANE_MODE + "on");
                    if (!mTimer.getIsTimerRunning()) {
                        mTimer.startTimer(mOnDurationSeekBar.getProgress() + MIN_VALUE);
                    }
                }
                else {
                    mStatusTextView.setText(AIRPLANE_MODE + "off");
                    if (!mTimer.getIsTimerRunning()) {
                        mTimer.startTimer(mOffDurationSeekBar.getProgress() + MIN_VALUE);
                    }
                }
                if(ModeTogglerService.getIsServiceRunning()) {
                    mCurrentTimeTextView.setText(mTimer.getRemainingTime());
                    
                }
                else {
                    mToggleButton.setChecked(false);
                }
                mHandler.postDelayed(this, 500);
            }
        };
    }
}
