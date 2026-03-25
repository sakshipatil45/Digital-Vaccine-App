package com.example.digitalvaccineapp.models;

public class Beneficiary {
    private String id;
    private String name;
    private String age;
    private String gender;
    private String village;
    private String mobileNumber;
    private String category; // Child, Pregnant Woman, Adult
    private String ashaId;

    public Beneficiary() {}

    public Beneficiary(String id, String name, String age, String gender, String village, String mobileNumber, String category, String ashaId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.village = village;
        this.mobileNumber = mobileNumber;
        this.category = category;
        this.ashaId = ashaId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAshaId() { return ashaId; }
    public void setAshaId(String ashaId) { this.ashaId = ashaId; }
}
