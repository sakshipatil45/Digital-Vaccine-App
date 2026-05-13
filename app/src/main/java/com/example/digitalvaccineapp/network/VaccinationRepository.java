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

        db.collection("vaccinations").whereEqualTo("memberId", patientId)
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
        data.put("memberId", beneficiaryId);
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

        db.collection("vaccinations").document(vaxId).update(updates)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void deleteVaccination(String beneficiaryId, String vaxId, DataCallback callback) {
        if (beneficiaryId == null || vaxId == null) return;
        db.collection("vaccinations").document(vaxId).delete()
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    // --- REMINDER SYNC METHODS ---

    public void addReminder(String beneficiaryId, String memberName, String vaccineName, String date, String time, String place, SimpleCallback callback) {
        if (beneficiaryId == null) return;

        Map<String, Object> reminder = new HashMap<>();
        reminder.put("memberId", beneficiaryId);
        reminder.put("memberName", memberName);
        reminder.put("vaccineName", vaccineName);
        reminder.put("reminderDate", date);
        reminder.put("reminderTime", time);
        reminder.put("place", place);
        reminder.put("status", "Pending");
        reminder.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("notifications").add(reminder)
            .addOnSuccessListener(doc -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void scheduleGlobalVaccination(String category, String vaccineName, String date, String time, String place, SimpleCallback callback) {
        Map<String, Object> campaign = new HashMap<>();
        campaign.put("targetCategory", category);
        campaign.put("vaccineName", vaccineName);
        campaign.put("reminderDate", date);
        campaign.put("time", time);
        campaign.put("place", place);
        campaign.put("type", "Campaign");
        campaign.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("campaign_reminders").add(campaign)
            .addOnSuccessListener(doc -> {
                // Mass-inject Pending Vaccination Records and Notifications
                db.collection("family_members").whereEqualTo("category", category).get()
                    .addOnSuccessListener(snapshots -> {
                        if (snapshots.isEmpty()) {
                            callback.onSuccess();
                            return;
                        }

                        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(snapshots.size());
                        for (QueryDocumentSnapshot memberDoc : snapshots) {
                            String memberId = memberDoc.getId();
                            String memberName = memberDoc.getString("name");
                            String ownerId = memberDoc.getString("userId");

                            // 1. Add to Trajectory (vaccinations collection)
                            Map<String, Object> vaxData = new HashMap<>();
                            vaxData.put("memberId", memberId);
                            vaxData.put("userId", ownerId);
                            vaxData.put("vaccineName", vaccineName);
                            vaxData.put("dateTaken", date + " " + time);
                            vaxData.put("hospitalName", place);
                            vaxData.put("status", "Pending");
                            vaxData.put("doseNumber", 1);
                            vaxData.put("dependentName", memberName);
                            vaxData.put("createdAt", com.google.firebase.Timestamp.now());

                            db.collection("vaccinations").add(vaxData).addOnCompleteListener(vaxTask -> {
                                // 2. Add Notification/Reminder
                                Map<String, Object> notif = new HashMap<>();
                                notif.put("memberId", memberId);
                                notif.put("memberName", memberName);
                                notif.put("vaccineName", vaccineName);
                                notif.put("reminderDate", date);
                                notif.put("reminderTime", time);
                                notif.put("place", place);
                                notif.put("status", "Pending");
                                notif.put("message", "Upcoming vaccination scheduled at " + place);
                                notif.put("createdAt", com.google.firebase.Timestamp.now());

                                db.collection("notifications").add(notif).addOnCompleteListener(notifTask -> {
                                    if (count.decrementAndGet() == 0) {
                                        callback.onSuccess();
                                    }
                                });
                            });
                        }
                    })
                    .addOnFailureListener(e -> callback.onSuccess());
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getRemindersForPatient(String beneficiaryId, DataCallback callback) {
        if (beneficiaryId == null) return;

        db.collection("notifications").whereEqualTo("memberId", beneficiaryId)
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
     * Dynamically calculates missing vaccines by comparing Master Schedule vs History.
     */
    public void getDueVaccines(String beneficiaryId, String category, DataCallback callback) {
        if (beneficiaryId == null || category == null) return;

        // Step 1: Fetch Master Schedule for this Category
        db.collection("vaccines_master").get().addOnSuccessListener(masterSnapshots -> {
            List<String> requiredVaccines = new ArrayList<>();
            for (QueryDocumentSnapshot doc : masterSnapshots) {
                String vGroup = doc.getString("ageGroup");
                String vName = doc.getString("name");
                
                boolean match = false;
                if ("Child".equalsIgnoreCase(category)) {
                    if ("Infant".equalsIgnoreCase(vGroup) || "Child".equalsIgnoreCase(vGroup)) match = true;
                } else if ("Adult".equalsIgnoreCase(category)) {
                    if ("Adult".equalsIgnoreCase(vGroup) || "Teen".equalsIgnoreCase(vGroup)) match = true;
                } else if ("Pregnant Woman".equalsIgnoreCase(category)) {
                    if ("Maternal".equalsIgnoreCase(vGroup)) match = true;
                }

                if (match && vName != null) {
                    requiredVaccines.add(vName.toLowerCase());
                }
            }

            // Step 2: Fetch Completed History
            db.collection("vaccinations").whereEqualTo("memberId", beneficiaryId).get()
                .addOnSuccessListener(vaxSnapshots -> {
                    List<String> takenVaccines = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : vaxSnapshots) {
                        String name = doc.getString("vaccineName");
                        String status = doc.getString("status");
                        if (name != null && status != null && status.equalsIgnoreCase("Completed")) {
                            takenVaccines.add(name.toLowerCase());
                        }
                    }

                    // Step 3: Calculate the Gap
                    List<Vaccination> dueList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : masterSnapshots) {
                        String name = doc.getString("name");
                        if (name != null && requiredVaccines.contains(name.toLowerCase()) && !takenVaccines.contains(name.toLowerCase())) {
                            Vaccination v = new Vaccination();
                            v.setVaccineName(name);
                            v.setStatus("Required");
                            v.setDoseNumber(1); // Default
                            dueList.add(v);
                        }
                    }
                    callback.onDataLoaded(dueList);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * LEGACY - Keeping signature for compatibility
     */
    public void getVaccinations(DataCallback callback) {
        callback.onError("Global fetch deprecated. Use getVaccinationsForPatient().");
    }
}
