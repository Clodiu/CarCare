package com.example.carcare;

public class Note {
    String title;
    String description;
    String price;
    String date;

    public Note(){
        this.title = "Nimic";
        this.description = "No description";
        this.price = "600Lei";
        this.date = "Last serviced: 01-01-2002";
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }
}
