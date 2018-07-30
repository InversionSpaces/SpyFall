package com.example.inv.test.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.inv.test.R;
import com.example.inv.test.utils.Network.Rotater;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    EditText login, pass;
    Button button;

    private SharedPreferences prefs;

    private void SaveStr(String key, String val){
        prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, val);
        edit.commit();
    }

    private String GetStr(String key, String dflt){
        prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString(key, dflt);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        login = findViewById(R.id.LoginText);
        pass = findViewById(R.id.PassText);
        button = findViewById(R.id.LoginButton);
    }

    public void Login(View v){
        JSONObject msg = new JSONObject();

        try {
            msg.put("method", "login");
            msg.put("name", login.getText().toString());
            msg.put("password", pass.getText().toString());
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        Rotater.addHandler("login", new Rotater.methodHandler(this) {
            @Override
            public void handle(JSONObject json){
                super.handle(json);

                try {
                    switch(json.getString("status")){
                        case("LoggedIn"):
                            startActivity(new Intent(context, HubActivity.class));
                            break;
                        case("WrongPass"):
                            Toast.makeText(context,"Wrong password", Toast.LENGTH_LONG).show();
                            break;
                        case("WrongLogin"):
                            break;
                        default:
                            Toast.makeText(context, "Unbound answer from server", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Rotater.delHandler("login");
            }
        });
        Rotater.sendMsg(msg);
    }
}
