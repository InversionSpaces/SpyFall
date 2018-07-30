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
import com.example.inv.test.utils.Adapters.PlayerAdapter;
import com.example.inv.test.utils.Elements.Player;
import com.example.inv.test.utils.Network.Rotater;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

//Запускается из Лобби, чтобы посмотреть список игроков
public class PlayerSelectActivity extends Activity implements AdapterView.OnItemClickListener {
    //LinearLayout linLayout;
    LayoutInflater ltInflater;
    Iterator<String> iter;
    String [] playersnames = {};// TODO fill
    boolean [] playersstates = {}; //TODO fill

    //String[] names;
    //String[] ids;
    //String[] tips;

    ListView lvMain;

    ArrayList<Player> players = new ArrayList<Player>();
    PlayerAdapter playerAdapter;

    Button btnDelete;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerselect_activity);
        fillData();
        playerAdapter = new PlayerAdapter(this, players);

        // настраиваем список
        lvMain = (ListView) findViewById(R.id.lvMain);

        lvMain.setAdapter(playerAdapter);

        lvMain.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        Player clickedplayer = (Player)playerAdapter.getItem(position);
        ContentValues cv = new ContentValues();

        Intent intent = new Intent();
        intent.putExtra("name", clickedplayer.name);
        intent.putExtra("ready", clickedplayer.ready);
        setResult(RESULT_OK, intent);
        finish();
    }




    private void fillData() { //здесь мы делаем запрос на сервер: 'method':'getplayersinfo' Возвращает словарь "имя":"готовнсоть"

        Rotater.addHandler("getplayersinfo", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json) {
                super.handle(json);
                JSONObject msg = new JSONObject();
                try {
                    msg.put("method", "getplayersinfo");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    switch(json.getString("status")){
                        case("Ok"):
                            for (int i = 0; iter.hasNext(); i++) {
                                JSONObject injson = new JSONObject();
                                injson = json.getJSONObject("dict");

                                iter = injson.keys();
                                String nstring = iter.next();

                                if (!nstring.equals("method") && !nstring.equals("status")) {
                                    playersnames[i] = nstring;
                                    try {
                                        playersstates[i] = json.getBoolean(playersnames[i]);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
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
                Rotater.sendMsg(msg);
            }
        });
        int i = 0;
        for (i = 0; i < playersnames.length; i++) {
            players.add(new Player(playersnames[i], playersstates[i], false));
        }
        players.add(new Player("All", true, false));
    }
}