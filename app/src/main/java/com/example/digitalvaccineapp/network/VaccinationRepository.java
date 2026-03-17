package com.example.digitalvaccineapp.network;

import android.content.Context;
import android.os.AsyncTask;
import com.example.digitalvaccineapp.models.ApiResponse;
import com.example.digitalvaccineapp.models.Vaccination;
import com.example.digitalvaccineapp.models.VaccinationEntity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VaccinationRepository {
    private VaccinationDao vaccinationDao;
    private ApiService apiService;

    public VaccinationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        vaccinationDao = db.vaccinationDao();
        apiService = RetrofitClient.getApiService();
    }

    public interface DataCallback {
        void onDataLoaded(List<Vaccination> vaccinations);
        void onError(String message);
    }

    public void getVaccinations(DataCallback callback) {
        // 1. Fetch from Local DB first for instant response
        new GetLocalTask(callback).execute();

        // 2. Then fetch from API to update
        apiService.getVaccinations().enqueue(new Callback<ApiResponse<List<Vaccination>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Vaccination>>> call, Response<ApiResponse<List<Vaccination>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Vaccination> apiData = response.body().getData();
                    callback.onDataLoaded(apiData);
                    // Update Local DB
                    new UpdateLocalTask(apiData).execute();
                } else {
                    callback.onError("Failed to refresh data from server");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Vaccination>>> call, Throwable t) {
                callback.onError("Offline mode: showing saved records");
            }
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
            if (!vaccinations.isEmpty()) {
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
