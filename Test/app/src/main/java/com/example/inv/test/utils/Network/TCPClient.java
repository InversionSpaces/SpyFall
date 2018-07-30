package com.example.inv.test.utils.Network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {
    private int PORT;
    private String ADDR;

    private DataInputStream in;
    private PrintWriter out;

    private IOnMsgRecv imsg;

    private boolean running = false;
    private boolean attempting = false;

    public TCPClient(String addr, int port, IOnMsgRecv imsg){
        this.imsg = imsg;

        ADDR = addr;
        PORT = port;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isAttempting() {return attempting;}

    public void run() {
        if(!(running || attempting)) {
            attempting = true;
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        InetAddress saddr = InetAddress.getByName(ADDR);
                        Socket sock = new Socket(saddr, PORT);

                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);
                        in = new DataInputStream(sock.getInputStream());

                        running = true;
                        attempting = false;

                        while (running) {
                            byte[] smsg = new byte[1024];
                            in.read(smsg);
                            imsg.msgRecv(Byte2JSON(smsg));
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    stop();
                }
            }).start();
        }
    }

    public void stop(){
        if(running || attempting) {
            attempting = false;
            running = false;
            try {
                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void SndMsg(final JSONObject msg){
        if(running) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        out.println(msg.toString() + "\r");
                        out.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public JSONObject Byte2JSON(byte[] bytes) throws UnsupportedEncodingException, JSONException {
        int size;
        for(size = 0; bytes[size] != 0; size++);
        return new JSONObject(new String(bytes, 0, size, "UTF-8"));
    }

    public interface IOnMsgRecv {
        public void msgRecv(JSONObject msg);
    }
}

