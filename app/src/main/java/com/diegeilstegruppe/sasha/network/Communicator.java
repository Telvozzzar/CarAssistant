package com.diegeilstegruppe.sasha.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.diegeilstegruppe.sasha.service.Notifications.BusProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;

import java.io.File;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
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
    private static final String API_KEY = "MRWFOWZ4LKEMEJ5K7GDMQJWUO6CWHPAZ";
    private static final String API_VER = "20170815";

    private witAiApi service;

    public Communicator() {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new BasicAuthInterceptor("Bearer", Communicator.API_KEY));
        httpClient.addInterceptor(logging);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        //The Retrofit builder will have the client attached, in order to get connection logs
        retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(API_URL)
                .build();

        service = retrofit.create(witAiApi.class);
    }

    private Retrofit retrofit;




    public void uploadFile(final File file) {
        // create upload service client
        witAiApi service = retrofit.create(witAiApi.class);
        // finally, execute the request
        Call<ServerResponse> call = service.post(RequestBody.create(MediaType.parse("audio/wav"), file));

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call,
                                   final Response<ServerResponse> response) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(new ResponseEvent(response.body()));
                    }
                });
                Log.v("Upload", "success");
                file.delete();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                file.delete();
            }
        });
    }

    @Produce
    public ResponseEvent produceResponseEvent(ServerResponse serverResponse) {
        return new ResponseEvent(serverResponse);
    }
}
