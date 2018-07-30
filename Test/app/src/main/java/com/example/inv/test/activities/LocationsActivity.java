package com.example.inv.test.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.inv.test.R;
import com.example.inv.test.utils.Adapters.LocationsAdapter;
import com.example.inv.test.utils.Elements.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

//Запускается из Лобби, чтобы посмотреть список игроков
public class LocationsActivity extends Activity implements AdapterView.OnItemClickListener {
    String [] locationsArray = {"Embassy", "School",
                                "Ocean Liner", "Beach", "Broadway theater", "Casino", "Circus tent",
                                "Bank", "Spa saloon", "Hotel", "Restaurant", "Supermarket",
                                "Service station", "Hospital", "Military base", "Police station",
                                "University", "Airplane", "Passenger train", "Submarine",
                                "Cathedral", "Corporate party", "Crusader army", "Pirate ship",
                                "Polar station", "Space station"};
    ListView lvMain;

    ArrayList<Location> locations = new ArrayList<Location>();
    LocationsAdapter locationsAdapter;

    Button btnBack;
    String role;
    boolean returnable = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locations_activity);
        fillData();
        locationsAdapter = new LocationsAdapter(this, locations);

        // настраиваем список
        lvMain = (ListView) findViewById(R.id.lvMain);

        lvMain.setAdapter(locationsAdapter);

        lvMain.setOnItemClickListener(this);
        btnBack = findViewById(R.id.btnBack);
        Intent intent = this.getIntent();
        role = intent.getStringExtra("role");
        returnable = intent.getBooleanExtra("returnable", true);
        btnBack.setClickable(returnable);
    }

    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        if (role.equals("spy")) {

            Location clickedlocation = (Location)locationsAdapter.getItem(position);

            Intent intent = new Intent();
            intent.putExtra("exit", "guess");
            intent.putExtra("location", clickedlocation.location);
            intent.putExtra("id", clickedlocation.id);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void goBack(View v) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra("exit", "noguess");
        finish();
    }


    private void fillData() { //здесь мы делаем запрос на сервер: 'method':'getplayersinfo' Возвращает словарь "имя":"готовнсоть"
        for (int i = 0; i < 26; i++) {
            locations.add(new Location(locationsArray[i], i));
        }
    }
}