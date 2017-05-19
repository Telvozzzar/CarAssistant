package com.diegeilstegruppe.sasha.network;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by denys on 19/05/2017.
 */

public class BasicAuthInterceptor implements Interceptor {

    private String credentials;

    public BasicAuthInterceptor(String user, String password) {
        this.credentials = Credentials.basic(user, password);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                // .header("Authorization", credentials).build();
                // TODO: why so sick?
                .header("Authorization", "Bearer 66NIUDMXAU6R25XPLDJXKL72S3BVVVWN").build();

        return chain.proceed(authenticatedRequest);
    }

}