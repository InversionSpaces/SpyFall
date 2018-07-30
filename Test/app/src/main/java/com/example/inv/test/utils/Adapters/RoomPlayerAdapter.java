
package com.example.inv.test.utils.Adapters;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.inv.test.R;
import com.example.inv.test.utils.Elements.RoomPlayer;


public class RoomPlayerAdapter extends BaseAdapter {

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<RoomPlayer> objects;

    public RoomPlayerAdapter(Context context, ArrayList<RoomPlayer> rplayers) {
        ctx = context;
        objects = rplayers;
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
            view = lInflater.inflate(R.layout.itemplayer_element, parent, false);
        }

        RoomPlayer rp = getPlayer(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvName)).setText(rp.name);
        return view;
    }

    RoomPlayer getPlayer(int position) {
        return ((RoomPlayer) getItem(position));
    }
}

