package com.example.cadastrodepessoas.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.cadastrodepessoas.model.Person;

import java.util.List;

public class PersonDAO implements IPersonDAO {

    private SQLiteDatabase write;
    private SQLiteDatabase read;

    public PersonDAO(Context context) {
        DbHelper db = new DbHelper(context);
        write       = db.getWritableDatabase();
        read        = db.getReadableDatabase();
    }

    @Override
    public boolean save(Person person) {

        ContentValues cv = new ContentValues();
        cv.put("nome", person.getName());
        cv.put("idade", person.getAge());

        try {
            write.insert(DbHelper.TABLE_PESSOAS, null, cv);
        } catch (Exception e) {
            Log.e("INFO", "Error saving on database");
            return false;
        }

        return true;
    }

    @Override
    public boolean update(Person person) {
        return false;
    }

    @Override
    public boolean delete(Person person) {
        return true;
    }

    @Override
    public List<Person> list() {
        return null;
    }
}
