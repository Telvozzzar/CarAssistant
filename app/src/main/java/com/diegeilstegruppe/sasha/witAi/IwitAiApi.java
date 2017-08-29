package com.diegeilstegruppe.sasha.witAi;

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

public interface IwitAiApi {

    @GET("/message")
    Call<WitAiResponse> get(
            @Query("q") String query
    );


    @Headers("Content-Type: audio/wav")
    @POST("/speech")
    Call<WitAiResponse> post(
            @Body RequestBody file
    );
}
