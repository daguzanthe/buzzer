package com.example.buzzer_thomas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    ArrayList<User> userList;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, ArrayList<User> userList) {
        this.userList = userList;
        inflter = (LayoutInflater.from(applicationContext));

    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.activity_listview, null);
        TextView user = (TextView)view.findViewById(R.id.user);
        TextView points = (TextView)view.findViewById(R.id.points);
        user.setText(userList.get(i).name);
        points.setText(String.valueOf(userList.get(i).pts));
        return view;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
}
