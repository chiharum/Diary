package com.ogchiharu.diary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void edit(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, EditorActivity.class);
        startActivity(intent);
    }

    public void check(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ListActivity.class);
        startActivity(intent);
    }

    public void setting(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }
}
