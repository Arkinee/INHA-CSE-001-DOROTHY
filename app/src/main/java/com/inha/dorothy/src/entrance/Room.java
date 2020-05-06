package com.inha.dorothy.src.entrance;

public class Room {

    private String title;
    private String password;
    private String person;

    public Room(String title, String password){
        this.title = title;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }
}
