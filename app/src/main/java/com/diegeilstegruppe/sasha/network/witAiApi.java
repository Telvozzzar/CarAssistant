package com.diegeilstegruppe.sasha.network;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by denys on 19/05/2017.
 */

public interface witAiApi {

    @GET("/message")
    Call<ServerResponse> get(
            @Query("q") String query
    );


    @Headers("Content-Type: audio/wav")
    @POST("/speech")
    Call<ServerResponse> post(
            @Body RequestBody file
    );
}
