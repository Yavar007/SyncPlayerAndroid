package com.yavar007.syncplayer.misc;

import java.security.SecureRandom;

public class RoomIdGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ROOM_ID_LENGTH = 10;
    private static SecureRandom random = new SecureRandom();

    public String generateRoomId() {
        StringBuilder roomId = new StringBuilder(ROOM_ID_LENGTH);
        for (int i = 0; i < ROOM_ID_LENGTH; i++) {
            roomId.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return roomId.toString();

    }
}
