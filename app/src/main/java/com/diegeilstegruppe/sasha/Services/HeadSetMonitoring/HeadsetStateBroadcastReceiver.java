package com.diegeilstegruppe.sasha.Services.HeadSetMonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.diegeilstegruppe.sasha.Services.Notifications.BusProvider;

/**
 * Created by Telvozzzar on 29.08.2017.
 */

public class HeadsetStateBroadcastReceiver extends BroadcastReceiver {
    public static final String[] HEADPHONE_ACTIONS = {
            Intent.ACTION_HEADSET_PLUG,
            "android.bluetooth.headset.action.STATE_CHANGED",
            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED"
    };

    @Override
    public void onReceive(final Context context, final Intent intent) {

        BusProvider.getInstance().register(this);

        boolean broadcast = false;

        // Wired headset monitoring
        if (intent.getAction().equals(HEADPHONE_ACTIONS[0])) {
            final int state = intent.getIntExtra("state", 0);
            broadcast = true;
        }

        // Bluetooth monitoring
        // Works up to and including Honeycomb
        if (intent.getAction().equals(HEADPHONE_ACTIONS[1])) {
            int state = intent.getIntExtra("android.bluetooth.headset.extra.STATE", 0);
            broadcast = true;
        }

        // Works for Ice Cream Sandwich
        if (intent.getAction().equals(HEADPHONE_ACTIONS[2])) {
            int state = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
            broadcast = true;
        }

        // Used to inform interested activities that the headset state has changed
        if (broadcast) {
            BusProvider.getInstance().post(new Intent("headsetStateChange"));
        }

    }

}
