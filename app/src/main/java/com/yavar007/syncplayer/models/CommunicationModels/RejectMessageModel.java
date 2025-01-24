package com.yavar007.syncplayer.models.CommunicationModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RejectMessageModel extends MessageModel {
    @JsonProperty("message")
    private String message;
    public RejectMessageModel(){
        super();
    }
    public RejectMessageModel(String id,String type, String deviceName, String deviceOs, String clientRole,String userName,String message){
        super(id,type,deviceName,deviceOs,clientRole,userName);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

