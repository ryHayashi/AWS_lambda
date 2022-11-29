package com.example.base64_practice2;

import android.graphics.Bitmap;
import android.provider.ContactsContract;

public class RequestClass {
    String op;
    DataClass data;

    public String getOp() {
        return op;
    }

    public void setop(String op) {
        this.op = op;
    }

    public RequestClass(String op, String id, String title, Double latitude, Double longitude, String image) {
        this.op = op;
        data = new DataClass(id, title, latitude, longitude, image);
    }

    public RequestClass(String op, String title, Double latitude, Double longitude, String image) {
        this.op = op;
        data = new DataClass(title, latitude, longitude, image);
    }

    public RequestClass(String op, Double latitude, Double longitude) {
        this.op = op;
        data = new DataClass(latitude,longitude);
    }

    public RequestClass(String op, String id) {
        this.op = op;
        data = new DataClass(id);
    }

    public RequestClass(String op) {
        this.op = op;
        data = new DataClass();
    }
    public RequestClass() {
    }
}


