package com.diegeilstegruppe.sasha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.diegeilstegruppe.sasha.audio.Speech;
import com.diegeilstegruppe.sasha.audio.SpeechRecorder;
import com.diegeilstegruppe.sasha.audio.WavAudioRecorder;
import com.diegeilstegruppe.sasha.network.Communicator;
import com.diegeilstegruppe.sasha.network.ResponseEvent;
import com.diegeilstegruppe.sasha.service.Notifications.BusProvider;
import com.google.gson.internal.LinkedHashTreeMap;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.otto.Subscribe;

import org.w3c.dom.Node;

import java.io.File;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback,ConnectionStateCallback {

    private static final String TAG = "MainActivity";


    //Spotify Player
    private static final String CLIENT_ID = "7ae9d4102d804979b912d01b36b4fe66";
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";
    private static String ACCESS_TOKEN;
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private SpotifyApi api = new SpotifyApi();


    // TODO: only test
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};


    private SpeechRecorder speechRecorder;
    private Speech speech;
    private WavAudioRecorder wavAudioRecorder;
    private MediaPlayer mediaPlayer;
    private Communicator communicator;
    private Player player;
    private SpotifyService spotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            final String mFileName =  getCacheDir().getAbsolutePath() + "/audio.wav";
            wavAudioRecorder = WavAudioRecorder.getInstance();



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
*/

        //Spotify Player
            logintoSpotify();

            final Button button = (Button) findViewById(R.id.btn_record);
            spotify = api.getService();

            BusProvider.getInstance().register(this);

            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // Pressed
                        if(mPlayer.getPlaybackState().isPlaying) {
                            mPlayer.pause(new Player.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    wavAudioRecorder.setOutputFile(mFileName);
                                    wavAudioRecorder.prepare();
                                    wavAudioRecorder.start();
                                }

                                @Override
                                public void onError(Error error) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Pause not successfull!", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });
                        }
                        else {
                            wavAudioRecorder.setOutputFile(mFileName);
                            wavAudioRecorder.prepare();
                            wavAudioRecorder.start();
                        }
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        // Released

                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        wavAudioRecorder.stop();

                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        wavAudioRecorder.reset();

                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        /*try{
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(mFileName);
                            mediaPlayer.prepare();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        mediaPlayer.start();*/

                        mPlayer.resume(null);
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
    @Subscribe public void answerAvailable(ResponseEvent event) {

        switch (event.getResponse().getEntities().getIntent().iterator().next().getValue()) {
            case "logout":
                mPlayer.logout();
                break;
            case "login":
                logintoSpotify();
                break;
            case "play":
                String searchquery = event.getResponse().getEntities().getSearchQuery().iterator().next().getValue();
                searchAndPlaySong(searchquery);
                break;
            case "pause":
                mPlayer.pause(null);
                break;
            case "skip":
                mPlayer.skipToNext(null);
                break;
            case "addToQueue":
                searchAndQueueSong(event.getResponse().getEntities().getSearchQuery().iterator().next().getValue());
                break;
            case "resume":
                //TODO:add debug information and logs whatsoever
                mPlayer.resume(null);
                break;
            case "search":
                //make something to display search!
                break;
            default:
                Toast toast = Toast.makeText(getApplicationContext(), "No Intent found!", Toast.LENGTH_LONG);
                toast.show();
                return;
        }
    }
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
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }



    //Overrides from spotify Player
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyAudioDeliveryDone:
                Log.d("onPlayBackEvent","kSpPlayBackNotifyAudioDelifery");
                mPlayer.skipToNext(null);
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        api.setAccessToken(ACCESS_TOKEN);
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

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                        ACCESS_TOKEN = response.getAccessToken();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    private void searchAndPlaySong(String query){
        spotify.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                String bestMatch = tracksPager.tracks.items.iterator().next().uri;   //get first element of results
                mPlayer.playUri(null,bestMatch,0,0); //this is the SpotifyPlayer. Just check its methods
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Callback Failure", error.getMessage());

            }
        });
    }

    private void searchAndQueueSong(String query){
        spotify.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                String bestMatch = tracksPager.tracks.items.iterator().next().uri;   //get first element of results
                mPlayer.queue(null,bestMatch); //this is the SpotifyPlayer. Just check its methods
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Callback Failure", error.getMessage());

            }
        });
    }

    private boolean logintoSpotify(){
        try {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN,
                    REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            return true;
        }catch (Exception e){
            Toast toast = Toast.makeText(getApplicationContext(), "Login not successfull!", Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
    }
}
