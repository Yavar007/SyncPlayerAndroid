package com.yavar007.syncplayer.models.CommunicationModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerMessageModel extends MessageModel {

    @JsonProperty("message")
    private String message;
    @JsonProperty("roomId")
    private String roomId;  // New field for room ID

    public PlayerMessageModel() {
        super();
    }


    // Constructor, getters, and setters...

    public PlayerMessageModel(String id,String type, String deviceName, String deviceOs, String clientRole,String userName,String message, String roomId) {
        super(id,type,deviceName,deviceOs,clientRole,userName);
        this.message = message;
        this.roomId = roomId;

    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
