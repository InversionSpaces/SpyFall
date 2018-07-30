package com.example.inv.test.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.inv.test.R;
import com.example.inv.test.utils.Adapters.RoomsAdapter;
import com.example.inv.test.utils.Elements.HubRoom;
import com.example.inv.test.utils.Network.Rotater;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class HubActivity extends AppCompatActivity{
    private ArrayList<HubRoom> rooms;
    private HashMap<String, Integer> ids;
    private RoomsAdapter roomsAdapter;

    private RecyclerView listrooms;
    private EditText roomname;
    private Button addroom;

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);
        setContentView(R.layout.hub_activity);

        rooms = new ArrayList<>();
        roomsAdapter = new RoomsAdapter(rooms);

        listrooms = findViewById(R.id.ListRooms);
        roomname = findViewById(R.id.RoomName);
        addroom = findViewById(R.id.AddButton);

        listrooms.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listrooms.setItemAnimator(new DefaultItemAnimator());
        listrooms.setAdapter(roomsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        Subscribe();
        GetRooms();
    }

    @Override
    public void onPause(){
        super.onPause();

        UnSubscribe();
        ClearRooms();
    }

    private void ClearRooms(){
        rooms.clear();
        roomsAdapter.notifyDataSetChanged();
    }

    private void GetRooms(){
        JSONObject msg = new JSONObject();

        try{
            msg.put("method", "getroomsinfo");
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        Rotater.addHandler("getroomsinfo", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json) {
                super.handle(json);

                try {
                    JSONObject data = json.getJSONObject("rooms");

                    Iterator<?> names = data.keys();
                    while (names.hasNext()) {
                        String name = (String) names.next();
                        rooms.add(new HubRoom(name, data.getInt(name)));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            roomsAdapter.notifyDataSetChanged();
                        }
                    });
                }
                catch (JSONException e){
                    e.printStackTrace();
                }

                Rotater.delHandler("getroomsinfo");
            }
        });
        Rotater.sendMsg(msg);
    }

    private void Subscribe(){
        Rotater.addHandler("roominfo", new Rotater.methodHandler(this){
            @Override
            public void handle(JSONObject json) {
                super.handle(json);

                try{
                    String status = json.getString("status");
                    final String roomname = json.getString("roomname");
                    int playersnum = json.getInt("playersnum");

                    switch (status){
                        case("UpdRoom"): {
                            for (int i = 0; i < rooms.size(); i++) {
                                HubRoom room = rooms.get(i);
                                if (room.getName().equals(roomname)) {
                                    room.setPlayers(playersnum);
                                    return;
                                }
                            }
                            rooms.add(new HubRoom(roomname, playersnum));
                        }
                        break;
                        case("DelRoom"): {
                            for (int i = 0; i < rooms.size(); i++) {
                                HubRoom room = rooms.get(i);
                                if (room.getName().equals(roomname)) {
                                    rooms.remove(i);
                                }
                            }
                        }
                        break;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            roomsAdapter.notifyDataSetChanged();
                        }
                    });
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void UnSubscribe(){
        Rotater.delHandler("roominfo");
    }

    public void AddRoom(View v){
        addroom.setClickable(false);

        String name = roomname.getText().toString();
        if(name.isEmpty()){
            Toast.makeText(this, "Enter some name", Toast.LENGTH_LONG).show();
        }
        else{
            JSONObject msg = new JSONObject();

            try {
                msg.put("method", "addroom");
                msg.put("roomname", name);

                Rotater.addHandler("addroom", new Rotater.methodHandler(this) {
                    @Override
                    public void handle(JSONObject json) {
                        super.handle(json);

                        Rotater.delHandler("addroom");
                        try {
                            switch (json.getString("status")) {
                                case ("NameTaken"):
                                    Toast.makeText(context, "Name is already taken", Toast.LENGTH_LONG).show();
                                    break;
                                case ("RoomOpened"):
                                    Rotater.delHandler("roominfo");
                                    addroom.setClickable(true);
                                    startActivity(new Intent(context, LobbyActivity.class));
                                    break;
                            }
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });
                Rotater.sendMsg(msg);
            }
            catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
}
