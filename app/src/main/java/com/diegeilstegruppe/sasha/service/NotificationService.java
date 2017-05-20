package com.diegeilstegruppe.sasha.service;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.diegeilstegruppe.sasha.audio.Speech;
import com.diegeilstegruppe.sasha.audio.WavAudioRecorder;
import com.diegeilstegruppe.sasha.network.Communicator;
import com.diegeilstegruppe.sasha.network.ServerResponse;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by denys on 20/05/2017.
 */

public class NotificationService extends NotificationListenerService {

    public static final String TAG = "NotificationService";

    private Speech speech;
    private Communicator communicator;
    private String message;

    @Override
    public void onCreate() {
        super.onCreate();
        speech = new Speech(getApplicationContext());
        Log.d(TAG, "NotificationListenerService onCreate");
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "NotificationListenerService onRebird");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.speech.onDestroy();
        BusProvider.getInstance().unregister(this);
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
                this.message = text;
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

    @Subscribe
    public void NewMessageNotifiedEvent(NewMessageNotifiedEvent newMessageNotifiedEvent) {

        Log.d(TAG, "NewMessageNotifiedEvent");

        final String mFileName =  getCacheDir().getAbsolutePath() + "/audio.wav";
        final WavAudioRecorder wavAudioRecorder = WavAudioRecorder.getInstanse();
        wavAudioRecorder.setOutputFile(mFileName);
        wavAudioRecorder.prepare();
        wavAudioRecorder.start();

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                Log.d(TAG, "TimerTask: run");
                wavAudioRecorder.stop();
                wavAudioRecorder.reset();
                communicator = new Communicator();
                communicator.uploadFile(new File(mFileName));
            }
        }, 5000);
    }

    @Subscribe
    public void ResponseEvent(ServerResponse serverResponse) {
        Log.d(TAG, "ResponseEvent");
    }

    public void readMessage(final String message) {
        this.speech.readNewMessage(message);
    }
}