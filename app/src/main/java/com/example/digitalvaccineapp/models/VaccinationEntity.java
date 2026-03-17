package com.example.digitalvaccineapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "vaccinations")
public class VaccinationEntity {
    @PrimaryKey
    @NonNull
    private String id;
    
    private String vaccineName;
    private int doseNumber;
    private String dateTaken;
    private String nextDueDate;
    private String hospitalName;
    private String status;
    private String dependentName;

    public VaccinationEntity(@NonNull String id, String vaccineName, int doseNumber, String dateTaken, 
                             String nextDueDate, String hospitalName, String status, String dependentName) {
        this.id = id;
        this.vaccineName = vaccineName;
        this.doseNumber = doseNumber;
        this.dateTaken = dateTaken;
        this.nextDueDate = nextDueDate;
        this.hospitalName = hospitalName;
        this.status = status;
        this.dependentName = dependentName;
    }

    @NonNull
    public String getId() { return id; }
    public String getVaccineName() { return vaccineName; }
    public int getDoseNumber() { return doseNumber; }
    public String getDateTaken() { return dateTaken; }
    public String getNextDueDate() { return nextDueDate; }
    public String getHospitalName() { return hospitalName; }
    public String getStatus() { return status; }
    public String getDependentName() { return dependentName; }
}
