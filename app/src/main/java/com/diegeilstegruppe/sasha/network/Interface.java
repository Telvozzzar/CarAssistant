package com.diegeilstegruppe.sasha.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by denys on 19/05/2017.
 */

public interface Interface {

    @GET("/message")
    Call<ServerResponse> get(
            @Query("q") String query,
            @Query("v") String version
    );
}
