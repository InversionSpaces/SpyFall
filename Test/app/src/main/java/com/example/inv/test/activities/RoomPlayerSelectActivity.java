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
import com.example.inv.test.utils.Adapters.RoomPlayerAdapter;
import com.example.inv.test.utils.Elements.RoomPlayer;
import com.example.inv.test.utils.Network.Rotater;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

//Запускается из Лобби, чтобы посмотреть список игроков
public class RoomPlayerSelectActivity extends Activity implements AdapterView.OnItemClickListener {
    //LinearLayout linLayout;
    LayoutInflater ltInflater;
    Iterator<String> iter;
    String [] playersnames = {};

    //String[] names;
    //String[] ids;
    //String[] tips;

    ListView lvMain;

    ArrayList<RoomPlayer> players = new ArrayList<RoomPlayer>();
    RoomPlayerAdapter playerAdapter;

    Button btnDelete;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roomplayerselect_activity);
        fillData();
        playerAdapter = new RoomPlayerAdapter(this, players);

        Intent myintent = this.getIntent();
        boolean exitable = myintent.getBooleanExtra("exitable", true);
        btnDelete = findViewById(R.id.btnBack);
        btnDelete.setClickable(exitable);
        // настраиваем список
        lvMain = (ListView) findViewById(R.id.lvMain);

        lvMain.setAdapter(playerAdapter);

        lvMain.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        RoomPlayer clickedplayer = (RoomPlayer)playerAdapter.getItem(position);
        ContentValues cv = new ContentValues();

        Intent intent = new Intent();
        intent.putExtra("exit", "playersel");
        intent.putExtra("name", clickedplayer.name);
        setResult(RESULT_OK, intent);
        finish();
    }


    private void fillData() { //здесь мы делаем запрос на сервер: 'method':'getplayersinfo' Возвращает словарь "имя":"готовнсоть"

        JSONObject msg = new JSONObject();
        try {
            msg.put("method", "getplayersinfo");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Rotater.addHandler("getplayersinfo", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json) {
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("Ok"):
                            iter = json.keys();
                            for (int i = 0; iter.hasNext(); i++) {
                                String nstring = iter.next();
                                if (!nstring.equals("method") && !nstring.equals("status")) {
                                    playersnames[i] = nstring;
                                }
                            }
                            break;
                        default:
                            Toast.makeText(context, "Unbound answer from server", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Rotater.delHandler("getplayersinfo");
            }
        });
        Rotater.sendMsg(msg);
        for (String playersname : playersnames) {
            players.add(new RoomPlayer(playersname, false));
        }
    }


    void GoBack(View v) {
        Intent intent = new Intent();
        intent.putExtra("exit", "back");
        setResult(RESULT_OK, intent);
        finish();
    }
}