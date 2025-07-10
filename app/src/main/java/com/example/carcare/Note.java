package com.example.carcare;

public class Note {
    int note_ID;
    String title;
    String description;
    int kilometers;
    String date;
    int creator_ID;
    int car_ID;

    public Note(){
        this.title = "Nimic";
        this.description = "No description";
        this.kilometers = 128000;
        this.date = "01-01-2002";
    }

    public Note(int note_ID, String title, String description, int kilometers, String date, int creator_ID, int car_ID){
        this.note_ID = note_ID;
        this.title = title;
        this.description = description;
        this.kilometers = kilometers;
        this.date = date;
        this.creator_ID = creator_ID;
        this.car_ID = car_ID;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getKm() {
        return kilometers;
    }

    public String getDate() {
        return date;
    }

    public int getNote_ID() {
        return note_ID;
    }

    public int getKilometers() {
        return kilometers;
    }

    public int getCreator_ID() {
        return creator_ID;
    }

    public int getCar_ID() {
        return car_ID;
    }
}
