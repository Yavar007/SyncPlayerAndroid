package com.yavar007.syncplayer.interfaces;

import com.yavar007.syncplayer.models.ClientModel;
import com.yavar007.syncplayer.models.UsersInRoomModel;

import java.util.List;

public interface IRoomDatabaseHelper {
    void insertData(UsersInRoomModel client);
    void updateData(UsersInRoomModel client);
    void deleteData(UsersInRoomModel client);
    UsersInRoomModel selectData(String id);
    List<UsersInRoomModel> selectAllData();
    void deleteAllData();
}
