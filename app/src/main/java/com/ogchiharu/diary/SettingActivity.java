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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    int tagsNumbers, originalTagId;
    String originalTagName;

    EditText editText;
    Button addTagButton, settingButton1, settingButton2, settingButton3, settingButton4;
    SharedPreferences sharedPreferences;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setTitle(getString(R.string.setting));

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        tagsNumbers = sharedPreferences.getInt("tagsNumbers", 0);

        settingButton1 = (Button)findViewById(R.id.button7);
        settingButton2 = (Button)findViewById(R.id.button8);
        settingButton3 = (Button)findViewById(R.id.button9);
        settingButton4 = (Button)findViewById(R.id.button6);

        settingButton1.setHeight(20);
        settingButton2.setHeight(20);
        settingButton3.setHeight(20);
        settingButton4.setHeight(20);
    }

    public void addTag(View view){

        nameEdit(0);
    }

    public void insertTag(String tagName){

        ContentValues values = new ContentValues();
        values.put("tag", tagName);

        database.insert(MySQLiteOpenHelper.TAGS_TABLE, null, values);
    }

    public void erase_tag(View view){

        String[] items = new String[tagsNumbers + 1];

        int i;
        for(i = 1; i <= tagsNumbers; i += 1){
            items[i - 1] = search(i);
        }

        items[tagsNumbers] = getString(R.string.all_erase_tag);

        final int[] checked = new int[1];

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.erase_tag_title));
        adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checked[0] = which;
            }
        });

        adb.setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(checked[0] == tagsNumbers + 1){
                    for(int i = 1; i <= tagsNumbers; i += i){
                        database.delete(MySQLiteOpenHelper.TAGS_TABLE, "id = " + i, null);
                    }
                    Toast.makeText(SettingActivity.this, "すべてのカテゴリーを消去しました。ただし、" + getString(R.string.first_inserted_tag_name) + "カテゴリーは削除されません。カテゴリーの名前を変える場合は「カテゴリーの名前を変更」から変更してください。", Toast.LENGTH_SHORT).show();

                    mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
                    database = mySQLiteOpenHelper.getWritableDatabase();

                    insertTag(getString(R.string.first_inserted_tag_name));

                    sharedPreferences.edit().putInt("tagsNumbers", 1).apply();
                    tagsNumbers = 1;
                }else{

                    if(tagsNumbers == 1){
                        Toast.makeText(getApplicationContext(), "カテゴリーは最低ひとつは必要です。カテゴリーの名前を変える場合は「カテゴリーの名前を変更」から変更してください。", Toast.LENGTH_SHORT).show();
                    }else{
                        database.delete(MySQLiteOpenHelper.TAGS_TABLE, "id = " + checked[0], null);
                        Toast.makeText(getApplicationContext(), "カテゴリー「" + search(checked[0] + 1) + "」を削除しました", Toast.LENGTH_SHORT).show();
                        sharedPreferences.edit().putInt("tagsNumbers", tagsNumbers - 1).apply();
                        tagsNumbers -= 1;
                    }
                }
            }
        });
        adb.setNegativeButton(getString(R.string.cancel), null);
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

    public void erase_all(View view){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("確認");
        alertDialog.setMessage("すべてのデータを消去します。よろしいですか。");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {

                deleteDatabase(MySQLiteOpenHelper.DATABASE);

                mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
                database = mySQLiteOpenHelper.getWritableDatabase();

                insertTag(getString(R.string.first_inserted_tag_name));
                tagsNumbers = 1;
                sharedPreferences.edit().putInt("tagsNumbers", tagsNumbers).apply();
                Toast.makeText(SettingActivity.this, "消去しました。", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("キャンセル", null);
        alertDialog.show();
    }

    public void renameTag(View view){

        final String[] items = new String[tagsNumbers];

        int i;
        for(i = 1; i <= tagsNumbers; i += 1){
            items[i - 1] = search(i);
        }

        final int[] checked = new int[1];

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.rename_tag_title));
        adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checked[0] = which;
            }
        });

        adb.setPositiveButton(getString(R.string.rename), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                nameEdit(1);
                originalTagId = checked[0] + 1;
                originalTagName = items[originalTagId - 1];
            }
        });
        adb.setNegativeButton(getString(R.string.cancel), null);
        adb.show();
    }

    public void nameEdit(final int editType){

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View adbLayout = inflater.inflate(R.layout.add_tag_dialog, null);

        editText = (EditText)adbLayout.findViewById(R.id.tagEditText);
        addTagButton = (Button)adbLayout.findViewById(R.id.addTagButton);
        if(editType == 0){
            addTagButton.setText(getString(R.string.add));
        }else{
            addTagButton.setText(getString(R.string.save));
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        View.OnClickListener listener = new View.OnClickListener(){
            public void onClick(View view1){

                String text;

                SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder)editText.getText();
                if(spannableStringBuilder == null){
                    text = null;
                }else{
                    text = spannableStringBuilder.toString();
                }

                if(text != null){

                    if(text.equals("すべて")){
                        Toast.makeText(SettingActivity.this, "「すべて」という名前のカテゴリーは追加できません。ほかの名前を付けてください。", Toast.LENGTH_SHORT).show();
                    }else if(editType == 0){
                        insertTag(text);
                        Toast.makeText(getApplicationContext(), "カテゴリー「" + text + "」が" + getString(R.string.added), Toast.LENGTH_SHORT).show();
                        sharedPreferences.edit().putInt("tagsNumbers", tagsNumbers + 1).apply();
                        tagsNumbers += 1;
                    }else if(editType == 1){

                        ContentValues values = new ContentValues();
                        values.put("tag", text);
                        database.update(MySQLiteOpenHelper.TAGS_TABLE, values, "id = " + originalTagId, null);
                        Toast.makeText(getApplicationContext(), "カテゴリー「" + originalTagName + "」が「" + text + "」に変更されました", Toast.LENGTH_SHORT).show();
                    }

                }else{

                    Toast.makeText(getApplicationContext(), getString(R.string.not_added), Toast.LENGTH_LONG).show();
                }

                alertDialog.dismiss();
            }
        };

        adbLayout.findViewById(R.id.addTagButton).setOnClickListener(listener);

        alertDialog.setView(adbLayout);
        alertDialog.show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){

            Intent intent = new Intent();
            intent.setClass(SettingActivity.this, MainActivity.class);
            startActivity(intent);

            return super.onKeyDown(keyCode, event);
        }else{
            return false;
        }
    }
}
