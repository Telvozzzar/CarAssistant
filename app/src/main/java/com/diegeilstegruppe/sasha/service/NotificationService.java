package com.diegeilstegruppe.sasha.service;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.diegeilstegruppe.sasha.audio.Speech;

/**
 * Created by denys on 20/05/2017.
 */

public class NotificationService extends NotificationListenerService {

    public static final String TAG = "NotificationService";

    private Speech speech;

    @Override
    public void onCreate() {
        super.onCreate();
        speech = new Speech(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.speech.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();

        // TODO: add another packages... facebook, whatsapp, etc.
        if (!TextUtils.isEmpty(packageName) && packageName.equals("com.google.android.talk")) {

            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;

            // Log.d(TAG, "category: " + notification.category);
            if (notification.category != null
                    && notification.category.equals(Notification.CATEGORY_MESSAGE)) {
                String text = extras.getCharSequence("android.text").toString();
                String title = extras.getCharSequence("android.title").toString();
                // Log.d(TAG, title);
                // Log.d(TAG, text);
                this.speech.sayNewMessage("Hangout", title);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Nothing to do
    }
}