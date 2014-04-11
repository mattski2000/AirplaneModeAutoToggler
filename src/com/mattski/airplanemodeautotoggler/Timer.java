/*
 * Timer.java
 */
package com.mattski.airplanemodeautotoggler;

import java.util.Locale;

/**
 * A class that tracks a value as it counts down from a starting value to 0.<br>
 * 
 * @author MattSki
 */
public class Timer {
    private static final String TIME_FORMAT = "%d:%02d";
    private long mCountdownStartPoint = 0;
    private long mStartTime = 0;
    private long mRemainingTime = 0;
    private boolean mTimerIsRunning = false;

    /** Converts minutes to milliseconds. Returns the new value. */
    public static long convertMinutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }

    /**
     * Starts the timer.
     * 
     * @param startTimeInMinutes
     */
    public void startTimer(long startTimeInMinutes) {
        setStartTime(startTimeInMinutes);
        mRemainingTime = getCountdown(true);
        mTimerIsRunning = true;
    }

    /** Stops timer and resets timer values. */
    public void stopTimer() {
        mRemainingTime = 0;
        mTimerIsRunning = false;
    }

    /**
     * Updates the remaining time if greater than 0. Otherwise resets remaining
     * time to the alternate starting time.
     */
    private void updateTimer() {
        if (mTimerIsRunning) {
            if (mRemainingTime == mStartTime) {
                mRemainingTime -= 1000; // forces remaining time to start on :59
            }
            else if (mRemainingTime > 0) {
                mRemainingTime = getCountdown(false);
            }
            else {
                stopTimer();
            }
        }
    }

    /** Converts and sets starting time from minutes. */
    public void setStartTime(long startTimeInMinutes) {
        mStartTime = convertMinutesToMillis(startTimeInMinutes);
    }

    /** Returns a string of the remaining amount of time of the timer. */
    public String getRemainingTime() {
        updateTimer();
        int seconds = (int) (mRemainingTime / 1000);
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format(Locale.getDefault(), TIME_FORMAT, minutes, seconds);
    }

    /** Accessor */
    public boolean getIsTimerRunning() {
        return mTimerIsRunning;
    }

    /**
     * Uses the system time to countdown from the starting time. If reset is
     * true, resets the counter to the starting time.
     * 
     * @param reset
     *            True to reset countdown to starting time.
     * @return The remaining time of the countdown.
     */
    private long getCountdown(boolean reset) {
        if (reset) {
            mCountdownStartPoint = System.currentTimeMillis();
            return mStartTime;
        }
        return mStartTime - (System.currentTimeMillis() - mCountdownStartPoint);
    }

}
