package com.diegeilstegruppe.sasha.network;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedHashTreeMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by denys on 19/05/2017.
 */

public class ServerResponse implements Serializable {

    public final int INTENT_PLAY = 1;
    public final int INTENT_QUEUE = 2;

    @SerializedName("msg_id")
    private String msgId;
    @SerializedName("_text")
    private String text;
    @SerializedName("entities")
    private Object entities;

    public ServerResponse(String msgId, String text, Object entities) {
        this.msgId = msgId;
        this.text = text;
        this.entities = entities;
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

    public Object getEntities() { return entities; }

    public void setEntities(Entities entities) {
        this.entities = entities;
    }
}

