package com.diegeilstegruppe.sasha.audio;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by denys on 20/05/2017.
 */

public class Speech {

    TextToSpeech textToSpeech;

    // TODO: use android string resource
    private final static String NEW_MESSAGE_TEXT = "Du hast eine neue %s Nachricht von %s! Soll ich sie dir vorlesen?";

    public Speech(Context context) {

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.GERMAN);
                }
            }
        });
    }

    protected void speak(String text) {
        if (Build.VERSION.SDK_INT >= 21) {
            textToSpeech.speak(text, MODE_PRIVATE, null, null);
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void sayNewMessage(String app, String name) {
        String string = String.format(NEW_MESSAGE_TEXT, app, name);
        this.speak(string);
    }

    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}