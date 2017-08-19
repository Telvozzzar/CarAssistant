
package com.diegeilstegruppe.sasha.network;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerResponse {

    @SerializedName("msg_id")
    @Expose
    private String msgId;
    @SerializedName("_text")
    @Expose
    private String text;
    @SerializedName("entities")
    @Expose
    private Entities entities;

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

    public Entities getEntities() {
        return entities;
    }

    public void setEntities(Entities entities) {
        this.entities = entities;
    }

}