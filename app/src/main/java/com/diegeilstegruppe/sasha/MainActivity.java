package com.diegeilstegruppe.sasha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.diegeilstegruppe.sasha.audio.WavAudioRecorder;
import com.diegeilstegruppe.sasha.network.Communicator;
import com.diegeilstegruppe.sasha.network.ResponseEvent;
import com.diegeilstegruppe.sasha.service.Notifications.BusProvider;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.otto.Subscribe;
import java.io.File;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback,ConnectionStateCallback {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private WavAudioRecorder wavAudioRecorder;
    private Communicator communicator;
    private com.diegeilstegruppe.sasha.Spotify spotify;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
            final String mFileName =  getCacheDir().getAbsolutePath() + "/audio.wav";
            wavAudioRecorder = WavAudioRecorder.getInstance();
            spotify = new com.diegeilstegruppe.sasha.Spotify(this, MainActivity.this, MainActivity.this, MainActivity.this);
            final Button button = (Button) findViewById(R.id.btn_record);
            BusProvider.getInstance().register(this);
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        spotify.pauseWhileRecording(wavAudioRecorder, mFileName);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        // Released

                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        wavAudioRecorder.stop();

                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        wavAudioRecorder.reset();

                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        spotify.resume();
                        communicator = new Communicator();

                        File file = new File(mFileName);
                        communicator.uploadFile(file);

                    }
                    return true;
                }
            });


        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
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
    @Subscribe public void answerAvailable(ResponseEvent event) {

        if(event.getResponse().getText().toLowerCase().contains("spotify")){
            if(event.getResponse().getText().toLowerCase().contains("logout")){
                spotify.logout();
                return;
            }
            if(event.getResponse().getText().toLowerCase().contains("login")) {
                spotify.logintoSpotify();
                return;
            }
            if(event.getResponse().getText().toLowerCase().contains("play")){
                    String searchquery = event.getResponse().getEntities().getSearchQuery().iterator().next().getValue();
                    spotify.searchAndPlaySong(searchquery);
                return;
            }
            if(event.getResponse().getText().toLowerCase().contains("pause")) {
                spotify.pause();
                return;
            }
            if(event.getResponse().getText().toLowerCase().contains("skip")) {
                spotify.skipToNext();
                return;
            }
            if(event.getResponse().getText().toLowerCase().contains("addToQueue")) {
                spotify.searchAndQueueSong(event.getResponse().getEntities().getSearchQuery().iterator().next().getValue());
                return;
            }
            if(event.getResponse().getText().toLowerCase().contains("resume")) {
                spotify.resume();
                return;
            }
            //if(event.getResponse().getText().toLowerCase().contains("search"))

            Toast toast = Toast.makeText(getApplicationContext(), "No Intent found!", Toast.LENGTH_LONG);
            toast.show();
            return;
            }
        }

        /*switch (event.getResponse().getEntities().getIntent().iterator().next().getValue()) {
            case "logout":
                spotify.logout();
                break;
            case "login":
                spotify.logintoSpotify();
                break;
            case "play":
                String searchquery = event.getResponse().getEntities().getSearchQuery().iterator().next().getValue();
                spotify.searchAndPlaySong(searchquery);
                break;
            case "pause":
                spotify.pause();
                break;
            case "skip":
                spotify.skipToNext();
                break;
            case "addToQueue":
                spotify.searchAndQueueSong(event.getResponse().getEntities().getSearchQuery().iterator().next().getValue());
                break;
            case "resume":
                //TODO:add debug information and logs whatsoever
                spotify.resume();
                break;
            case "search":
                //make something to display search!
                break;
            default:
                Toast toast = Toast.makeText(getApplicationContext(), "No Intent found!", Toast.LENGTH_LONG);
                toast.show();
                return;
        }*/

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyAudioDeliveryDone:
                Log.d("onPlayBackEvent","kSpPlayBackNotifyAudioDelifery");
                spotify.skipToNext();
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        spotify.loggedin();
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        spotify.startPlayer(requestCode, resultCode, intent);
        }

}
