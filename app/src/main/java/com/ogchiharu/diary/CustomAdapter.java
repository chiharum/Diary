package com.ogchiharu.diary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<Item> {

    LayoutInflater layoutInflater;

    public CustomAdapter(Context context, int resource, List<Item> items){
        super(context, resource, items);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroupparent){

        final ViewHolder viewHolder;

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.diary_list_layout, null);
        }
    }
}
