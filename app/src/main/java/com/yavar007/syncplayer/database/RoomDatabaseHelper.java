package com.yavar007.syncplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yavar007.syncplayer.interfaces.IRoomDatabaseHelper;
import com.yavar007.syncplayer.models.UsersInRoomModel;

import java.util.ArrayList;
import java.util.List;

public class RoomDatabaseHelper extends SQLiteOpenHelper implements IRoomDatabaseHelper {
    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "UsersInRoom";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_NICKNAME = "nickName";
    private static final String COLUMN_OSNAME = "OSName";
    private static final String COLUMN_CLIENTROLE = "clientRole";
    private static final String COLUMN_ISONLINE = "isOnline";
    private static final String COLUMN_LASTALIVE = "lastAlive";
    private static final int MAX_USERS = 10;

    public RoomDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NICKNAME + " TEXT, " +
                COLUMN_OSNAME + " TEXT, " +
                COLUMN_CLIENTROLE + " TEXT, " +
                COLUMN_ISONLINE + " INTEGER, " +
                COLUMN_LASTALIVE + " INTEGER" +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void insertData(UsersInRoomModel client) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, client.getID());
        values.put(COLUMN_NICKNAME, client.getNickName());
        values.put(COLUMN_OSNAME, client.getOSName());
        values.put(COLUMN_CLIENTROLE, client.getClientRole());
        values.put(COLUMN_ISONLINE, client.isOnline() ? 1 : 0);
        values.put(COLUMN_LASTALIVE, client.getLastAlive());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    @Override
    public void updateData(UsersInRoomModel client) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NICKNAME, client.getNickName());
        values.put(COLUMN_OSNAME, client.getOSName());
        values.put(COLUMN_CLIENTROLE, client.getClientRole());
        values.put(COLUMN_ISONLINE, client.isOnline() ? 1 : 0);
        values.put(COLUMN_LASTALIVE, client.getLastAlive());

        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{client.getID()});
        db.close();
    }

    @Override
    public void deleteData(UsersInRoomModel client) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{client.getID()});
        db.close();
    }

    @Override
    public UsersInRoomModel selectData(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{id}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            UsersInRoomModel user = new UsersInRoomModel(
                    cursor.getString(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_OSNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CLIENTROLE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ISONLINE)) == 1,
                    cursor.getLong(cursor.getColumnIndex(COLUMN_LASTALIVE))
            );
            cursor.close();
            db.close();
            return user;
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return null;
    }

    @Override
    public List<UsersInRoomModel> selectAllData() {
        List<UsersInRoomModel> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                UsersInRoomModel user = new UsersInRoomModel(
                        cursor.getString(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_OSNAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_CLIENTROLE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ISONLINE)) == 1,
                        cursor.getLong(cursor.getColumnIndex(COLUMN_LASTALIVE))
                );
                users.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return users;
    }

    @Override
    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }


    public boolean canAddUser() {
        return selectAllData().size() < MAX_USERS;
    }
}