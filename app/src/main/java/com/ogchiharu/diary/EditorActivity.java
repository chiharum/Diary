package com.ogchiharu.diary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class EditorActivity extends AppCompatActivity {

    EditText editText;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        editText = new EditText(this);


    }

    public String search(int dateData){
        Cursor cursor = null;
        String result = "";

        try{
            cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"date", "diary"}, "date = ?", new String[]{String.valueOf(dateData)}, null, null, null);

            int indexDiary = cursor.getColumnIndex("diary");

            while(cursor.moveToNext()){
                result = cursor.getString(indexDiary);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }
}
