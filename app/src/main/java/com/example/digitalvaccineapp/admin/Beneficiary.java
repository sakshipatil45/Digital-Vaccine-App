package com.example.digitalvaccineapp.admin;

public class Beneficiary {
    private String id;
    private String name;
    private String age;
    private String gender;

    private String mobileNumber;
    private String category; // Child, Pregnant Woman, Adult
    private String adminId;

    public Beneficiary() {}

    public Beneficiary(String id, String name, String age, String gender, String mobileNumber, String category, String adminId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;

        this.mobileNumber = mobileNumber;
        this.category = category;
        this.adminId = adminId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }



    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
}
