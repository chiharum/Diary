package com.ogchiharu.diary;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends AppCompatActivity {

    int editingYear, editingMonth, editingDay, date, tagsNumbers, diaryAmount, saveNumber, editorTagId;
    String editorTag;
    TextView editorDateText;
    EditText editText;
    ListView editorList;
    EditorCustomAdapter customAdapter;
    List<editorItem> items;

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
        editorList = (ListView)findViewById(R.id.editorList);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        tagsNumbers = sharedPreferences.getInt("tagsAmounts", 0);
        editingYear = getIntent().getIntExtra("year", 0);
        editingMonth = getIntent().getIntExtra("month", 0);
        editingDay = getIntent().getIntExtra("day", 0);
        editorTagId = getIntent().getIntExtra("tag", 0);

        editorTag = searchTag(editorTagId);

        date = editingDay + editingMonth * 100 + editingYear * 10000;
        diaryAmount = (int) DatabaseUtils.queryNumEntries(database, MySQLiteOpenHelper.DIARY_TABLE, "date = ? and tag = ?", new String[]{String.valueOf(date), editorTag});
        items = new ArrayList<>();

        editorDateText.setText(editingYear + "年" + editingMonth + "月" + editingDay + "日");

        setListView();
    }

    public void setListView(){

        items.clear();

        if(diaryAmount != 0){
            for (int a = 0; a < diaryAmount; a++) {

                String itemText = searchDiary(date, editorTag)[a];

                editorItem item;
                item = new editorItem(itemText);
                items.add(item);
            }
        }

        customAdapter = new EditorCustomAdapter(this, R.layout.editor_list, items);
        editorList.setAdapter(customAdapter);
        editorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                editDiary(position + 1, false);
            }
        });

        editorList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                eraseCheckDialog(position + 1);
                return false;
            }
        });
    }

    public void eraseCheckDialog(final int erasingNumber){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("このデータを削除します");
        adb.setMessage("このデータを削除します。よろしいですか？");
        adb.setPositiveButton("削除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                erase(erasingNumber);
                diaryAmount = diaryAmount - 1;
                setListView();
            }
        });
        adb.setNegativeButton("キャンセル", null);
        adb.show();
    }

    public void erase(int number){

        database.delete(MySQLiteOpenHelper.DIARY_TABLE, "date = " + date + " and number = " + number, null);
    }

    public void editDiary(final int number, final boolean isNewDiary){

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View adbLayout = inflater.inflate(R.layout.add_tag_dialog, null);

        editText = (EditText)adbLayout.findViewById(R.id.tagEditText);
        Button addDiaryButton = (Button)adbLayout.findViewById(R.id.addTagButton);
        addDiaryButton.setText(getString(R.string.done));

        if(!isNewDiary){
            editText.setText(searchDiary(date, editorTag)[number - 1]);
        }

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
                if(isNewDiary){
                    save(number, text);
                    diaryAmount = diaryAmount + 1;
                }else{
                    update(date, editorTag, text, number);
                }
                setListView();
                alertDialog.dismiss();
            }
        };

        adbLayout.findViewById(R.id.addTagButton).setOnClickListener(listener);

        alertDialog.setView(adbLayout);
        alertDialog.show();
    }

    public String[] searchDiary(int dateData, String tag){

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

    public String searchTag(int tagId){

        Cursor cursor = null;
        String resultTag = "";

        try{
            cursor = database.query(MySQLiteOpenHelper.TAGS_TABLE, new String[]{"tag"}, "id = ?", new String[]{String.valueOf(tagId)}, null, null, null);

            int indexTag = cursor.getColumnIndex("tag");

            while(cursor.moveToNext()){
                resultTag = cursor.getString(indexTag);
            }
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return resultTag;
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

    public void update(int date, String tag, String diary, int number){

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("tag", tag);
        values.put("diary", diary);
        values.put("number", number);

        database.update(MySQLiteOpenHelper.DIARY_TABLE, values, "number = " + number, null);
    }

    public void goBackToList(){

        Intent intent = new Intent();
        intent.setClass(EditorActivity.this, ListActivity.class);
        intent.putExtra("tag", editorTagId);
        startActivity(intent);
    }

    public void addDiary(View view){

        editDiary(diaryAmount + 1, true);
    }

    public void eraseAll(View view){

        saveNumber = 1;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("確認");
        alertDialog.setMessage("この日の分のデータを消去します。よろしいですか。");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {

                database.delete(MySQLiteOpenHelper.DIARY_TABLE, "date = " + date, null);
                diaryAmount = 0;
                setListView();
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
