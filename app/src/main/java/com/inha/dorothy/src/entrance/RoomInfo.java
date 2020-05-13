package com.inha.dorothy.src.entrance;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class RoomInfo {

    public String title;
    public String password;
    public Long person;
    public Long doodles;

    public RoomInfo(){

    }

    public RoomInfo(String title, String password, Long  person, Long doodle){
        this.title = title;
        this.password = password;
        this.person = person;
        this.doodles = doodles;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("password", password);
        result.put("person", person);
        result.put("doodle", doodles);
        return result;
    }

}
