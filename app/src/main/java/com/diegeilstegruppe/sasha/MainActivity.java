package com.diegeilstegruppe.sasha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.diegeilstegruppe.sasha.audio.Speech;
import com.diegeilstegruppe.sasha.audio.SpeechRecorder;
import com.diegeilstegruppe.sasha.audio.WavAudioRecorder;
import com.diegeilstegruppe.sasha.network.Communicator;
import com.diegeilstegruppe.sasha.service.Notifications.BusProvider;
import com.diegeilstegruppe.sasha.service.Spotify.SpotifyPlayerReference;
import com.diegeilstegruppe.sasha.service.Spotify.spotifyController;
import com.spotify.sdk.android.player.Player;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // TODO: only test
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int REQUEST_CODE = 1337;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};


    private SpeechRecorder speechRecorder;
    private Speech speech;
    private WavAudioRecorder wavAudioRecorder;
    private MediaPlayer mediaPlayer;
    private Communicator communicator;
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

        /*final String mFileName =  getCacheDir().getAbsolutePath() + "/audio.wav";
        wavAudioRecorder = WavAudioRecorder.getInstanse();
        wavAudioRecorder.setOutputFile(mFileName);*/
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        /*final TextView text = (TextView) findViewById(R.id.textView);

        final Switch switch_activated = (Switch) findViewById(R.id.switch_activated);
        switch_activated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    text.setText("Recording");
                } else {
                    text.setText("Not recording");
                }
            }
        });

        final Switch switch_demo = (Switch) findViewById(R.id.switch_demo);
        switch_demo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wavAudioRecorder.prepare();
                    wavAudioRecorder.start();
                } else {
                    wavAudioRecorder.stop();
                    wavAudioRecorder.reset();
                    try{
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(mFileName);
                        mediaPlayer.prepare();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    mediaPlayer.start();

                    communicator = new Communicator();

                    File file = new File(mFileName);
                    communicator.uploadFile(file);

                }
            }
        });

        Log.d(TAG, "onCreate");

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        speechRecorder = new SpeechRecorder(this);
        speech = new Speech(this);
*/
            Intent intent = new Intent(this, spotifyController.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE:
                SpotifyPlayerReference.player.playUri(null, "spotify:track:3d9DChrdc6BOeFsbrZ3Is0", 0, 0);
                HttpRequestTask.doSomething(player,SpotifyPlayerReference.accessToken);
        }
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
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

    }
}
