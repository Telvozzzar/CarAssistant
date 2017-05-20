package com.diegeilstegruppe.sasha.network;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by denys on 19/05/2017.
 */

public class ServerResponse implements Serializable {
    @SerializedName("msg_id")
    private String msgId;
    @SerializedName("_text")
    private String text;

    public ServerResponse(String msgId, String text) {
        this.msgId = msgId;
        this.text = text;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

