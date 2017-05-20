package com.diegeilstegruppe.sasha.network;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by denys on 19/05/2017.
 */

public class ServerResponse implements Serializable {
    @SerializedName("msg_id")
    private String msgId;
    @SerializedName("_text")
    private String text;
//    @SerializedName("entities")
//    private ArrayList<String> entities = new ArrayList<>();

    public ServerResponse(String msgId, String text) {
        this.msgId = msgId;
        this.text = text;
        //this.entities = entities;
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

//    public ArrayList<String> getEntities() { return entities; }
//
//    public void setEntities(ArrayList<String> entities) {
//        this.entities = entities;
//    }
}

