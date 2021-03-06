package com.ogchiharu.diary;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EditorCustomAdapter extends ArrayAdapter<editorItem> {

    LayoutInflater layoutInflater;

    public EditorCustomAdapter(Context context, int resource, List<editorItem> items){
        super(context, resource, items);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        final ViewHolder viewHolder;

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.editor_list, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        editorItem item = getItem(position);

        if(item != null){
            viewHolder.diaryText.setText(item.content);
        }

        return convertView;
    }

    private class ViewHolder{
        TextView diaryText;

        public ViewHolder(View view){
            diaryText = (TextView)view.findViewById(R.id.diaryText);
        }
    }
}
