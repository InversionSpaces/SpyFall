package com.example.inv.test.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.inv.test.R;
import com.example.inv.test.utils.Adapters.LobbyMsgAdapter;
import com.example.inv.test.utils.Elements.Message;
import com.example.inv.test.utils.Network.Rotater;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etMessage;
    Button btnPlayers, btnSend, btnLocations;
    TextView tvNick, tvRole;

    ListView lvMain;

    ArrayList<Message> msgs = new ArrayList<Message>();
    LobbyMsgAdapter msgAdapter;

    String role;
    boolean iSpeak = false;
    String msgType = "common";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Bundle recieveBundle = this.getIntent().getBundleExtra();
        Intent recieveIntent = this.getIntent();
        role = recieveIntent.getStringExtra("role");

        iSpeak = recieveIntent.getBooleanExtra("sayer", false);

        if (iSpeak) msgType = "question";

        setContentView(com.example.inv.test.R.layout.game_activity);
        addRotaterHandlers();
        fillData();
        msgAdapter = new LobbyMsgAdapter(this, msgs);

        // настраиваем список
        lvMain = (ListView) findViewById(R.id.lvMain);

        lvMain.setAdapter(msgAdapter);


        tvRole = (TextView) findViewById(R.id.tvRole);
        tvRole.setText("Your role is" + role);

        tvNick = (TextView) findViewById(R.id.tvNick);

        btnPlayers = (Button) findViewById(com.example.inv.test.R.id.btnPlayers);
        btnPlayers.setOnClickListener(this);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        btnLocations = (Button)findViewById(R.id.btnLocations);
        btnLocations.setOnClickListener(this);

        etMessage = (EditText) findViewById(R.id.etMsg);
    }


    private void addRotaterHandlers() {
        Rotater.addHandler("spyguess", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("SpyWon"):
                            delRotaterHandlers();
                            Intent intent = new Intent(context, LobbyActivity.class);
                            if (!role.equals("spy")) {
                                Toast.makeText(context, "You Lose! Congrats!", Toast.LENGTH_LONG).show();
                            }
                            startActivity(intent);
                            break;
                        case("InnocentWon"):
                            delRotaterHandlers();
                            Intent intent1 = new Intent(context, LobbyActivity.class);
                            if (!role.equals("spy")) {
                                Toast.makeText(context, "You Won! Congrats!", Toast.LENGTH_LONG).show();
                            }
                            startActivity(intent1);
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


        Rotater.addHandler("newmsggamechat", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("Ok"):
                            String name = json.getString("name");
                            String msg = json.getString("msg");
                            if (json.getString("msgtype").equals("answer")) {
                                msgs.add(new Message(name + "answer", msg, false));
                            }
                            else if (json.getString("msgtype").equals("answer")) {
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

        Rotater.addHandler("gameinfo", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("YourAnswer"):
                            iSpeak = true;
                            msgType = "answer";
                            btnPlayers.setClickable(false);
                            tvNick.setText("All");
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
        Rotater.delHandler("endgame");
        Rotater.delHandler("newmsggamechat");
        Rotater.delHandler("roominfo");
    }

    //String method = "msgroomchat";
    public void SendMessage(View v) throws JSONException {
        JSONObject msg = new JSONObject();

        try {
            msg.put("method", "msggamechat");
            msg.put("msg", etMessage.getText().toString());
            msg.put("msgtype", msgType);
            msg.put("whom", tvNick.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Rotater.addHandler("msggamechat", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("Ok"):
                            if (msgType.equals("question")) {
                                msgType = "common";
                                iSpeak = false;
                            }
                            else if (msgType.equals("answer")) {
                                msgType = "question";
                                btnPlayers.setClickable(true);
                                btnSend.setClickable(false);
                            }
                            break;
                        default:
                            Toast.makeText(context, "Unknown Player", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Rotater.delHandler("msggamechat");
            }
        });
        Rotater.sendMsg(msg);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        String exittype = data.getStringExtra("exit");
        if (exittype.equals("noguess")) {
            return;
        }
        if (exittype.equals("back")) {
            return;
        }
        if (exittype.equals("guess")) {
            makeGuess(requestCode, resultCode, data);
        }
        if (exittype.equals("playersel")) {
            setMessagePrivate(requestCode, resultCode, data);
        }
    }


    private void fillData() {

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnPlayers:
                Intent intent = new Intent();
                boolean exitable = true;
                if (msgType.equals("question")) {
                    exitable = false;
                }
                intent.putExtra("exitable", exitable);
                startActivityForResult(intent, 1);
                break;
            case R.id.btnSend:
                try {
                    SendMessage(view); //TODO check again
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnLocations:
                Intent intent1 = new Intent();
                intent1.putExtra("role", role);
                startActivityForResult(intent1, 1);
                break;
        }
    }


    void setMessagePrivate (int requestCode, int resultCode, Intent data) {
        String retName = data.getStringExtra("name");
        tvNick.setText(retName);
        if (msgType.equals("question")) {
            btnSend.setClickable(true);
        }
        if (msgType.equals("answer")) {
            tvNick.setText("All");
        }
    }



    void makeGuess(int requestCode, int resultCode, Intent data) {
        String retLocation = data.getStringExtra("location");
        int retId = data.getIntExtra("id", 0);

        JSONObject msg = new JSONObject();

        try {
            msg.put("method", "spyguess");
            msg.put("location", retId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Rotater.addHandler("spyguess", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("WrongRole"):
                            Toast.makeText(context,"ACHTUNG! WRONG ROLE", Toast.LENGTH_LONG).show();
                            break;
                        case("SpyWon"):
                            Toast.makeText(context, "You Won! Congrats!", Toast.LENGTH_LONG).show();
                            break;
                        case("InnocentWon"):
                            Toast.makeText(context, "You Lose! Congrats!", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Rotater.delHandler("spyguess");
            }
        });
        Rotater.sendMsg(msg);
    }
}
