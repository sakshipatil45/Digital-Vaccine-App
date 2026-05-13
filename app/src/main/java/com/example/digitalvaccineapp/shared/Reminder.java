package com.example.digitalvaccineapp.shared;

public class Reminder {
    private String id;
    private String vaccineName;
    private String reminderDate;
    private String reminderTime;
    private String place;
    private String status;
    private String targetCategory;
    private String memberName;
    private String memberId;

    public Reminder() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }

    public String getReminderDate() { return reminderDate; }
    public void setReminderDate(String reminderDate) { this.reminderDate = reminderDate; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTargetCategory() { return targetCategory; }
    public void setTargetCategory(String targetCategory) { this.targetCategory = targetCategory; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
}
