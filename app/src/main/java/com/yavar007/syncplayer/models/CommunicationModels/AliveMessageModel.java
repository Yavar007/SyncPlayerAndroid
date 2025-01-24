package com.yavar007.syncplayer.models.CommunicationModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AliveMessageModel extends MessageModel {
    @JsonProperty("roomId")
    private String roomId;


    public AliveMessageModel() {}

    public AliveMessageModel(String id, String type, String deviceName, String deviceOs, String clientRole, String roomId,String username) {
        super(id, type, deviceName, deviceOs, clientRole,username);
        this.roomId = roomId;


    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

}
