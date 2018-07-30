package com.example.inv.test.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.inv.test.R;
import com.example.inv.test.utils.Adapters.LobbyMsgAdapter;
import com.example.inv.test.utils.Elements.Message;
import com.example.inv.test.utils.Network.Rotater;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class LobbyActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etMessage;
    Button btnPlayers, btnSend;
    TextView tvNick;
    Switch swReady;

    ListView lvMain;

    ArrayList<Message> msgs = new ArrayList<Message>();
    LobbyMsgAdapter msgAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_activity);
        addRotaterHandlers();
        fillData();
        msgAdapter = new LobbyMsgAdapter(this, msgs);

        // настраиваем список
        lvMain = (ListView) findViewById(R.id.lvMain);

        lvMain.setAdapter(msgAdapter);

        tvNick = findViewById(R.id.tvNick);
        btnPlayers = findViewById(R.id.btnPlayers);
        btnPlayers.setOnClickListener(this);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        etMessage = findViewById(R.id.etMsg);
        swReady = findViewById(R.id.swReady);
        swReady.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    onSwitch(b);
                    swReady.setClickable(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addRotaterHandlers() {
        Rotater.addHandler("rungame", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("game begins"):
                            delRotaterHandlers();
                            Intent intent = new Intent(context, GameActivity.class);
                            intent.putExtra("role", json.getString("role"));
                            intent.putExtra("sayer", json.getBoolean("sayer"));
                            startActivity(intent);
                            break;
                        default:
                            Toast.makeText(context, "Unbound answer from server", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        Rotater.addHandler("newmsg", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("Ok"):
                            String name = json.getString("name");
                            String msg = json.getString("msg");
                            if (json.getString("msgtype").equals("public")) {
                                msgs.add(new Message(name, msg, false));
                            }
                            else {
                                msgs.add(new Message(name + "(private)", msg, false));
                            }
                            msgAdapter.notifyDataSetChanged();
                            break;
                        default:
                            Toast.makeText(context, "Unbound answer from server", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void delRotaterHandlers() {
        Rotater.delHandler("rungame");
        Rotater.delHandler("newmsg");
    }

    String method = "msgroomchat";
    public void SendMessage(View v) throws JSONException {
        JSONObject msg = new JSONObject();
        method = "msgroomchat";
        if (!tvNick.getText().toString().equals("All")) {
            method = "privatemsgroomchat";
            msg.put("whom", tvNick.getText().toString());
            tvNick.setText("All");
        }
        msg.put("msg", etMessage.getText().toString());
        msg.put("method", method);

        Rotater.addHandler(method, new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("Ok"):

                            break;
                        default:
                            Toast.makeText(context, "Unbound answer from server", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Rotater.delHandler(method);
            }
        });
        Rotater.sendMsg(msg);
    }


    public void onSwitch(boolean b) throws JSONException {
        JSONObject msg = new JSONObject();

        msg.put("method", "selectroomstate");
        msg.put("state", b);

        Rotater.addHandler("selectroomstate", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("Ok"):

                            break;
                        default:
                            Toast.makeText(context, "Unbound answer from server", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Rotater.delHandler("selectroomstate");
            }
        });
        Rotater.sendMsg(msg);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPlayers:
                Intent intent = new Intent(this, PlayerSelectActivity.class);
                startActivityForResult(intent,  1);
            break;
            case R.id.btnSend:
                try {
                    SendMessage(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        String retname = data.getStringExtra("name");
        tvNick.setText(retname);
    }


    private void fillData() {

    }



    @Override
    public void onBackPressed() {
        JSONObject msg = new JSONObject();

        try{
            msg.put("method", "exitroom");
            Rotater.sendMsg(msg);
            super.onBackPressed();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }
}
