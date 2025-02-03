package com.yavar007.syncplayer.models;

public class ClientModel {
    private String id;
    private String deviceName;
    private String deviceOs;
    private String UserName;

    public ClientModel(String id, String deviceName, String deviceOs,String UserName) {
        this.id = id;
        this.deviceName = deviceName;
        this.deviceOs = deviceOs;
        this.UserName=UserName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }
}
