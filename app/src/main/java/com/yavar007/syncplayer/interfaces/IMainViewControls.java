package com.yavar007.syncplayer.interfaces;


import com.yavar007.syncplayer.models.UsersInRoomModel;

public interface IMainViewControls {
        void setUsername(String username);
        void setMovieLink(String movieLink);
        void setMovieTime(String movieTime);
        void addNewSpectator(UsersInRoomModel spectator);
        void showMessage(String message);
}

