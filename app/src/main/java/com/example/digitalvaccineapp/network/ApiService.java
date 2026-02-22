package com.example.digitalvaccineapp.network;

import com.example.digitalvaccineapp.models.ApiResponse;
import com.example.digitalvaccineapp.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("api/vaccinations/add-vaccination")
    Call<ApiResponse<Void>> addVaccination(@Body Vaccination vaccination);

    @GET("api/vaccinations/get-vaccinations")
    Call<ApiResponse<List<Vaccination>>> getVaccinations();

    @PUT("api/vaccinations/update-vaccination/{id}")
    Call<ApiResponse<Void>> updateVaccination(@Path("id") String id, @Body Vaccination vaccination);

    @DELETE("api/vaccinations/delete-vaccination/{id}")
    Call<ApiResponse<Void>> deleteVaccination(@Path("id") String id);
}
