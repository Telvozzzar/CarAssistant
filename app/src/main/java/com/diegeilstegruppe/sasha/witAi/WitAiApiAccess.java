package com.diegeilstegruppe.sasha.witAi;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.diegeilstegruppe.sasha.Services.Notifications.BusProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

public class WitAiApiAccess {

    private static final String TAG = "WitAiApiAccess";

    private static final String API_URL = "https://api.wit.ai/";
    private static final String API_KEY = "HLL2QUN7QCVTVWDL4PNN5XJAIBRFTMBZ";
    private static final String API_VER = "20170820";


    public WitAiApiAccess() {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new BasicAuthInterceptor("Bearer", WitAiApiAccess.API_KEY));
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
    }

    private Retrofit retrofit;




    public void uploadFile(final File file) {
        // create upload service client
        IwitAiApi service = retrofit.create(IwitAiApi.class);
        // finally, execute the request
        Call<WitAiResponse> call = service.post(RequestBody.create(MediaType.parse("audio/wav"), file));

        call.enqueue(new Callback<WitAiResponse>() {
            @Override
            public void onResponse(Call<WitAiResponse> call,
                                   final Response<WitAiResponse> response) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(response.body());
                    }
                });
                Log.v("Upload", "success");
                file.delete();
            }

            @Override
            public void onFailure(Call<WitAiResponse> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                file.delete();
            }
        });
    }

    public void sendText(String s) {
        IwitAiApi service = retrofit.create(IwitAiApi.class);
        // finally, execute the request
        Call<WitAiResponse> call = service.get(s);
        call.enqueue(new Callback<WitAiResponse>() {
            @Override
            public void onResponse(Call<WitAiResponse> call, final Response<WitAiResponse> response) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(response.body());
                    }
                });
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<WitAiResponse> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }
}
