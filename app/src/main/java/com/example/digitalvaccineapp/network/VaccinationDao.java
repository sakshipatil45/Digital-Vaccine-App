package com.example.digitalvaccineapp.network;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.digitalvaccineapp.shared.VaccinationEntity;
import java.util.List;

@Dao
public interface VaccinationDao {
    @Query("SELECT * FROM vaccinations ORDER BY dateTaken DESC")
    List<VaccinationEntity> getAllVaccinations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VaccinationEntity> vaccinations);

    @Query("DELETE FROM vaccinations")
    void deleteAll();
}
