package com.yavar007.syncplayer.models.CommunicationModels;


import com.fasterxml.jackson.annotation.JsonProperty;

public class AcceptMessageModel extends MessageModel{

    @JsonProperty("message")
    private String message;
    @JsonProperty("moveLink")
    private String movieLink;
    @JsonProperty("roomId")
    private String roomId;

    public AcceptMessageModel() {

    }
    public AcceptMessageModel(String id,String type, String deviceName, String deviceOs, String clientRole,String userName,String message, String movieLink,String roomId) {
        super(id,type,deviceName,deviceOs,clientRole,userName);
        this.message = message;
        this.movieLink = movieLink;
        this.roomId=roomId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMovieLink() {
        return movieLink;
    }

    public void setMovieLink(String movieLink) {
        this.movieLink = movieLink;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
