package com.example.digitalvaccineapp.shared;

public class User {
    private String uid;
    private String email;
    private String name;
    private String phone;
    private String age;
    private String address;
    private String gender;

    private String familySyncPhone;

    public User() {}

    public User(String uid, String email, String name, String phone, String familySyncPhone, String age, String address, String gender) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.familySyncPhone = familySyncPhone;
        this.age = age;
        this.address = address;
        this.gender = gender;

    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getFamilySyncPhone() { return familySyncPhone; }
    public void setFamilySyncPhone(String familySyncPhone) { this.familySyncPhone = familySyncPhone; }


}
