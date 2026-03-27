package com.example.digitalvaccineapp.shared;

public class CertificateSummary {
    private String name;
    private String vaccine;
    private int dose;
    private String status;
    private String verifiedOn;
    private String vaccinationId;

    public String getName() { return name; }
    public String getVaccine() { return vaccine; }
    public int getDose() { return dose; }
    public String getStatus() { return status; }
    public String getVerifiedOn() { return verifiedOn; }
    public String getVaccinationId() { return vaccinationId; }
}
