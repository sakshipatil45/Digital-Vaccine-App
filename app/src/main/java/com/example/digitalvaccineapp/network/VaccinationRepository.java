package com.example.digitalvaccineapp.network;

import com.example.digitalvaccineapp.shared.Vaccination;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class VaccinationRepository {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public VaccinationRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public interface DataCallback {
        void onDataLoaded(List<Vaccination> vaccinations);
        void onError(String message);
    }

    public void getVaccinations(DataCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onError("User not logged in");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("vaccinations")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Vaccination> vaccinations = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Vaccination v = doc.toObject(Vaccination.class);
                    v.setId(doc.getId());
                    vaccinations.add(v);
                }
                callback.onDataLoaded(vaccinations);
            })
            .addOnFailureListener(e -> {
                callback.onError("Failed to fetch vaccinations: " + e.getMessage());
            });
    }

    public void addVaccination(Vaccination vaccination, DataCallback callback) {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", userId);
        data.put("vaccineName", vaccination.getVaccineName());
        data.put("doseNumber", vaccination.getDoseNumber());
        data.put("dateTaken", vaccination.getDateTaken());
        data.put("hospitalName", vaccination.getHospitalName());
        data.put("status", vaccination.getStatus());
        data.put("dependentName", vaccination.getDependentName());
        data.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("vaccinations").add(data)
            .addOnSuccessListener(documentReference -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void updateVaccination(String id, Vaccination vaccination, DataCallback callback) {
        if (id == null) return;
        
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("vaccineName", vaccination.getVaccineName());
        updates.put("doseNumber", vaccination.getDoseNumber());
        updates.put("dateTaken", vaccination.getDateTaken());
        updates.put("hospitalName", vaccination.getHospitalName());
        updates.put("status", vaccination.getStatus());
        updates.put("dependentName", vaccination.getDependentName());
        updates.put("updatedAt", com.google.firebase.Timestamp.now());

        db.collection("vaccinations").document(id).update(updates)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void deleteVaccination(String id, DataCallback callback) {
        if (id == null) return;
        db.collection("vaccinations").document(id).delete()
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }
}
