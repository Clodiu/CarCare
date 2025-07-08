package com.example.carcare;

public class Note {
    String title;
    String description;
    int km;
    String date;

    public Note(){
        this.title = "Nimic";
        this.description = "No description";
        this.km = 128000;
        this.date = "Last serviced: 01-01-2002";
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getKm() {
        return km;
    }

    public String getDate() {
        return date;
    }
}
