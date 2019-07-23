package com.balakrishnan.poorna.call_record;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends ArrayAdapter{
    private Context context;
    private ArrayList<Name> list;
    TextView textView2,textView;

    public MyAdapter(Context context,ArrayList<Name> list){
        super(context, R.layout.mylayout,list);
        this.context=context;
        this.list=list;

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView =inflater.inflate(R.layout.mylayout,parent,false);
        textView=(TextView)rowView.findViewById(R.id.textView2);
        textView.setText(list.get(position).getName());
        textView2=(TextView)rowView.findViewById(R.id.textView3);
        textView2.setText(list.get(position).getNum());

        return rowView;
    }

    public String getTextView() {
        return textView.getText().toString();
    }

    public String getTextView2() {
        return textView2.getText().toString();
    }
}
