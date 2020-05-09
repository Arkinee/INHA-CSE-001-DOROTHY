package com.inha.dorothy.src.entrance;

import java.util.HashMap;
import java.util.Map;

public class RoomInfo {

    public String title;
    public String password;
    public Integer person;
    public Integer doodle;

    public RoomInfo(){

    }

    public RoomInfo(String title, String password, Integer  person, Integer doodle){
        this.title = title;
        this.password = password;
        this.person = person;
        this.doodle = doodle;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("password", password);
        result.put("person", person);
        result.put("doodle", doodle);
        return result;
    }

}
