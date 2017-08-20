package com.diegeilstegruppe.sasha;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.diegeilstegruppe.sasha.audio.WavAudioRecorder;
import com.google.common.base.Function;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.File;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Alina on 20.08.2017.
 */

public class Spotify {

    //Spotify Player
    private SpotifyService spotify;
    private static final String CLIENT_ID = "7ae9d4102d804979b912d01b36b4fe66";
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";
    private static String ACCESS_TOKEN;
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private SpotifyApi api = new SpotifyApi();
    private Context context;
    private Activity activity;
    ConnectionStateCallback connectionStateCallback;
    Player.NotificationCallback notificationCallback;

    public Spotify(Context _context, Activity _activity, ConnectionStateCallback _connectionStateCallback, Player.NotificationCallback _notificationCallback){
        context = _context;
        activity = _activity;
        logintoSpotify();
        spotify = api.getService();
        connectionStateCallback = _connectionStateCallback;
        notificationCallback = _notificationCallback;
    }


    public boolean isPlaying(){
        if(mPlayer.getPlaybackState().isPlaying)
            return true;
        else
            return false;
    }

    public boolean pauseWhileRecording(final WavAudioRecorder wavAudioRecorder, final String mFileName){
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
                    Toast toast = Toast.makeText(context, "Pause not successfull!", Toast.LENGTH_LONG);
                    toast.show();
                }
            });
            return true;
        }
        else {
            wavAudioRecorder.setOutputFile(mFileName);
            wavAudioRecorder.prepare();
            wavAudioRecorder.start();
            return false;
        }
    }

public void resume(){
    mPlayer.resume(null);
}
public void logout(){
    mPlayer.logout();
}
    public void pause(){
    mPlayer.pause(null);
}

    public void skipToNext(){
    mPlayer.skipToNext(null);
}
    public void loggedin(){
    Log.d("MainActivity", "User logged in");
    api.setAccessToken(ACCESS_TOKEN);
}

    public void startPlayer(int requestCode, int resultCode, Intent intent){
    if (requestCode == REQUEST_CODE) {
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
        if (response.getType() == AuthenticationResponse.Type.TOKEN) {
            Config playerConfig = new Config(context, response.getAccessToken(), CLIENT_ID);
            com.spotify.sdk.android.player.Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    mPlayer = spotifyPlayer;
                    mPlayer.addConnectionStateCallback(connectionStateCallback);
                    mPlayer.addNotificationCallback(notificationCallback);
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

    public boolean logintoSpotify(){
        try {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN,
                    REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);
            return true;
        }catch (Exception e){
            Toast toast = Toast.makeText(context, "Login not successfull!", Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
    }

    public void searchAndQueueSong(String query){
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

    public String searchAndPlaySong(String query){
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

        return mPlayer.getMetadata().currentTrack.artistName + " - " + mPlayer.getMetadata().currentTrack.name;
    }


}
