package com.diegeilstegruppe.sasha.network;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by denys on 19/05/2017.
 */

public class Communicator {

    private static final String TAG = "Communicator";

    private static final String API_URL = "https://api.wit.ai/";
    private static final String API_KEY = "66NIUDMXAU6R25XPLDJXKL72S3BVVVWN";
    private static final String API_VER = "19.05.2017";

    private Interface service;

    public Communicator() {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new BasicAuthInterceptor("Bearer", Communicator.API_KEY));
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(API_URL)
                .build();

        service = retrofit.create(Interface.class);
    }

    public void send(String query) {

        Call<ServerResponse> call = service.get(query, Communicator.API_VER);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                // BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG,"Success");
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handle execution failures like no internet connectivity
                // BusProvider.getInstance().post(new ErrorEvent(-2,t.getMessage()));
                Log.e(TAG,"Failure");
            }
        });
    }
}
