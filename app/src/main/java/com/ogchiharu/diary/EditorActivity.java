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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends AppCompatActivity {

    int editingYear, editingMonth, editingDay, date, tagsNumbers, diaryAmount, saveNumber;
    String editorTag;
    TextView editorDateText;
    EditText editText;
    Button editDiaryButton;
    ListView editorList;
    ArrayAdapter<String> arrayAdapter;
    EditorCustomAdapter customAdapter;
    List<editorItem> items;
    editorItem item;

    Spinner tagsSpinner;

    SharedPreferences sharedPreferences;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        setTitle("一言を編集");

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        editorDateText = (TextView)findViewById(R.id.editorDateText);
        tagsSpinner = (Spinner)findViewById(R.id.tagsSpinner);
        editorList = (ListView)findViewById(R.id.editorList);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        tagsNumbers = sharedPreferences.getInt("tagsNumbers", 0);
        editingYear = getIntent().getIntExtra("year", 0);
        editingMonth = getIntent().getIntExtra("month", 0);
        editingDay = getIntent().getIntExtra("day", 0);
        editorTag = getIntent().getStringExtra("tag");

        date = editingDay + editingMonth * 100 + editingYear * 10000;
        diaryAmount = searchDiaryAmount(date, editorTag);

        editorDateText.setText(editingYear + "年" + editingMonth + "月" + editingDay + "日");

        // スピナー

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for(int i = 1; i <= tagsNumbers; i = i + 1){
            arrayAdapter.add(searchTags(i));
        }
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagsSpinner.setAdapter(arrayAdapter);
        if(editorTag.equals("すべて")){
            tagsSpinner.setSelection(0);
        }else{
            tagsSpinner.setSelection(searchTagId(editorTag) - 1);
        }
        tagsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Spinner spinner = (Spinner) parent;
                editorTag = (String) spinner.getSelectedItem();

                if (!(editorTag.equals("すべて"))) {
                    diaryAmount = searchDiaryAmount(date, editorTag);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // エディターリスト

        items = new ArrayList<>();
        if(editorTag.equals("すべて")){

            for(int a = 1; a <= tagsNumbers; a++){

                diaryAmount = searchDiaryAmount(date, searchTags(a));

                if(diaryAmount == 0){
                    item = new editorItem("");
                }else{
                    for(int b = 0; b < diaryAmount; b++){

                        item = new editorItem(search(date, searchTags(a))[b]);
                    }
                }
                items.add(item);
            }
        }else{

            if(diaryAmount == 0){
                item = new editorItem("");
            }else{
                for(int a = 0; a < diaryAmount; a++){

                    item = new editorItem(search(date, editorTag)[a]);
                }
            }
            items.add(item);
        }

        setListView();
    }

    public void setListView(){

        customAdapter = new EditorCustomAdapter(this, R.layout.editor_list, items);
        editorList.setAdapter(customAdapter);
        editorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            }
        });
    }

    public void editDiary(String tag, final int number){

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View adbLayout = inflater.inflate(R.layout.add_tag_dialog, null);

        editText = (EditText)adbLayout.findViewById(R.id.tagEditText);
        Button addDiaryButton = (Button)adbLayout.findViewById(R.id.addTagButton);
        addDiaryButton.setText(getString(R.string.done));

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        View.OnClickListener listener = new View.OnClickListener(){
            public void onClick(View view){

                String text;

                SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder)editText.getText();
                if(spannableStringBuilder == null){
                    text = null;
                }else{
                    text = spannableStringBuilder.toString();
                }
                save(number, text);

                alertDialog.dismiss();
            }
        };

        adbLayout.findViewById(R.id.addTagButton).setOnClickListener(listener);

        alertDialog.setView(adbLayout);
        alertDialog.show();
    }

    public String[] search(int dateData, String tag){

        Cursor cursor = null;
        String[] result = new String[100];

        try{
            cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"date", "tag", "diary"}, "date = ? and tag = ?", new String[]{String.valueOf(dateData), tag}, null, null, null);

            int indexDiary = cursor.getColumnIndex("diary");

            diaryAmount = 0;
            while(cursor.moveToNext()){
                result[diaryAmount] = cursor.getString(indexDiary);
                diaryAmount++;
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
            cursor = database.query(MySQLiteOpenHelper.TAGS_TABLE, new String[]{"id", "tag"}, "id = ?", new String[]{String.valueOf(idNum)}, null, null, null);

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

    public int searchTagId(String tagName){
        Cursor cursor = null;
        int result = 0;

        try{
            cursor = database.query(MySQLiteOpenHelper.TAGS_TABLE, new String[]{"id", "tag"}, "tag = ?", new String[]{tagName}, null, null, null);

            int indexId = cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                result = cursor.getInt(indexId);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    public int searchDiaryAmount(int date, String tag){

        Cursor cursor = null;
        int result = 0;

        try{
            cursor = database.query(MySQLiteOpenHelper.DIARY_TABLE, new String[]{"date", "tag", "number"}, "date = ? and tag = ?", new String[]{String.valueOf(date), tag}, null, null, null);

            int indexNumber = cursor.getColumnIndex("number");

            while(cursor.moveToNext()){
                result = cursor.getInt(indexNumber);
            }
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    public void insert(int date, String tag, String diary, int number){

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("tag", tag);
        values.put("diary", diary);
        values.put("number", number);

        database.insert(MySQLiteOpenHelper.DIARY_TABLE, null, values);
    }

    public void save(int saveNumber, String text){

        insert(date, editorTag, text, saveNumber);

        Toast.makeText(EditorActivity.this, "保存しました。", Toast.LENGTH_SHORT).show();
    }

    public void goBackToList(){

        Intent intent = new Intent();
        intent.setClass(EditorActivity.this, ListActivity.class);
        intent.putExtra("tag", editorTag);
        startActivity(intent);
    }

    public void erase(View view){

        saveNumber = 1;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("確認");
        alertDialog.setMessage("この日の分のデータを消去します。よろしいですか。");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {
//                editText.setText("");
                insert(date, editorTag, "", saveNumber);
                Toast.makeText(EditorActivity.this, "消去しました。", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("キャンセル", null);
        alertDialog.show();
    }

    public void done(View view){

        goBackToList();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){

            goBackToList();

            return super.onKeyDown(keyCode, event);
        }else{
            return false;
        }
    }
}
