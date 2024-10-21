package com.aakash.aadhan;

public class Student {
    public String id;
    public String name;
    public String rollNumber;
    public String companyName;
    public String technology;
    public String imageUrl;

    // Default constructor required for Firestore
    public Student() {}

    public Student(String id, String name, String rollNumber, String companyName, String technology) {
        this.id = id;
        this.name = name;
        this.rollNumber = rollNumber;
        this.companyName = companyName;
        this.technology = technology;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getRollNumber() { return rollNumber; }
    public String getCompanyName() { return companyName; }
    public String getTechnology() { return technology; }
    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setTechnology(String technology) { this.technology = technology; }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
