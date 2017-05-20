package com.diegeilstegruppe.sasha;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.diegeilstegruppe.sasha.audio.SpeechRecorder;
import com.diegeilstegruppe.sasha.network.Communicator;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // TODO: only test
    private static final int SPEECH_REQUEST_CODE = 0;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private Communicator communicator;
    private SpeechRecorder speechRecorder;

    TextToSpeech t1;
    CharSequence newMessageReceivedText = "Du hast eine neue Nachricht! Soll ich sie dir vorlesen?";

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

        Log.d(TAG, "onCreate");

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        communicator = new Communicator();
        speechRecorder = new SpeechRecorder(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (speechRecorder != null) {
            speechRecorder.onActivityStop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

//    // Create an intent that can start the Speech Recognizer activity
//    private void displaySpeechRecognizer() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        // Start the activity, the intent will be populated with the speech text
//        startActivityForResult(intent, SPEECH_REQUEST_CODE);
//    }
//
//    // This callback is invoked when the Speech Recognizer returns.
//    // This is where you process the intent and extract the speech text from the intent.
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode,
//                                    Intent data) {
//        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
//            List<String> results = data.getStringArrayListExtra(
//                    RecognizerIntent.EXTRA_RESULTS);
//            String spokenText = results.get(0);
//            // Do something with spokenText
//
//            Log.d(TAG, spokenText);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    protected void tts(String text){
        boolean read = true;
        if (Build.VERSION.SDK_INT >= 21){
            t1.speak(newMessageReceivedText, MODE_PRIVATE, null, null);
            //HIER DAS ZUHÖREN AUF JA ODER NEIN und in read Variable
            if(read){
                t1.speak((CharSequence) text, MODE_PRIVATE, null, null);
            }


        }else{
            t1.speak((String) newMessageReceivedText, TextToSpeech.QUEUE_FLUSH, null);
            //HIER DAS ZUHÖREN AUF JA ODER NEIN und in read Variable
            if(read){
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }

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
