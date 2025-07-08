package com.example.carcare;

public class Car {
    int carId;
    String manufacturer;
    String model;
    String registerPlate;
    String lastServiced;
    int creatorId;

    public Car(){
        this.carId = 0;
        this.manufacturer = "Default";
        this.model = "No model";
        this.registerPlate = "AA00BBB";
        this.lastServiced = "Never";
        this.creatorId = 0;
    }

    public Car(int carId,String manufacturer,String model, String registerPlate,String lastServiced, int creatorId){
        this.carId = carId;
        this.manufacturer = manufacturer;
        this.model = model;
        this.registerPlate = registerPlate;
        this.lastServiced = lastServiced;
        this.creatorId = creatorId;
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
