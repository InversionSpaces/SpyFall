package com.example.inv.test.utils.Network;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

public class Rotater {
    private static Thread thread = new ConnectionThread();
    private static TCPClient.IOnMsgRecv reciever = new TCPClient.IOnMsgRecv() {
        @Override
        public void msgRecv(JSONObject msg) {
            try {
                methods.get(msg.getString("method")).handle(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private static TCPClient client;
    private static HashMap<String, methodHandler> methods = new HashMap<>();

    private static boolean connected = false;

    public static boolean isConnected(){
        return connected;
    }

    public static void run(final String addr, final int port) {
        if(thread.isAlive()){
            thread.interrupt();
        }

        client = new TCPClient(addr, port, reciever);

        thread.start();
    }

    public static void run(){
        if(!thread.isAlive()){
            thread.start();
        }
    }

    public static void stop() {
        if(thread.isAlive()){
            thread.interrupt();
        }
    }

    public static void sendMsg(JSONObject msg) {
        if(connected)
            client.SndMsg(msg);
    }

    public static void addHandler(String method, methodHandler handler){
        methods.put(method, handler);
    }

    public static void delHandler(String method){
        methods.remove(method);
    }

    public abstract static class methodHandler {
        protected Context context;

        public methodHandler(Context context){
            this.context = context;
        }

        public void handle(final JSONObject json){}
    }

    private static class ConnectionThread extends Thread{
        @Override
        public void run() {
            super.run();

            while (true) {
                client.run();

                while(client.isAttempting()){}

                while (client.isRunning()) {
                    connected = true;
                }

                client.stop();
                connected = false;

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();

            client.stop();
            connected = false;
        }
    }
}
