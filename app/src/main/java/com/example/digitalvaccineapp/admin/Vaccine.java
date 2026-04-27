package com.example.digitalvaccineapp.admin;

public class Vaccine {
    private String id;
    private String name;
    private String ageGroup; // Infant, Child, Teen, Adult
    private int recommendedMonths; // Age in months for the vaccine
    private String doseInfo; // e.g., "0.5ml", "2 drops"
    private String description;
    private String benefits;
    private String sideEffects;

    public Vaccine() {
    }

    public Vaccine(String id, String name, String ageGroup, int recommendedMonths, String doseInfo, String description,
            String benefits, String sideEffects) {
        this.id = id;
        this.name = name;
        this.ageGroup = ageGroup;
        this.recommendedMonths = recommendedMonths;
        this.doseInfo = doseInfo;
        this.description = description;
        this.benefits = benefits;
        this.sideEffects = sideEffects;
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

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public int getRecommendedMonths() {
        return recommendedMonths;
    }

    public void setRecommendedMonths(int recommendedMonths) {
        this.recommendedMonths = recommendedMonths;
    }

    public String getDoseInfo() {
        return doseInfo;
    }

    public void setDoseInfo(String doseInfo) {
        this.doseInfo = doseInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }
}
