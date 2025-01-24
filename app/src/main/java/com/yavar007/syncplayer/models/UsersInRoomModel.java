package com.yavar007.syncplayer.models;

public class UsersInRoomModel {
    private String ID;
    private String nickName;
    private String OSName;
    private String clientRole;
    private boolean isOnline;
    private long lastAlive;
    public UsersInRoomModel() {
    }

    public UsersInRoomModel(String ID, String nickName, String OSName, String clientRole, boolean isOnline,long lastAlive) {
        this.ID = ID;
        this.nickName = nickName;
        this.OSName = OSName;
        this.clientRole = clientRole;
        this.isOnline = isOnline;
        this.lastAlive = lastAlive;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getOSName() {
        return OSName;
    }

    public void setOSName(String OSName) {
        this.OSName = OSName;
    }

    public String getClientRole() {
        return clientRole;
    }

    public void setClientRole(String clientRole) {
        this.clientRole = clientRole;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public long getLastAlive() {
        return lastAlive;
    }

    public void setLastAlive(long lastAlive) {
        this.lastAlive = lastAlive;
    }
}
