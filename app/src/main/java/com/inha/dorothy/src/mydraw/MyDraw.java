package com.inha.dorothy.src.mydraw;

public class MyDraw {

    public String id;
    public String room;
    public DrawInfo info;
    public Boolean isCheck;

    public MyDraw(){

    }

    public MyDraw(String id, String room, DrawInfo info){
        this.id = id;
        this.room = room;
        this.info = info;
        this.isCheck = false;
    }

}
