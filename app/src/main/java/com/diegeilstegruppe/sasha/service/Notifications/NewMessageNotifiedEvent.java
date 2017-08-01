package com.diegeilstegruppe.sasha.service.Notifications;

/**
 * Created by denys on 20/05/2017.
 */

public class NewMessageNotifiedEvent {

    protected String message;

    public NewMessageNotifiedEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
