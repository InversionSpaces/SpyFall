package com.example.inv.test.utils.Adapters;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.inv.test.R;
import com.example.inv.test.utils.Elements.Message;


public class LobbyMsgAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Message> objects;

    public LobbyMsgAdapter(Context context, ArrayList<Message> msgs) {
        ctx = context;
        objects = msgs;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.message_element, parent, false);
        }

        Message p = getMessage(position);

        ((TextView) view.findViewById(R.id.tvName)).setText(p.name);
        ((TextView) view.findViewById(R.id.tvMsg)).setText(p.msg);

        return view;
    }

    Message getMessage(int position) {
        return ((Message) getItem(position));
    }
}

