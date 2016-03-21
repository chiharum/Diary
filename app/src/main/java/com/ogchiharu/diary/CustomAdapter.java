package com.ogchiharu.diary;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<Item> {

    LayoutInflater layoutInflater;

    public CustomAdapter(Context context, int resource, List<Item> items){
        super(context, resource, items);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent){

        final ViewHolder viewHolder;

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.diary_list_layout, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Item item = getItem(position);

        if(item != null){
            viewHolder.dateText.setText(item.dateText);
            viewHolder.contentText.setText(item.diary);
        }

        return convertView;
    }

    private class ViewHolder{
        TextView dateText;
        TextView contentText;

        public ViewHolder(View view){
            dateText = (TextView)view.findViewById(R.id.dateText);
            contentText = (TextView)view.findViewById(R.id.contentText);
        }
    }
}
