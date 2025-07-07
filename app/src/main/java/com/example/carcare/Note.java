package com.example.carcare;

public class Note {
    String title;
    String description;
    String price;

    public Note(){
        this.title = "Nimic";
        this.description = "No description";
        this.price = "600Lei";
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
}
