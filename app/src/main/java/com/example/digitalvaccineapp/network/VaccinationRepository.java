package com.example.digitalvaccineapp.network;

import com.example.digitalvaccineapp.shared.Vaccination;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Repository to handle vaccination data storage and retrieval from Firestore.
 * Updated for Smooth Sync: Vaccinations are now stored under beneficiaries/{patientId}/vaccinations
 */
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

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Fetch vaccinations for a specific beneficiary/patient.
     */
    public void getVaccinationsForPatient(String patientId, DataCallback callback) {
        if (mAuth.getCurrentUser() == null || patientId == null) {
            callback.onError("User not logged in or invalid patient ID");
            return;
        }

        db.collection("beneficiaries").document(patientId).collection("vaccinations")
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

    public void addVaccination(String beneficiaryId, Vaccination vaccination, DataCallback callback) {
        if (mAuth.getCurrentUser() == null || beneficiaryId == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("vaccineName", vaccination.getVaccineName());
        data.put("doseNumber", vaccination.getDoseNumber());
        data.put("dateTaken", vaccination.getDateTaken());
        data.put("hospitalName", vaccination.getHospitalName());
        data.put("status", vaccination.getStatus());
        data.put("dependentName", vaccination.getDependentName());
        data.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("beneficiaries").document(beneficiaryId).collection("vaccinations").add(data)
            .addOnSuccessListener(documentReference -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void updateVaccination(String beneficiaryId, String vaxId, Vaccination vaccination, DataCallback callback) {
        if (beneficiaryId == null || vaxId == null) return;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("vaccineName", vaccination.getVaccineName());
        updates.put("doseNumber", vaccination.getDoseNumber());
        updates.put("dateTaken", vaccination.getDateTaken());
        updates.put("hospitalName", vaccination.getHospitalName());
        updates.put("status", vaccination.getStatus());
        updates.put("dependentName", vaccination.getDependentName());
        updates.put("updatedAt", com.google.firebase.Timestamp.now());

        db.collection("beneficiaries").document(beneficiaryId).collection("vaccinations").document(vaxId).update(updates)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void deleteVaccination(String beneficiaryId, String vaxId, DataCallback callback) {
        if (beneficiaryId == null || vaxId == null) return;
        db.collection("beneficiaries").document(beneficiaryId).collection("vaccinations").document(vaxId).delete()
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    // --- REMINDER SYNC METHODS ---

    public void addReminder(String beneficiaryId, String vaccineName, String date, SimpleCallback callback) {
        if (beneficiaryId == null) return;

        Map<String, Object> reminder = new HashMap<>();
        reminder.put("vaccineName", vaccineName);
        reminder.put("reminderDate", date);
        reminder.put("status", "Pending");
        reminder.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("beneficiaries").document(beneficiaryId).collection("reminders").add(reminder)
            .addOnSuccessListener(doc -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getRemindersForPatient(String beneficiaryId, DataCallback callback) {
        if (beneficiaryId == null) return;

        db.collection("beneficiaries").document(beneficiaryId).collection("reminders")
            .get()
            .addOnSuccessListener(snapshots -> {
                // We're reusing Vaccination model or mapping to it for the callback
                // In a real app, you'd have a Reminder model.
                // For now, let's just use it to notify data loaded.
                callback.onDataLoaded(null); 
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * LEGACY - Keeping signature for compatibility
     */
    public void getVaccinations(DataCallback callback) {
        callback.onError("Global fetch deprecated. Use getVaccinationsForPatient().");
    }
}
