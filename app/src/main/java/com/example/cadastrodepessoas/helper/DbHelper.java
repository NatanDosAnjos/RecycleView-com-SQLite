package com.example.cadastrodepessoas.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

        private static String NAME_DB = "DB_PESSOAS";
        public static String TABLE_PESSOAS = "pessoas";
        private static int VERSION = 1;

    public DbHelper(@Nullable Context context) {
        super(context, NAME_DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_PESSOAS
                + " (nome TEXT NOT NULL, idade INTEGER (3));";

        try {
            db.execSQL(sql);
        } catch (Exception e) {
            Log.i("INFO_DB", "Error creating table" + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void resetTable(SQLiteDatabase db, String tableName) {
        dropTable(db, tableName);
        onCreate(db);
    }

    public void dropTable(SQLiteDatabase db, String tableName) {
        String sqlCommand = "DROP TABLE IF EXISTS " + TABLE_PESSOAS + " ;";

        db.execSQL(sqlCommand);
    }
}
