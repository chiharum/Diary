package com.ogchiharu.diary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

        setTitle("「" + tag + "」" + "の" + getString(R.string.list));

        gap = todayMaxDaysOfMonth - todayDay;

        setListView();
        listView.setSelection(todayDay - 1);
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
                item = new Item(dateText, searchDiary(tag,date));
            }else{
                item = new Item(dateText, searchDiary(tag, date));
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
                intent.putExtra("tag", tag);
                startActivity(intent);
            }
        });
    }

    public String searchDiary(String tag, int dateData){
        Cursor cursor = null;
        String result = null;

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

                    if(result == null){
                        result = cursor.getString(indexTag) + "：" + cursor.getString(indexDiary);
                    }else{
                        result = result + "\n" + cursor.getString(indexTag) + "：" + cursor.getString(indexDiary);
                    }
                }else{

                    if(result == null){
                        result = tag + "：" + cursor.getString(indexDiary);
                    }else{
                        result = result + "\n" + tag + "：" + cursor.getString(indexDiary);
                    }
                }
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){

            Intent intent = new Intent();
            intent.setClass(ListActivity.this, MainActivity.class);
            startActivity(intent);

            return super.onKeyDown(keyCode, event);
        }else{
            return false;
        }
    }
}
