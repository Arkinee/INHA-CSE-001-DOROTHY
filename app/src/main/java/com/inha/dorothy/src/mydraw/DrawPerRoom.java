package com.inha.dorothy.src.mydraw;

import java.util.ArrayList;

public class DrawPerRoom {

    public String room_id;
    public String title;
    public ArrayList<String> ids;

    public DrawPerRoom(String room_id, String title, ArrayList<String> ids){
        this.room_id = room_id;
        this.ids = ids;
        this.title = title;
    }

}
