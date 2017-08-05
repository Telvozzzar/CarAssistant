package com.diegeilstegruppe.sasha.service.Spotify;

import com.spotify.sdk.android.player.Player;

import kaaes.spotify.webapi.android.models.Album;

/**
 * Created by Telvozzzar on 04.08.2017.
 */

public class SpotifyPlayerReference {

    public static Player player;
    public static String accessToken;
    public static Album album;

    public static boolean isInit(){
        if(player==null || accessToken == null) return false;
        return true;
    }

}
