package com.example.digitalvaccineapp.admin;

public class Campaign {
    private String id;
    private String targetCategory;
    private String vaccineName;
    private String reminderDate;
    private String time;
    private String place;

    public Campaign() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTargetCategory() { return targetCategory; }
    public void setTargetCategory(String targetCategory) { this.targetCategory = targetCategory; }

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }

    public String getReminderDate() { return reminderDate; }
    public void setReminderDate(String reminderDate) { this.reminderDate = reminderDate; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
}
