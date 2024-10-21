package com.aakash.aadhan.model;

public class Student {
    public String name;
    public String placedCompany;
    public String imageUrl;

    // Default constructor required for Firestore
    public Student() {}

    public Student(String name, String placedCompany) {
        this.name = name;
        this.placedCompany = placedCompany;
    }

    public Student(String name, String placedCompany, String imageUrl) {
        this.name = name;
        this.placedCompany = placedCompany;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlacedCompany() {
        return placedCompany;
    }

    public void setPlacedCompany(String placedCompany) {
        this.placedCompany = placedCompany;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
