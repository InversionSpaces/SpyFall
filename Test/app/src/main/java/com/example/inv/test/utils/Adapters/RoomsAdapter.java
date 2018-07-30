package com.example.inv.test.utils.Adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inv.test.R;
import com.example.inv.test.activities.LobbyActivity;
import com.example.inv.test.utils.Elements.HubRoom;
import com.example.inv.test.utils.Network.Rotater;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.ElementHolder> {
    private ArrayList<HubRoom> rooms;

    public RoomsAdapter(ArrayList<HubRoom> rooms){
        this.rooms = rooms;
    }

    @NonNull
    @Override
    public ElementHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View iv = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hubroom_element, parent, false);
        return new ElementHolder(iv, i);
    }

    @Override
    public void onBindViewHolder(@NonNull ElementHolder holder, int i) {
        HubRoom room = rooms.get(i);
        holder.name.setText("Room: "  + room.getName());
        holder.players.setText("Players: " + room.getPlayers());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class ElementHolder extends RecyclerView.ViewHolder{
        public TextView name, players;
        public HubRoom element;

        public ElementHolder(final View iv, int i){
            super(iv);

            name = (TextView) iv.findViewById(R.id.Name);
            players = (TextView) iv.findViewById(R.id.Players);

            element = rooms.get(i);

            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JSONObject msg = new JSONObject();

                    try{
                        msg.put("method", "enterroom");
                        msg.put("roomname", element.getName());

                        Rotater.addHandler("enterroom", new Rotater.methodHandler(iv.getContext()) {
                            @Override
                            public void handle(JSONObject json) {
                                super.handle(json);

                                Rotater.delHandler("enterroom");
                                try{
                                    switch (json.getString("status")){
                                        case("EnteredRoom"):
                                            context.startActivity(new Intent(context, LobbyActivity.class));
                                            break;
                                        case("FullRoom"):
                                            Toast.makeText(context, "Room is full", Toast.LENGTH_LONG).show();
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
            });
        }


    }
}
