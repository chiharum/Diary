package com.ogchiharu.diary;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    int todayYear, todayMonth, todayDay, todayMaxDaysOfMonth, todayDate, screenYear, screenMonth, screenDay, screenMaxDaysOfMonth, screenDate, gap;
    CustomAdapter customAdapter;
    Calendar calendar;
    List<Item> items;

    ListView listView;
    TextView title;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        listView = (ListView)findViewById(R.id.listView);
        title = (TextView)findViewById(R.id.textView4);

        items = new ArrayList<>();

        calendar = Calendar.getInstance();
        todayYear = calendar.get(Calendar.YEAR);
        todayMonth = calendar.get(Calendar.MONTH) + 1;
        todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        todayDate = todayDay + todayMonth * 100 + todayYear * 10000;
        todayMaxDaysOfMonth = calendar.getActualMaximum(Calendar.DATE);

        screenYear = todayYear;
        screenMonth = todayMonth;
        screenDay = todayDay;
        screenMaxDaysOfMonth = todayMaxDaysOfMonth;
        screenDate = todayDate;

        title.setText(getString(R.string.list) + "（" + screenMonth + "月）");

        gap = todayMaxDaysOfMonth - todayDay;

        setListView();
    }

    public void previous(View view){
        if(screenMonth == 1){
            screenYear -= 1;
            screenMonth = 12;
        }else{
            screenMonth -= 1;
        }
        screenDay = 1;
        calendar.set(screenYear, screenMonth, screenDay);
        screenMaxDaysOfMonth = calendar.getActualMaximum(Calendar.DATE);
        title.setText(getString(R.string.list) + "（" + screenMonth + "月）");

        setListView();
    }

    public void today(View view){
        screenYear = todayYear;
        screenMonth = todayMonth;
        screenDay = todayDay;
        screenMaxDaysOfMonth = todayMaxDaysOfMonth;
        screenDate = todayDate;

        setListView();
    }

    public void next(View view){
        if(screenMonth == 12){
            screenYear += 1;
            screenMonth = 1;
        }else{
            screenMonth += 1;
        }
        screenDay = 1;
        calendar.set(screenYear, screenMonth, screenDay);
        screenMaxDaysOfMonth = calendar.getActualMaximum(Calendar.DATE);
        title.setText(getString(R.string.list) + "（" + screenMonth + "月）");

        setListView();
    }

    public void setListView(){

        items.clear();

        for(int i = 0; i < screenMaxDaysOfMonth; i++){
            String dateText;
            dateText = screenYear + "年" + screenMonth + "月" + (1 + i) + "日";
            Log.i("dateText", dateText);
            Item item = new Item(dateText, search(1 + i + screenMonth * 100 + screenYear * 10000));
            items.add(item);
        }

        customAdapter = new CustomAdapter(this, R.layout.diary_list_layout, items);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent();
                intent.setClass(ListActivity.this, EditorActivity.class);
                intent.putExtra("year", screenYear);
                intent.putExtra("month", screenMonth);
                intent.putExtra("day", position + 1);
                startActivity(intent);
            }
        });
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
