package com.diegeilstegruppe.sasha;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Alina on 20.08.2017.
 */

public class Spotify {
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

    public Spotify(Context _context, Activity _activity, ConnectionStateCallback _connectionStateCallback, Player.NotificationCallback _notificationCallback){
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

    public boolean pauseWhileRecording(final WavAudioRecorder wavRecorder, final String mFileName){
        if(spotifyPlayer.getPlaybackState().isPlaying) {
            spotifyPlayer.pause(new Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    wavRecorder.setOutputFile(mFileName);
                    wavRecorder.prepare();
                    wavRecorder.start();
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
            wavRecorder.setOutputFile(mFileName);
            wavRecorder.prepare();
            wavRecorder.start();
            return false;
        }
    }

    public void resume(){
    spotifyPlayer.resume(null);
}
    public void logout(){
    spotifyPlayer.logout();
}
    public void pause(){
    spotifyPlayer.pause(null);
}

    public void skipToNext(){
    spotifyPlayer.skipToNext(null);
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
        spotifyWebApi.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                String bestMatch = tracksPager.tracks.items.iterator().next().uri;   //get first element of results
                spotifyPlayer.queue(null,bestMatch); //this is the SpotifyPlayer. Just check its methods
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Callback Failure", error.getMessage());

            }
        });
    }

    public void searchAndPlaySong(String query){
        spotifyWebApi.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
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
    public void showSearchResults(String query){
        spotifyWebApi.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                BusProvider.getInstance().post(tracksPager);
                Log.d("showSearchReults", "Success!");
            }

            @Override
            public void failure(RetrofitError error) {

                Log.d("showSearchResults", "Failure! " + error.getMessage());
            }
        });
    }


}
class SpotifySearchAdapter extends RecyclerView.Adapter<SpotifySearchAdapter.ViewHolder> {
    ArrayList<Track> mDataset;
    Context mContext;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public String uri;
        public TextView title, subtitle;
        public ImageView cover;
        public ViewHolder(View v) {
            super(v);
            title = (TextView)  v.findViewById(R.id.entity_title);
            subtitle  = (TextView) v.findViewById(R.id.entity_subtitle);
            cover = (ImageView) v.findViewById(R.id.entity_image);
        }
        @Override
        public void onClick(View v) {
            BusProvider.getInstance().post(uri);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SpotifySearchAdapter(ArrayList<Track> myDataset, Context mContext) {
        mDataset = myDataset;
        this.mContext = mContext;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SpotifySearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.title.setText(mDataset.get(position).name);
        holder.subtitle.setText(mDataset.get(position).album.name);
        Image image = mDataset.get(position).album.images.get(0);
        if (image != null) {
            Picasso.with(mContext).load(image.url).into(holder.cover);
        }
        holder.uri = mDataset.get(position).uri;

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
