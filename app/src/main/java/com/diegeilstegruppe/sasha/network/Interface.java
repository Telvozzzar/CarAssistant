package com.diegeilstegruppe.sasha.network;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import static android.R.attr.name;

/**
 * Created by denys on 19/05/2017.
 */

public interface Interface {

    @GET("/message")
    Call<ServerResponse> get(
            @Query("q") String query,
            @Query("v") String version
    );

    @POST("/speech")
    Call<ServerResponse> post(
            @Header("Content-Type") String contenttype,
            @Body File file
    );
}
