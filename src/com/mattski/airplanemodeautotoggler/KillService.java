/*
 * KillService.java
 */
package com.mattski.airplanemodeautotoggler;

import android.app.IntentService;
import android.content.Intent;

/**
 * Sevice's only purpose is to stop ModeToggleService.
 * 
 * @author MattSki
 */
public class KillService extends IntentService {

    /** Constructor */
    public KillService() {
        super("KillService");
    }

    @Override
    protected void onHandleIntent(Intent arg0) {
        stopService(new Intent(this, ModeTogglerService.class));
    }
}
