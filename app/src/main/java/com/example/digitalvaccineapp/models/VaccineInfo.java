package com.example.digitalvaccineapp.models;

public class VaccineInfo {
    private String name;
    private String description;
    private String dosage;
    private String ageGroup;
    private int iconResId;

    public VaccineInfo(String name, String description, String dosage, String ageGroup, int iconResId) {
        this.name = name;
        this.description = description;
        this.dosage = dosage;
        this.ageGroup = ageGroup;
        this.iconResId = iconResId;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDosage() { return dosage; }
    public String getAgeGroup() { return ageGroup; }
    public int getIconResId() { return iconResId; }
}
