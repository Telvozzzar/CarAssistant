package com.diegeilstegruppe.sasha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;

import static com.diegeilstegruppe.sasha.R.id.tv_search_query;


public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback,ConnectionStateCallback {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private WavAudioRecorder wavAudioRecorder;
    private Communicator communicator;
    private com.diegeilstegruppe.sasha.Spotify spotify;

    //for the searchView
    private RecyclerView recyclerView;
    private RecyclerView.Adapter searchAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Parcelable> results = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //search results
            recyclerView = (RecyclerView) findViewById(R.id.searchResults);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            //request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);

            //init waveRecorder
            final String mFileName =  getCacheDir().getAbsolutePath() + "/audio.wav";
            wavAudioRecorder = WavAudioRecorder.getInstance();

            //init spotify Player
            spotify = new com.diegeilstegruppe.sasha.Spotify(this, MainActivity.this, MainActivity.this, MainActivity.this);

            //init bus for ServerResponses
            BusProvider.getInstance().register(this);

            //iinit Layout
            final Button button = (Button) findViewById(R.id.btn_record);
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean wasPlaying = false;
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        TextView tv = (TextView) findViewById(tv_search_query);
                        tv.setText("");
                        wasPlaying = spotify.pauseWhileRecording(wavAudioRecorder, mFileName);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        // Released
                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        wavAudioRecorder.stop();

                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        wavAudioRecorder.reset();

                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());

                        communicator = new Communicator();

                        File file = new File(mFileName);
                        communicator.uploadFile(file);
                        if(wasPlaying)
                            spotify.resume();

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
        TextView tv = (TextView) findViewById(tv_search_query);
        String query = event.getResponse().getEntities().getSearchQuery().iterator().next().getValue();
        String intent = event.getResponse().getEntities().getIntent().iterator().next().getValue();
        switch (intent) {
            case "logout":
                tv.setText(intent);
                spotify.logout();
                break;
            case "login":
                tv.setText(intent);
                spotify.logintoSpotify();
                break;
            case "play":
                tv.setText(intent + ": " + query);
                spotify.searchAndPlaySong(query);
                break;
            case "pause":
                tv.setText(intent);
                spotify.pause();
                break;
            case "skip":
                tv.setText(intent);
                spotify.skipToNext();
                break;
            case "addToQueue":
                tv.setText(intent + ": " + query);
                spotify.searchAndQueueSong(query);
                break;
            case "resume":
                tv.setText(intent);
                spotify.resume();
                break;
            case "search":
                tv.setText(intent + ": " + query);
                spotify.showSearchResults(query);
                break;
            case "searchPlaylist":
                tv.setText(intent + ": " + query);
                spotify.searchPlaylists(query);
                break;
            case "searchAlbum":
                tv.setText(intent + ": " + query);
                spotify.searchAlbum(query);
                break;
            default:
                Toast toast = Toast.makeText(getApplicationContext(), "No Intent found!", Toast.LENGTH_LONG);
                toast.show();
                return;
        }
    }

    @Subscribe
    public void resultsReady(TracksPager results){
        this.results.clear();
        for (Track t: results.tracks.items) {
            this.results.add(t);
        }
        searchAdapter = new SpotifySearchAdapter(this.results, getApplicationContext());
        recyclerView.setAdapter(searchAdapter);
    }

    @Subscribe
    public void onUriPost(String uri){
        spotify.playSong(uri);
    }

    @Subscribe
    public void onPlaylistPost(PlaylistsPager playlists){
        this.results.clear();
        for (PlaylistSimple pl: playlists.playlists.items) {
            this.results.add(pl);
        }
        searchAdapter = new SpotifySearchAdapter(this.results, getApplicationContext());
        recyclerView.setAdapter(searchAdapter);
    }
    @Subscribe
    public void onAlbumPost(AlbumsPager albums){
        this.results.clear();
        for (AlbumSimple pl: albums.albums.items) {
            this.results.add(pl);
        }
        searchAdapter = new SpotifySearchAdapter(this.results, getApplicationContext());
        recyclerView.setAdapter(searchAdapter);
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
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyAudioDeliveryDone:
                Log.d("onPlayBackEvent","kSpPlayBackNotifyAudioDelivery");
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
