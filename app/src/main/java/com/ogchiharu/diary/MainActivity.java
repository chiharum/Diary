package com.ogchiharu.diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    int width, height, tagsAmounts, year, month, day, previousVersion;
    final int presentVersion = 10;
    String editorTag;
    LinearLayout linearLayout;
    SharedPreferences preferences;

    TextView text1, text2, text3;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        tagsAmounts = preferences.getInt("tagsNumbers", 0);
        previousVersion = preferences.getInt("previousVersion", presentVersion);

        //ここに、アップデート後の最初の起動で行う操作を書く。
        //Write here tasks for the first use after an update.

        if(previousVersion == 1 || previousVersion == presentVersion - 1){

            beCompatible();
        }


        beCompatible();


        preferences.edit().putInt("previousVersion", presentVersion).apply();

        text1 = (TextView)findViewById(R.id.editTitleTextView);
        text2 = (TextView)findViewById(R.id.listTitleTextView);
        text3 = (TextView)findViewById(R.id.settingTitleTextView);

        text1.setTypeface(Typeface.SERIF);
        text2.setTypeface(Typeface.SERIF);
        text3.setTypeface(Typeface.SERIF);

        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        if(tagsAmounts == 0){
            insertFirst();
            preferences.edit().putInt("tagsNumbers", 1).apply();
            tagsAmounts = 1;
        }

        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);

        int dpi = getResources().getDisplayMetrics().densityDpi;
        preferences.edit().putInt("dpi", dpi).apply();
    }

    public void beCompatible(){

        long amountOfDiary = DatabaseUtils.queryNumEntries(database, MySQLiteOpenHelper.PRE_DIARY_TABLE);

        for(int countUpId = 1; countUpId <= amountOfDiary; countUpId++){

            ContentValues contentValues = new ContentValues();
            contentValues.put("tag", searchData(countUpId)[0]);
            contentValues.put("date", Integer.parseInt(searchData(countUpId)[1]));
            contentValues.put("diary", searchData(countUpId)[2]);
            contentValues.put("number", 1);

            database.insert(MySQLiteOpenHelper.DIARY_TABLE, null, contentValues);
        }
    }

    public String[] searchData(int id){

        Cursor cursor = null;
        String[] data = new String[3];

        try{
            cursor = database.query(MySQLiteOpenHelper.PRE_DIARY_TABLE, new String[]{"tag", "date", "diary"}, "id = ?", new String[]{String.valueOf(id)}, null, null, null);

            int indexTag = cursor.getColumnIndex("tag");
            int indexDate = cursor.getColumnIndex("date");
            int indexDiary = cursor.getColumnIndex("diary");

            while(cursor.moveToNext()){
                data[0] = cursor.getString(indexTag);
                data[1] = String.valueOf(cursor.getInt(indexDate));
                data[2] = cursor.getString(indexDiary);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return data;
    }

    public void datePicker(){

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int pickerYear, int monthOfYear, int dayOfMonth) {

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, EditorActivity.class);
                intent.putExtra("tag", editorTag);
                intent.putExtra("year", pickerYear);
                intent.putExtra("month", monthOfYear + 1);
                intent.putExtra("day", dayOfMonth);
                startActivity(intent);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    public String searchTag(int searchId){
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

        value.put("tag", getString(R.string.first_inserted_tag_name));

        database.insert(MySQLiteOpenHelper.TAGS_TABLE, null, value);
    }

    public void edit(View view){

        final String[] items = new String[tagsAmounts];

        for(int i = 1; i <= tagsAmounts; i = i + 1){
            items[i - 1] = searchTag(i);
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.chose_category_title));
        adb.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                editorTag = items[which];

                datePicker();
            }
        });
        adb.show();
    }

    public void list(View view){

        final String[] items = new String[tagsAmounts + 1];

        for(int i = 1; i <= tagsAmounts; i = i + 1){
            items[i - 1] = searchTag(i);
        }

        items[tagsAmounts] = "すべて";

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.chose_category_title));
        adb.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ListActivity.class);
                intent.putExtra("tag", items[which]);
                intent.putExtra("year", year);
                intent.putExtra("month", month + 1);
                intent.putExtra("day", day);
                startActivity(intent);
            }
        });
        adb.show();
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
