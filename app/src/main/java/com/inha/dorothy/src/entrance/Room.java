package com.inha.dorothy.src.entrance;

import java.util.HashMap;
import java.util.Map;

public class Room {

    public String title;
    public String password;
    public int person;
    public int id;
    public int doodle;

    public Room(){

    }

    public Room(String title, String password, int person, int id, int doodle){
        this.title = title;
        this.password = password;
        this.person = person;
        this.id = id;
        this.doodle = doodle;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("room_id", id);
        result.put("title", title);
        result.put("password", password);
        result.put("person", person);
        result.put("doodle", doodle);
        return result;
    }

}
