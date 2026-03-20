package com.example.digitalvaccineapp.network;

import android.content.Context;
import android.os.AsyncTask;
import com.example.digitalvaccineapp.models.Vaccination;
import com.example.digitalvaccineapp.models.VaccinationEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class VaccinationRepository {
    private VaccinationDao vaccinationDao;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public VaccinationRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        vaccinationDao = database.vaccinationDao();
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

        // 1. Fetch from Local DB first for instant response (Offline-First)
        new GetLocalTask(callback).execute();

        // 2. Then fetch from Firestore to update
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("vaccinations")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Vaccination> vaccinations = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Vaccination v = doc.toObject(Vaccination.class);
                    v.setId(doc.getId());
                    vaccinations.add(v);
                }
                callback.onDataLoaded(vaccinations);
                // Update Local Room DB
                new UpdateLocalTask(vaccinations).execute();
            })
            .addOnFailureListener(e -> {
                callback.onError("Cloud sync failed: " + e.getMessage());
            });
    }

    public void addVaccination(Vaccination vaccination, DataCallback callback) {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        // Prepare map or ensure POJO has userId
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

    public void deleteVaccination(String id, DataCallback callback) {
        db.collection("vaccinations").document(id).delete()
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onDataLoaded(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    private class GetLocalTask extends AsyncTask<Void, Void, List<Vaccination>> {
        private DataCallback callback;
        GetLocalTask(DataCallback callback) { this.callback = callback; }

        @Override
        protected List<Vaccination> doInBackground(Void... voids) {
            List<VaccinationEntity> entities = vaccinationDao.getAllVaccinations();
            List<Vaccination> vaccinations = new ArrayList<>();
            for (VaccinationEntity entity : entities) {
                Vaccination v = new Vaccination(entity.getVaccineName(), entity.getDoseNumber(),
                        entity.getDateTaken(), entity.getHospitalName(), entity.getStatus(), entity.getDependentName());
                v.setId(entity.getId());
                v.setNextDueDate(entity.getNextDueDate());
                vaccinations.add(v);
            }
            return vaccinations;
        }

        @Override
        protected void onPostExecute(List<Vaccination> vaccinations) {
            if (vaccinations != null) {
                callback.onDataLoaded(vaccinations);
            }
        }
    }

    private class UpdateLocalTask extends AsyncTask<Void, Void, Void> {
        private List<Vaccination> vaccinations;
        UpdateLocalTask(List<Vaccination> vaccinations) { this.vaccinations = vaccinations; }

        @Override
        protected Void doInBackground(Void... voids) {
            vaccinationDao.deleteAll();
            List<VaccinationEntity> entities = new ArrayList<>();
            for (Vaccination v : vaccinations) {
                entities.add(new VaccinationEntity(v.getId(), v.getVaccineName(), v.getDoseNumber(),
                        v.getDateTaken(), v.getNextDueDate(), v.getHospitalName(), v.getStatus(), v.getDependentName()));
            }
            vaccinationDao.insertAll(entities);
            return null;
        }
    }
}
