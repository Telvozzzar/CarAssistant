package com.diegeilstegruppe.sasha.service;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by denys on 20/05/2017.
 */

public class NotificationService extends NotificationListenerService {

    public static final String TAG = "NotificationService";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();

        // TODO: add another packages... facebook, whatsapp, etc.
        if (!TextUtils.isEmpty(packageName) && packageName.equals("com.google.android.talk")) {

            Bundle extras = sbn.getNotification().extras;

            String text = extras.getCharSequence("android.text").toString();
            String title = extras.getCharSequence("android.title").toString();

            Log.d(TAG, title);
            Log.d(TAG, text);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Nothing to do
    }
}