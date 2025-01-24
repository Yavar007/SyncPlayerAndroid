package com.yavar007.syncplayer.models;

public class ClientModel {
    private String id;
    private String deviceName;
    private String deviceOs;

    public ClientModel(String id, String deviceName, String deviceOs) {
        this.id = id;
        this.deviceName = deviceName;
        this.deviceOs = deviceOs;
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
}
