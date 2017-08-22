package com.diegeilstegruppe.sasha.network;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.diegeilstegruppe.sasha.MainActivity;
import com.diegeilstegruppe.sasha.R;
import com.diegeilstegruppe.sasha.Spotify;
import com.diegeilstegruppe.sasha.service.Notifications.BusProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;

public class SpotifySearchAdapter extends RecyclerView.Adapter<SpotifySearchAdapter.ViewHolder> {
    static ArrayList<Parcelable> mDataset;
    static Context mContext;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        // each data item is just a string in this case
        public String uri;
        public TextView title, subtitle;
        public ImageView cover;

        public ViewHolder(View v) {
            super(v);
            title = (TextView)  v.findViewById(R.id.entity_title);
            subtitle  = (TextView) v.findViewById(R.id.entity_subtitle);
            cover = (ImageView) v.findViewById(R.id.entity_image);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View v) {
            BusProvider.getInstance().post(uri);
        }

        @Override
        public boolean onLongClick(View v) {
            Spotify.getInstance().addSongToQueue(uri);
            Toast.makeText(mContext, "Added!", Toast.LENGTH_LONG).show();
            return true;
        }
    }



    // Provide a suitable constructor (depends on the kind of dataset)
    public SpotifySearchAdapter(ArrayList<Parcelable> myDataset, Context mContext) {
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
        Parcelable element = mDataset.get(position);
        if(element instanceof Track){    //TRACK
            Track track = (Track) element;
            holder.title.setText(track.name);
            holder.subtitle.setText(track.album.name);
            Image image = track.album.images.get(0);
            if (image != null) {
                Picasso.with(mContext).load(image.url).into(holder.cover);
            }
            holder.uri = track.uri;
        }else if(element instanceof TrackSimple){     //TRACKSIMPLE
            TrackSimple trackSimple = (TrackSimple) element;
            holder.title.setText(trackSimple.name);
            holder.subtitle.setText("Placeholder!");
            holder.uri = trackSimple.uri;
        }else if(element instanceof Album){       //ALBUM
            Album album = (Album) element;
            holder.title.setText(album.name);
            holder.subtitle.setText("Placeholder!");
            Image image = album.images.get(0);
            if(image != null){
                Picasso.with(mContext).load(image.url).into(holder.cover);
            }
            holder.uri = album.uri;
        }else if(element instanceof AlbumSimple){       //ALBUMSIMPLE
            AlbumSimple album = (AlbumSimple) element;
            holder.title.setText(album.name);
            holder.subtitle.setText("Placeholder!");
            Image image = album.images.get(0);
            if(image != null){
                Picasso.with(mContext).load(image.url).into(holder.cover);
            }
            holder.uri = album.uri;
        }else if(element instanceof Artist){            //ARTIST
            Artist artist = (Artist) element;
            holder.title.setText(artist.name);
            holder.subtitle.setText("Placeholder!");
            Image image = artist.images.get(0);
            if(image != null){
                Picasso.with(mContext).load(image.url).into(holder.cover);
            }
            holder.uri = artist.uri;
        }else if(element instanceof ArtistSimple){      //ARTISTSIMPLE
            ArtistSimple artistSimple = (ArtistSimple) element;
            holder.title.setText(artistSimple.name);
            holder.subtitle.setText("Placeholder!");
            holder.uri = artistSimple.uri;
        }else if(element instanceof Playlist){            //PLAYLIST
            Playlist playlist = (Playlist) element;
            holder.title.setText(playlist.name);
            holder.subtitle.setText("Placeholder!");
            Image image = playlist.images.get(0);
            if(image != null){
                Picasso.with(mContext).load(image.url).into(holder.cover);
            }
            holder.uri = playlist.uri;
        }else if(element instanceof PlaylistSimple){            //PLAYLISTSIMPLE
            PlaylistSimple playlistSimple = (PlaylistSimple) element;
            holder.title.setText(playlistSimple.name);
            holder.subtitle.setText("Placeholder!");
            Image image = playlistSimple.images.get(0);
            if(image != null){
                Picasso.with(mContext).load(image.url).into(holder.cover);
            }
            holder.uri = playlistSimple.uri;
        }else if(element instanceof SavedAlbum){            //SAVED ALBUM
            SavedAlbum savedAlbum = (SavedAlbum) element;
            holder.title.setText(savedAlbum.album.name);
            holder.subtitle.setText("Placeholder!");
            Image image = savedAlbum.album.images.get(0);
            if(image != null){
                Picasso.with(mContext).load(image.url).into(holder.cover);
            }
            holder.uri = savedAlbum.album.uri;
        }else if(element instanceof SavedTrack) {            //SAVED TRACK
            SavedTrack savedTrack = (SavedTrack) element;
            holder.title.setText(savedTrack.track.name);
            holder.subtitle.setText("Placeholder!");
            Image image = savedTrack.track.album.images.get(0);
            if (image != null) {
                Picasso.with(mContext).load(image.url).into(holder.cover);
            }
            holder.uri = savedTrack.track.uri;
        }
        else{
            holder.title.setText("Not implemented!");
            holder.subtitle.setText("Not implemented!");
        }



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}