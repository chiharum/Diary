package com.ogchiharu.diary;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditorActivity extends AppCompatActivity {

    int year, month, day, date, tagsNumbers;
    String editorTag;
    EditText editText;
    TextView editorDateText;
    ArrayAdapter<String> arrayAdapter;

    Spinner tagsSpinner;

    SharedPreferences sharedPreferences;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        editText = new EditText(this);
        editText = (EditText)findViewById(R.id.editText);
        editorDateText = (TextView)findViewById(R.id.editorDateText);
        tagsSpinner = (Spinner)findViewById(R.id.tagsSpinner);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        tagsNumbers = sharedPreferences.getInt("tagsNumbers", 1);
        year = getIntent().getIntExtra("year", 0);
        month = getIntent().getIntExtra("month", 0);
        day = getIntent().getIntExtra("day", 0);
        editorTag = getIntent().getStringExtra("tag");

        for(int i = 1; i < tagsNumbers; i = i + 1){
            arrayAdapter.add(searchTags(i));
        }
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tagsSpinner.setAdapter(arrayAdapter);
//        tagsSpinner.setSelection(0);
        tagsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Spinner spinner = (Spinner)parent;
                editorTag = (String)spinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        date = day + month * 100 + year * 10000;

        editorDateText.setText(year + "年" + month + "月" + day + "日の予定");

        editText.setText(search(date));
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

    public String searchTags(int idNum){
        Cursor cursor = null;
        String result = "";

        try{
            cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"id", "tag"}, "id = ?", new String[]{String.valueOf(idNum)}, null, null, null);

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

    public void insert(int date, String tag, String diary){

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("tag", tag);
        values.put("diary", diary);

        database.insert(MySQLiteOpenHelper.DIARY_TABLE, null, values);
    }

    public void save(View view){
        SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder)editText.getText();
        String text = spannableStringBuilder.toString();
        insert(date, editorTag, text);

        Toast.makeText(EditorActivity.this, "保存しました。", Toast.LENGTH_SHORT).show();
    }

    public void erase(View view){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("確認");
        alertDialog.setMessage("この日の分のデータを消去します。よろしいですか。");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {
                editText.setText("");
                insert(date,editorTag, "");
                Toast.makeText(EditorActivity.this, "消去しました。", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("キャンセル", null);
        alertDialog.show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){

            Intent intent = new Intent();
            intent.setClass(EditorActivity.this, ListActivity.class);
            intent.putExtra("tag", editorTag);
            startActivity(intent);

            return super.onKeyDown(keyCode, event);
        }else{
            return false;
        }
    }
}
