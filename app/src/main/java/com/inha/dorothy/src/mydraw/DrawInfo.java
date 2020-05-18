package com.inha.dorothy.src.mydraw;

public class DrawInfo {

    public String time;
    public String fileName;
    public String direction;
    public Long azimuth;
    public Long pitch;
    public Long roll;
    public String url;

    public DrawInfo(String time, String name, String direction, Long azimuth, Long pitch, Long roll, String url) {
        this.time = time;
        this.fileName = name;
        this.direction = direction;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
        this.url = url;
    }
    public DrawInfo(){

    }

}
