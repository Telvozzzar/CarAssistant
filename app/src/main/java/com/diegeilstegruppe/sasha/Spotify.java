package com.diegeilstegruppe.sasha;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.diegeilstegruppe.sasha.audio.WavAudioRecorder;
import com.diegeilstegruppe.sasha.service.Notifications.BusProvider;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Alina on 20.08.2017.
 */

public class Spotify {
    private static Spotify instance;
    //Spotify Player
    private SpotifyService spotifyWebApi;
    private static final String CLIENT_ID = "7ae9d4102d804979b912d01b36b4fe66";
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";
    private static String ACCESS_TOKEN;
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;
    private Player spotifyPlayer;
    private SpotifyApi api = new SpotifyApi();
    private Context context;
    private Activity activity;
    ConnectionStateCallback connectionStateCallback;
    Player.NotificationCallback notificationCallback;
    private boolean wasPlaying = false;

    public static Spotify getInstance(){
        //TODO unschön!
        return instance;
    }

    public static Spotify getInstance(Context _context, Activity _activity, ConnectionStateCallback _connectionStateCallback, Player.NotificationCallback _notificationCallback){
        if(Spotify.instance == null) {
            Spotify.instance = new Spotify( _context,  _activity,  _connectionStateCallback,  _notificationCallback);
        }
        BusProvider.getInstance().register(instance);
        return instance;
    }

    private Spotify(Context _context, Activity _activity, ConnectionStateCallback _connectionStateCallback, Player.NotificationCallback _notificationCallback){
        context = _context;
        activity = _activity;
        logintoSpotify();
        spotifyWebApi = api.getService();
        connectionStateCallback = _connectionStateCallback;
        notificationCallback = _notificationCallback;
    }


    public boolean isPlaying(){
        if(spotifyPlayer.getPlaybackState().isPlaying)
            return true;
        else
            return false;
    }

    public void record(final WavAudioRecorder wavRecorder, final String mFileName){
        if(spotifyPlayer.getPlaybackState().isPlaying) {
            wasPlaying = true;
            spotifyPlayer.pause(null);
        }
        wavRecorder.setOutputFile(mFileName);
        wavRecorder.prepare();
        wavRecorder.start();
    }



    //Authentication and Initialization
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
                        Spotify.this.spotifyPlayer = spotifyPlayer;
                        Spotify.this.spotifyPlayer.addConnectionStateCallback(connectionStateCallback);
                        Spotify.this.spotifyPlayer.addNotificationCallback(notificationCallback);
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
            builder.setScopes(new String[]{"user-read-private", "streaming","user-library-read"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);
            return true;
        }catch (Exception e){
            Toast.makeText(context, "Login not successfull!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    //User Content
    public void showMyPlaylists(Object offset ){
        Map<String, Object> options = new HashMap<>();
        options.put("offset", offset);
        options.put("limit", 50);
        spotifyWebApi.getMyPlaylists(options,new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                BusProvider.getInstance().post(playlistSimplePager);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Spotify", error.getMessage());
            }
        });
    }
    public void showMyTracks(Object offset ){
        Map<String, Object> options = new HashMap<>();
        options.put("offset", offset);
        options.put("limit", 50);
        spotifyWebApi.getMySavedTracks(options,new Callback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                BusProvider.getInstance().post(savedTrackPager);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Spotify", error.getMessage());
            }
        });
    }
    public void showMyAlbums(Object offset ) {
        Map<String, Object> options = new HashMap<>();
        options.put("offset", offset);
        options.put("limit", 50);
        spotifyWebApi.getMySavedAlbums(options,new Callback<Pager<SavedAlbum>>() {
            @Override
            public void success(Pager<SavedAlbum> savedAlbumPager, Response response) {
                BusProvider.getInstance().post(savedAlbumPager);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    //Controlls
    public void skipToNext(){
        if(spotifyPlayer.getMetadata().nextTrack!=null)
            spotifyPlayer.skipToNext(new Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    Log.d("Spotify", "Skip successful!");
                }

                @Override
                public void onError(Error error) {
                    Log.d("Spotify", "Skip unsuccessful");
                }
            });
    }
    public void skipToPrevious(){
        if(spotifyPlayer.getMetadata().prevTrack != null){
            spotifyPlayer.skipToPrevious(new Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    Log.d("Spotify", "Skip successful!");
                }

                @Override
                public void onError(Error error) {
                    Log.d("Spotify", "Skip unsuccessful");
                }
            });
        }
    }
    public void resume(){
        if(spotifyPlayer.getMetadata().nextTrack!=null)
            spotifyPlayer.resume(null);
    }
    public void logout(){
        spotifyPlayer.logout();
    }
    public void pause(){
        spotifyPlayer.pause(null);
    }
    public void playUri(String uri){
        spotifyPlayer.playUri(null,uri,0,0); //this is the SpotifyPlayer. Just check its methods
        Log.d("Spotify.playUri", "play Song successful!");
    }
    public void addSongToQueue(String uri) {
        spotifyPlayer.queue(null,uri);
    }
    public void resumeIfWasPlaying() {
        if (wasPlaying) {
            spotifyPlayer.resume(null);
            wasPlaying = false;
        }
    }
    public Player getSpotifyPlayer(){
        return spotifyPlayer;
    }

    //Search
    public void searchAndQueueSong(String query){
        spotifyWebApi.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                BusProvider.getInstance().post(tracksPager);
                String bestMatch = tracksPager.tracks.items.iterator().next().uri;   //get first element of results
                spotifyPlayer.queue(null,bestMatch); //this is the SpotifyPlayer. Just check its methods
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Callback Failure", error.getMessage());

            }
        });
    }
    public void searchPlaylists(String query){
        spotifyWebApi.searchPlaylists(query, new Callback<PlaylistsPager>() {
            @Override
            public void success(PlaylistsPager playlistsPager, Response response) {
                BusProvider.getInstance().post(playlistsPager);
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO
            }
        });
    }
    public void searchAlbum(String query) {
        spotifyWebApi.searchAlbums(query, new Callback<AlbumsPager>() {
            @Override
            public void success(AlbumsPager albumsPager, Response response) {
                BusProvider.getInstance().post(albumsPager);
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO
            }
        });
    }
    public void searchAndPlaySong(String query){
        spotifyWebApi.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                BusProvider.getInstance().post(tracksPager);
                String bestMatch = tracksPager.tracks.items.iterator().next().uri;   //get first element of results
                spotifyPlayer.playUri(null,bestMatch,0,0); //this is the SpotifyPlayer. Just check its methods
                Log.d("Spotify.searchAndPLay", "searchAndPLay successful!");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Spotify.searchAndPLay", error.getMessage());

            }
        });

    }
    public void searchTracks(String query){
        spotifyWebApi.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                BusProvider.getInstance().post(tracksPager);
                Log.d("showSearchReults", "Success!");
            }

            @Override
            public void failure(RetrofitError error) {

                Log.d("searchTracks", "Failure! " + error.getMessage());
            }
        });
    }
    public void searchArtist(String query) {
        spotifyWebApi.searchArtists(query, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                BusProvider.getInstance().post(artistsPager);
                Log.d("showSearchReults", "Success!");
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
    //BusListener
    @Subscribe
    public void onUriPost(String uri){
        instance.playUri(uri);
    }


}

