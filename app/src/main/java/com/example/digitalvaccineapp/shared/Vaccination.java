package com.example.digitalvaccineapp.shared;

import com.google.gson.annotations.SerializedName;
import com.google.firebase.firestore.Exclude;

public class Vaccination {
    @SerializedName("vaccinationId")
    private String id;

    private String vaccineName;
    private int doseNumber;
    private String dateTaken;
    private String nextDueDate;
    private String hospitalName;
    private String status;
    private String dependentName;
    private Object createdAt;

    // Smooth Sync Field: Stores the ID of the beneficiary document this record
    // belongs to
    @Exclude
    private String patientId;

    // Default constructor for Firebase/GSON
    public Vaccination() {
    }

    public Vaccination(String vaccineName, int doseNumber, String dateTaken, String hospitalName, String status,
            String dependentName) {
        this.vaccineName = vaccineName;
        this.doseNumber = doseNumber;
        this.dateTaken = dateTaken;
        this.hospitalName = hospitalName;
        this.status = status;
        this.dependentName = dependentName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public int getDoseNumber() {
        return doseNumber;
    }

    public void setDoseNumber(int doseNumber) {
        this.doseNumber = doseNumber;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(String nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDependentName() {
        return dependentName;
    }

    public void setDependentName(String dependentName) {
        this.dependentName = dependentName;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public String getPatientId() {
        return patientId;
    }

    @Exclude
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
