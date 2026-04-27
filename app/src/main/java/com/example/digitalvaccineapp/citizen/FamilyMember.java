package com.example.digitalvaccineapp.citizen;

public class FamilyMember {
    private String id;
    private String name;
    private String age;
    private String gender;
    private String relationship;

    public FamilyMember() {
        // Required empty constructor for Firestore
    }

    public FamilyMember(String id, String name, String age, String gender, String relationship) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.relationship = relationship;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
