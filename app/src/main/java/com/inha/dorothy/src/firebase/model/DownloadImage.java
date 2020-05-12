package com.inha.dorothy.src.firebase.model;

public class DownloadImage {

    private String createdTime;
    private String url;
    private String direction;
    private String thumbnail;
    private String fileName;
    private String time;
    private int azimuth;
    private int pitch;
    private int roll;


    public DownloadImage() {

    }
    public DownloadImage(String createdTime, String url, String direction, String thumbnail,
                         String fileName, int azimuth, int pitch, int roll) {
        this.createdTime = createdTime;
        this.url = url;
        this.direction = direction;
        this.thumbnail = thumbnail;
        this.fileName = fileName;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
    }

    public String getCreatedTime(){ return createdTime; }
    public String getUrl(){ return url; }
    public String getDirection(){ return direction; }
    public String getThumbnail(){ return thumbnail; }
    public String getFileName(){ return fileName; }
    public int getAzimuth(){ return azimuth; }
    public int getPitch(){ return pitch; }
    public int getRoll(){ return roll; }



}
