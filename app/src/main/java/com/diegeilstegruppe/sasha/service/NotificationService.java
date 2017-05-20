package com.diegeilstegruppe.sasha.service;

import android.app.Notification;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.diegeilstegruppe.sasha.audio.Speech;
import com.diegeilstegruppe.sasha.audio.SpeechRecorder;
import com.diegeilstegruppe.sasha.network.Communicator;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by denys on 20/05/2017.
 */

public class NotificationService extends NotificationListenerService {

    public static final String TAG = "NotificationService";

    private Speech speech;
    private Communicator communicator;

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
                String title = extras.getCharSequence("android.title").toString();
                // Log.d(TAG, title);
                // Log.d(TAG, text);
                this.speech.sayNewMessage("Hangout", title + " ");
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Nothing to do
    }

    @Subscribe
    public void NewMessageNotifiedEvent(NewMessageNotifiedEvent newMessageNotifiedEvent) {
        SpeechRecorder recorder = new SpeechRecorder(this);
        recorder.startRecording();
        Log.d(TAG, "Start thread");
        Thread thread = new Thread(new BackgroundCounter());
        thread.start();
        try {
            thread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "join thread");
        recorder.stopRecording();
        String fileName = SpeechRecorder.getFileName();
        communicator = new Communicator();
        communicator.uploadFile(new File(fileName));
        MediaPlayer mediaPlayer = null;
        try{

             mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
        mediaPlayer.start();
        Log.d(TAG, "" + mediaPlayer.isPlaying());
        Log.d(TAG, "NewFile " + fileName);
        Log.d(TAG, "NewMessageNotifiedEvent: " + newMessageNotifiedEvent.getMessage());
    }

    protected class BackgroundCounter implements Runnable   {
        public void run(){
            Thread thread = Thread.currentThread();

            try {
                Log.d(TAG, "Start sleep");
                Thread.sleep(10000);
                Log.d(TAG, "Stop sleep");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

}