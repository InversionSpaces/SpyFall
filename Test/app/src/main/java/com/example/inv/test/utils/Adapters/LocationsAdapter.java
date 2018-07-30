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
import com.example.inv.test.utils.Elements.Location;


public class LocationsAdapter extends BaseAdapter {

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Location> locs;

    public LocationsAdapter(Context context, ArrayList<Location> locations) {
        ctx = context;
        locs = locations;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return locs.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return locs.get(position);
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
            view = lInflater.inflate(R.layout.itemlocation_element, parent, false);
        }

        Location loc = getLocation(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvId)).setText(loc.id);
        ((TextView) view.findViewById(R.id.tvLocation)).setText(loc.location);
        return view;
    }

    Location getLocation(int position) {
        return ((Location) getItem(position));
    }
}

