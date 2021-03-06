package com.ogchiharu.diary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    int todayYear, todayMonth, todayDay, todayMaxDaysOfMonth, todayDate, screenYear, screenMonth, screenDay, screenMaxDaysOfMonth, screenDate, gap, tagsAmounts, tagId;
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

        tagId = getIntent().getIntExtra("tag", 0);
        tag = searchTags(tagId);

        tagsAmounts = (int)DatabaseUtils.queryNumEntries(database, MySQLiteOpenHelper.TAGS_TABLE);

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

        title.setText("「" + tag + "」" + "の" + getString(R.string.list) + "（" + screenYear + "年" + screenMonth + "月）");

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
        listView.setSelection(todayDay - 1);
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
            String dateText = screenYear + "年" + screenMonth + "月" + (1 + i) + "日";
            int date = 1 + i + screenMonth * 100 + screenYear * 10000;

            Item item;
            item = new Item(dateText, searchDiary(tag, date));
            items.add(item);
        }
        customAdapter = new CustomAdapter(this, R.layout.diary_list_layout, items);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(tagId == tagsAmounts + 1){

                    chooseTag(position);
                }else{

                    Intent intent = new Intent();
                    intent.setClass(ListActivity.this, EditorActivity.class);
                    intent.putExtra("year", screenYear);
                    intent.putExtra("month", screenMonth);
                    intent.putExtra("day", position + 1);
                    intent.putExtra("tag", tagId);
                    startActivity(intent);
                }
            }
        });
    }

    public void chooseTag(final int position){
        final String[] items = new String[tagsAmounts];

        for(int i = 1; i <= tagsAmounts; i = i + 1){
            items[i - 1] = searchTags(i);
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.chose_category_title));
        adb.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent();
                intent.setClass(ListActivity.this, EditorActivity.class);
                intent.putExtra("year", screenYear);
                intent.putExtra("month", screenMonth);
                intent.putExtra("day", position + 1);
                intent.putExtra("tag", which + 1);
                startActivity(intent);
            }
        });
        adb.show();
    }

    public String searchTags(int searchId){

        Cursor cursor = null;
        String result = "";

        try{
            cursor = database.query(MySQLiteOpenHelper.TAGS_TABLE, new String[]{"tag"}, "id = ?", new String[]{String.valueOf(searchId)}, null, null, null);

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

    public String searchDiary(String tag, int dateData){

        Cursor cursor = null;
        String result = null;

        try{

            int indexTag = 0;

            if(tagId == tagsAmounts + 1){
                cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"diary"}, "date = ?", new String[]{String.valueOf(dateData)}, null, null, null);
            }else{
                cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"tag", "diary"}, "date = ? and tag = ?", new String[]{String.valueOf(dateData), String.valueOf(tag)}, null, null, null);
                indexTag = cursor.getColumnIndex("tag");
            }

            int indexDiary = cursor.getColumnIndex("diary");

            while(cursor.moveToNext()){

                if(tagId == tagsAmounts + 1){

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
