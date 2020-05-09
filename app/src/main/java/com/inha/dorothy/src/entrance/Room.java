package com.inha.dorothy.src.entrance;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Room {

    public int id;
    public RoomInfo info;

    public Room(){

    }

    public Room(int id, RoomInfo info){
        this.id = id;
        this.info = info;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("room_id", id);
        result.put(String.valueOf(id), info);
        return result;
    }

}
