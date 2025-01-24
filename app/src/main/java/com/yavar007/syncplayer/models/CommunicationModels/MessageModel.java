package com.yavar007.syncplayer.models.CommunicationModels;

import com.fasterxml.jackson.annotation.JsonProperty;
public class MessageModel {
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("deviceName")
    private String deviceName;
    @JsonProperty("deviceOs")
    private String deviceOs;
    @JsonProperty("clientRole")
    private String clientRole;
    @JsonProperty("userName")
    private String userName;
    public MessageModel(String id, String type, String deviceName, String deviceOs, String clientRole, String userName) {
        this.id = id;
        this.type = type;
        this.deviceName = deviceName;
        this.deviceOs = deviceOs;
        this.clientRole = clientRole;
        this.userName = userName;
    }

    public MessageModel() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        this.deviceOs = deviceOs;
    }

    public String getClientRole() {
        return clientRole;
    }

    public void setClientRole(String clientRole) {
        this.clientRole = clientRole;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
