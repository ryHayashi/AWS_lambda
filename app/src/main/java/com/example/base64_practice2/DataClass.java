package com.example.base64_practice2;

public class DataClass {
    String id = "";
    String title = "";
    Double latitude = 0.0;
    Double longitude = 0.0;
    String image = "";


    public String getId() {
        return id;
    }
    public String getTitle() { return title; }
    public Double getLatitude() {
        return latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public String getImage() {
        return image;
    }


    public void setId(String greetings) {
        this.id = id;
    }
    public void setTitle(String greetings) {
        this.title = title;
    }
    public void setLatitude(String greetings) {
        this.latitude = latitude;
    }
    public void setLongitude(String greetings) {
        this.longitude = longitude;
    }
    public void setImage(String greetings) {
        this.image = image;
    }

    public DataClass(String id , String title, Double latitude, Double longitude, String image) {
        this.id = id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
    }

    public DataClass( Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public DataClass( String id) {
        this.id = id;
    }

    public DataClass() {
    }
}
