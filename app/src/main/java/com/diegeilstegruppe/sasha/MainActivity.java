package com.diegeilstegruppe.sasha;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView text = (TextView) findViewById(R.id.textView);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.GERMAN);
                }
            }
        });

        final Switch switch_activated = (Switch) findViewById(R.id.switch_activated);
        switch_activated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    text.setText("Recording");
                } else {
                    text.setText("Not recording");
                }
            }
        });

    }

    protected void tts(String text){
        if (Build.VERSION.SDK_INT >= 21){
            t1.speak((CharSequence) text, MODE_PRIVATE, null, null);

        }else{
            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }


    }

    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }
}
