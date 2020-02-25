package com.example.cadastrodepessoas.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    public static String NAME_DB = "DB_PESSOAS";
    public static String TABLE_PESSOAS = "pessoas";
    public static int VERSION = 1;

    public DbHelper(@Nullable Context context) {
        super(context, NAME_DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_PESSOAS
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL);";

        try {
            db.execSQL(sql);
        } catch (Exception e) {
            Log.i("INFO_DB", "Error creating table" + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
