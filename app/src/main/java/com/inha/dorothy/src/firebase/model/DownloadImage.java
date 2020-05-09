package com.inha.dorothy.src.firebase.model;

import java.util.HashMap;
import java.util.Map;

public class DownloadImage {

    private String url;
    private String direction;
    private String thumbnail;
    private String fileName;
    private String time;
    private int azimuth;
    private int pitch;
    private int roll;

    public DownloadImage(String url, String direction, String thumbnail,
                         String fileName, String time, int azimuth, int pitch, int roll) {
        this.url = url;
        this.direction = direction;
        this.thumbnail = thumbnail;
        this.fileName = fileName;
        this.time = time;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
    }

    public String getUrl() {
        return url;
    }

    public String getDirection() {
        return direction;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTime() {
        return time;
    }

    public int getAzimuth() {
        return azimuth;
    }

    public int getPitch() {
        return pitch;
    }

    public int getRoll() {
        return roll;
    }

    public DownloadImage(){

    }



}
