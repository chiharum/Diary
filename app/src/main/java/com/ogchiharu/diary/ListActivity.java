package com.ogchiharu.diary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Calendar;

public class ListActivity extends AppCompatActivity {

    int todayYear, todayMonth, todayDay, todayMaxDaysOfMonth, todayDate, screenYear, screenMonth, screenDay, screenMaxDaysOfMonth, screenDate, gap;
    ArrayAdapter arrayAdapter;
    Calendar calendar;

    ListView listView;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        listView = (ListView)findViewById(R.id.listView);

        calendar = Calendar.getInstance();
        todayYear = calendar.get(Calendar.YEAR);
        todayMonth = calendar.get(Calendar.MONTH);
        todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        todayDate = todayDay + todayMonth * 100 + todayYear * 10000;
        todayMaxDaysOfMonth = calendar.get(Calendar.DATE);

        screenYear = todayYear;
        screenMonth = todayMonth;
        screenDay = todayDay;
        screenMaxDaysOfMonth = todayMaxDaysOfMonth;
        screenDate = todayDate;

        // 2016年2月16日　「なになに」

        gap = todayMaxDaysOfMonth - todayDay;

        for(int i = 0; i < todayMaxDaysOfMonth; i++){
            String text;
            text = todayYear + "年" + todayMonth + "月" + (1 + i) + "日" + search(1 + i + todayMonth * 100 + todayYear * 10000);
            arrayAdapter.add(text);
        }

        listView.setAdapter(arrayAdapter);
    }

    public void previous(View view){
        if(screenMonth == 1){
            screenYear -= 1;
            screenMonth = 12;
        }else{
            screenMonth -= 1;
        }
        calendar.set(Calendar.MONTH, screenMonth);
        screenMaxDaysOfMonth = calendar.get(Calendar.DATE);

        for(int i = 0; i < screenMaxDaysOfMonth; i++){
            String text;
            text = screenYear + "年" + screenMonth + "月" + (1 + i) + "日" + search(1 + i + screenMonth * 100 + screenYear * 10000);
            arrayAdapter.add(text);
        }

        listView.setAdapter(arrayAdapter);
    }

    public void next(View view){
        if(screenMonth == 12){
            screenYear += 1;
            screenMonth = 1;
        }else{
            screenMonth += 1;
        }
        calendar.set(Calendar.MONTH, screenMonth);
        screenMaxDaysOfMonth = calendar.get(Calendar.DATE);

        for(int i = 0; i < screenMaxDaysOfMonth; i++){
            String text;
            text = screenYear + "年" + screenMonth + "月" + (1 + i) + "日" + search(1 + i + screenMonth * 100 + screenYear * 10000);
            arrayAdapter.add(text);
        }

        listView.setAdapter(arrayAdapter);
    }

    public String search(int dateData){
        Cursor cursor = null;
        String result = "";

        try{
            cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"todayDate", "diary"}, "todayDate = ?", new String[]{String.valueOf(dateData)}, null, null, null);

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
