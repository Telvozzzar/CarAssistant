package com.diegeilstegruppe.sasha.network;

/**
 * Created by burin on 20.05.2017.
 */

public class ResponseEvent {
    private ServerResponse response;
    public ResponseEvent(ServerResponse response) {
        this.response = response;
    }

    public ServerResponse getResponse() {
        return response;
    }

    public void setResponse(ServerResponse response) {
        this.response = response;
    }
}
