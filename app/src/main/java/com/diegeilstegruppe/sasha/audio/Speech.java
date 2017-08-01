package com.diegeilstegruppe.sasha.audio;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.diegeilstegruppe.sasha.service.Notifications.BusProvider;
import com.diegeilstegruppe.sasha.service.Notifications.NewMessageNotifiedEvent;
import com.squareup.otto.Produce;

import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by denys on 20/05/2017.
 */

public class Speech implements TextToSpeech.OnInitListener {

    TextToSpeech textToSpeech;
    private Context ctx;
    public final static String TAG = "Speech";

    // TODO: use android string resource
    private final static String NEW_MESSAGE_TEXT = "Du hast eine neue %s Nachricht von %s! Soll ich sie dir vorlesen?";

    public Speech(Context context) {
            ctx = context;
            textToSpeech = new TextToSpeech(context, this);
    }

    protected void speakDone()  {
        Log.d(TAG, "Speak Done");
        // TODO: we need message
        // BusProvider.getInstance().post(new NewMessageNotifiedEvent("test123123123"));

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                BusProvider.getInstance().post(new NewMessageNotifiedEvent("test123123123"));
            }
        });
    }

    protected void speak(String text) {
        if (Build.VERSION.SDK_INT >= 21) {
            Log.i(TAG, "speak; " + text );
            textToSpeech.speak(text, MODE_PRIVATE, null, "messageID");
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void sayNewMessage(String app, String name) {
        String string = String.format(NEW_MESSAGE_TEXT, app, name);
        this.speak(string);
    }

    public void readNewMessage(final String message) {
        this.speak(message);
    }

    public void onDestroy() {
        Log.d(TAG, "TTS Shutdown");
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Produce
    public NewMessageNotifiedEvent produceNewMessageNotifiedEvent(String message) {
        return new NewMessageNotifiedEvent(message);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if(textToSpeech.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                textToSpeech.setLanguage(Locale.US);
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                         @Override
                         public void onStart(String utteranceId) {

                         }

                         @Override
                         public void onDone(String utteranceId) {
                             speakDone();
                         }

                        @Override
                         public void onError(String utteranceId) {

                         }
                     });
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(ctx, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
}
