package com.example.inv.test.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.inv.test.R;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    EditText msg;
    TextView txt;

    private Socket sock;
    private PrintWriter out;
    private DataInputStream in;

    private static final int SERVERPORT = 12543;
    private static final String SERVER_IP = "92.63.105.60";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        msg = (EditText) findViewById(R.id.editText3);
        txt = (TextView) findViewById(R.id.textView4);

        new Thread(new ClientThread()).start();

        txt.setText(getIntent().getStringExtra("login"));
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SendMessage(View v) {
        //Toast.makeText(this, "Sended", Toast.LENGTH_SHORT).show();
        //txt.setText(txt.getText() + "\n" + msg.getText());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.println(msg.getText());
                    out.flush();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

        msg.setText("");
    }

    class ClientThread implements Runnable{
        @Override
        public void run() {
            try{
                InetAddress servAddr = InetAddress.getByName(SERVER_IP);
                sock = new Socket(servAddr, SERVERPORT);

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), false);
                in = new DataInputStream(sock.getInputStream());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void InterractUp(){

    }

    public void InterractDown(){

    }
}
