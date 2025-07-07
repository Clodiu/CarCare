package com.example.carcare;

public class Car {
    String manufacturer;
    String model;
    String registerPlate;
    String lastServiced;

    public Car(){
        this.manufacturer = "Default";
        this.model = "No model";
        this.registerPlate = "AA00BBB";
        this.lastServiced = "Never";
    }

    public Car(String manufacturer,String model, String registerPlate,String lastServiced){
        this.manufacturer = manufacturer;
        this.model = model;
        this.registerPlate = registerPlate;
        this.lastServiced = lastServiced;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getRegisterPlate() {
        return registerPlate;
    }

    public String getLastServiced() {
        return lastServiced;
    }
}
