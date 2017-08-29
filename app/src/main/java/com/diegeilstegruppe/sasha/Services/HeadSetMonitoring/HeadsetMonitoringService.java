package com.diegeilstegruppe.sasha.Services.HeadSetMonitoring;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Created by Telvozzzar on 29.08.2017.
 */

public class HeadsetMonitoringService extends Service {
    HeadsetStateBroadcastReceiver headsetStateReceiver;

    @Override
    public void onCreate() {

        headsetStateReceiver = new HeadsetStateBroadcastReceiver();
        final IntentFilter filter = new IntentFilter();
        for (String action: HeadsetStateBroadcastReceiver.HEADPHONE_ACTIONS) {
            filter.addAction(action);
        }

        registerReceiver(headsetStateReceiver, filter);

    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(headsetStateReceiver);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

}
