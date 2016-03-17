package com.ogchiharu.diary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    static final String DATABASE = "diary.database";
    static final int DATABASE_VERSION = 1;
    static final String DIARY_TABLE = "diary";
    static final String TAGS_TABLE = "tags";


    public MySQLiteOpenHelper(Context context){

        super(context, DATABASE, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase database){

        database.execSQL("create table " + DIARY_TABLE + " (id integer primary key autoincrement not null, tag text not null, date integer not null, diary text)");
        database.execSQL("create table " + TAGS_TABLE + " (id integer primary key autoincrement not null, tag text not null)");

    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        
    }
}
