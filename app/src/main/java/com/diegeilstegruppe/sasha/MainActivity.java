package com.diegeilstegruppe.sasha;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import java.io.*;

import com.diegeilstegruppe.sasha.audio.SpeechRecorder;
import com.diegeilstegruppe.sasha.network.Communicator;

import java.io.File;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // TODO: only test
    private static final int SPEECH_REQUEST_CODE = 0;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private Communicator communicator;
    private SpeechRecorder speechRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        communicator = new Communicator();
        speechRecorder = new SpeechRecorder(this);

        Button button = (Button) findViewById(R.id.button);
        Button button2 = (Button) findViewById(R.id.button2);

        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "click");

                        communicator.send("baaaasinga");
                    }
                }
        );

//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                displaySpeechRecognizer();
//            }
//        });

        button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "pressed");
                        speechRecorder.startRecording();
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "released");
                        speechRecorder.stopAndPlayRecording();
                        String path = speechRecorder.getFileName();

                        communicator.uploadFile(getFile(path));
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
    }

    private File getFile(String path) {
        File toReturn = new File(path);
        return toReturn;
    }

    @Override
    public void onStop(){
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
}
