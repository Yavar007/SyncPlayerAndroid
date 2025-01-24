package com.yavar007.syncplayer.models.CommunicationModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestToJoinMessageModel extends MessageModel {
    @JsonProperty("roomId")
    private String roomId;
    public RequestToJoinMessageModel(){
        super();
    }
    public RequestToJoinMessageModel(String id,String type, String deviceName, String deviceOs, String clientRole, String roomId,String userName) {
        super(id,type,deviceName,deviceOs,clientRole,userName);
        this.roomId = roomId;
    }


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

}
