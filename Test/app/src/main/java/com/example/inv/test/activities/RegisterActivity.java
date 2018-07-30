package com.example.inv.test.activities;

import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {
    Button regbtn;
    EditText login, pass, chkpass;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        regbtn = findViewById(R.id.RegisterButton);
        login = findViewById(R.id.LoginText);
        pass = findViewById(R.id.PassText);
        chkpass = findViewById(R.id.LoginText);
    }

    public void Register(View v) {
        String password = pass.getText().toString();
        String checkpassword = chkpass.getText().toString();

        if(!password.equals(checkpassword)){
            Toast.makeText(this, "Passwords mismatch", Toast.LENGTH_LONG);
        }
        else{
            JSONObject msg = new JSONObject();

            try {
                msg.put("method", "register");
                msg.put("login", login.getText().toString());
                msg.put("password", password);
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            Rotater.addHandler("register", new Rotater.methodHandler(this) {
                @Override
                public void handle(JSONObject json) {
                    super.handle(json);

                    try {
                        switch (json.getString("status")) {
                            case ("Registered"):
                                startActivity(new Intent(context, HubActivity.class));
                                break;
                            case ("NameTaken"):
                                Toast.makeText(context, "Name already taken", Toast.LENGTH_LONG);
                                break;
                            default:
                                Toast.makeText(context, "Unbound answer from server", Toast.LENGTH_LONG);
                                break;
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    Rotater.delHandler("register");
                }
            });
            Rotater.sendMsg(msg);

        }
    }
}
