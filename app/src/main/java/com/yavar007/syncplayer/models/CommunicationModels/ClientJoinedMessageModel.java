package com.yavar007.syncplayer.models.CommunicationModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientJoinedMessageModel extends MessageModel{
    @JsonProperty("roomId")
    private String roomId;
    public ClientJoinedMessageModel(String id, String type, String deviceName, String deviceOs, String clientRole, String userName, String roomId) {
        super(id, type, deviceName, deviceOs, clientRole, userName);
        this.roomId = roomId;
    }
    public ClientJoinedMessageModel() {}

    public String getRoomId() {
        return roomId;
    }

    public void setRoomID(String roomId) {
        this.roomId = roomId;
    }
}
