package com.diegeilstegruppe.sasha.witAi;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by denys on 19/05/2017.
 */

public class BasicAuthInterceptor implements Interceptor {

    private String credentials;

    public BasicAuthInterceptor(String user, String password) {
        this.credentials = user + " " + password;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", this.credentials).build();

        return chain.proceed(authenticatedRequest);
    }

}