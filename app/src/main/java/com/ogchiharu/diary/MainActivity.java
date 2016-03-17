package com.ogchiharu.diary;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    int width, height, tagsNumbers;
    LinearLayout linearLayout;
    SharedPreferences preferences;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        tagsNumbers = preferences.getInt("tagsNumbers", 1);

        if(tagsNumbers == 1){
            insertFirst();
        }

        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);

        int dpi = getResources().getDisplayMetrics().densityDpi;
        preferences.edit().putInt("dpi", dpi).apply();
    }

    public void edit(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, EditorActivity.class);
        startActivity(intent);
    }

    public void check(View view){

        final String[] items = new String[tagsNumbers + 1];

        for(int i = 1; i <= tagsNumbers; i = i + 1){
            items[i - 1] = search(i);
        }

        items[tagsNumbers] = "すべて";

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("タグを選択してください");
        adb.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ListActivity.class);
                intent.putExtra("tag", items[which]);
                startActivity(intent);
            }
        });
        adb.show();
    }

    public String search(int searchId){
        Cursor cursor = null;
        String result = "";

        try{
            cursor = database.query(MySQLiteOpenHelper.TAGS_TABLE, new String[]{"id", "tag"}, "id = ?", new String[]{String.valueOf(searchId)}, null, null, null);

            int indexTag = cursor.getColumnIndex("tag");

            while(cursor.moveToNext()){
                result = cursor.getString(indexTag);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    public void insertFirst(){

        ContentValues value = new ContentValues();

        value.put("tag", "目標");

        database.insert(MySQLiteOpenHelper.TAGS_TABLE, null, value);
    }

    public void setting(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        width = linearLayout.getWidth();
        height = linearLayout.getHeight();

        preferences.edit().putInt("width", width).apply();
        preferences.edit().putInt("height", height).apply();
    }
}
