package com.ogchiharu.diary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    int todayYear, todayMonth, todayDay, todayMaxDaysOfMonth, todayDate, screenYear, screenMonth, screenDay, screenMaxDaysOfMonth, screenDate, gap;
    String tag;
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

        tag = getIntent().getStringExtra("tag");

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int height = preferences.getInt("height", 0);

        listView = (ListView)findViewById(R.id.listView);
        title = (TextView)findViewById(R.id.listTitleTextView);

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

        title.setText("「" + tag + "」" + "の" + getString(R.string.list) + "（" + screenMonth + "月）");
        title.setTextSize((float) height / 8);

        Log.i("size", String.valueOf(title.getTextSize()));

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
        title.setText("「" + tag + "」" + "の" + getString(R.string.list) + "（" + screenMonth + "月）");

        setListView();
    }

    public void today(View view){
        screenYear = todayYear;
        screenMonth = todayMonth;
        screenDay = todayDay;
        screenMaxDaysOfMonth = todayMaxDaysOfMonth;
        screenDate = todayDate;
        title.setText("「" + tag + "」" + "の" + getString(R.string.list) + "（" + screenMonth + "月）");

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
        title.setText("「" + tag + "」" + "の" + getString(R.string.list) + "（" + screenMonth + "月）");

        setListView();
    }

    public void setListView(){

        items.clear();

        for(int i = 0; i < screenMaxDaysOfMonth; i++){
            String dateText;
            dateText = screenYear + "年" + screenMonth + "月" + (1 + i) + "日";
            int date = 1 + i + screenMonth * 100 + screenYear * 10000;

            Item item;
            if(tag.equals("すべて")){
                item = new Item(dateText, searchDiary(tag, date)[0], searchDiary(tag,date)[1]);
            }else{
                item = new Item(dateText, tag, searchDiary(tag, date)[1]);
            }
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

    public String[] searchDiary(String tag, int dateData){
        Cursor cursor = null;
        String[] result = new String[2];

        try{

            int indexTag = 0;

            if(tag.equals("すべて")){
                cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"tag", "date", "diary"}, "date = ?", new String[]{String.valueOf(dateData)}, null, null, null);
            }else{
                cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"tag", "date", "diary"}, "date = ? and tag = ?", new String[]{String.valueOf(dateData), String.valueOf(tag)}, null, null, null);
                indexTag = cursor.getColumnIndex("tag");
            }

            int indexDiary = cursor.getColumnIndex("diary");

            while(cursor.moveToNext()){

                if(tag.equals("すべて")){
                    result[0] += cursor.getString(indexTag) + "\n";
                }
                result[1] += cursor.getString(indexDiary) + "\n";
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }
}
