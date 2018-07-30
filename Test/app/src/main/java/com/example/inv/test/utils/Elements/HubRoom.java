package com.example.inv.test.utils.Elements;

public class HubRoom {
    private String name;
    private int players;

    public HubRoom(String name, int players){
        this.name = name;
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }
}
