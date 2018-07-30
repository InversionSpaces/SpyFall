package com.example.inv.test.utils.Elements;

public class Player {
    public String name;
    public boolean ready;
    //int id;
    //boolean box;
    public boolean clicked;

    public Player (String gname, boolean status, boolean gclicked) {
        name = gname;
        ready = status;
        clicked = gclicked;
    }
}
