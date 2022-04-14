package com.example.bbcnewsreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlDbHelper extends SQLiteOpenHelper {
    public static final String SqlDbHelper = "FavouritesDB";
    public static final int VERSION_NUM = 2;
    public static final String TABLE_NAME = "Favourites";

    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "TITLE";
    public static final String COL_DESCRIPTION = "DESCRIPTION";
    public static final String COL_LINK = "LINK";
    public static final String COL_GUID = "GUID";
    public static final String COL_PUBDATE = "PUBDATE";

    public SqlDbHelper(Context context) {
        super(context, SqlDbHelper, null, VERSION_NUM);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String SCRIPT_CREATE_DATABASE = "create table "
                + TABLE_NAME + " ("
                + COL_ID + " integer primary key autoincrement, "
                + COL_TITLE + " text not null, "
                + COL_DESCRIPTION + " text not null, "
                + COL_LINK + " text not null, "
                + COL_GUID + " text not null, "
                + COL_PUBDATE + " text not null);";

        db.execSQL(SCRIPT_CREATE_DATABASE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
