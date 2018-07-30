package com.example.inv.test.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.inv.test.R;
import com.example.inv.test.utils.Network.Rotater;

public class EnterActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_activity);

        Log.d("ENTER", "I IM LOGGING NOW");

        Rotater.run("92.63.105.60", 12133);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Rotater.stop();
    }

    public void Login(View v){
        StartIfConnected(LoginActivity.class);
    }

    public void Register(View v){
        StartIfConnected(RegisterActivity.class);
    }

    private void StartIfConnected(Class<?> cls){
        if(Rotater.isConnected()){
            Intent intent = new Intent(this, cls);
            startActivity(intent);
        }
        else{
            Toast.makeText(this,"No connection to server", Toast.LENGTH_LONG).show();
        }
    }

    public void Or(View v){
        Toast toast = Toast.makeText(getApplicationContext(),"Live or die?", Toast.LENGTH_SHORT);
        toast.show();
    }
}
