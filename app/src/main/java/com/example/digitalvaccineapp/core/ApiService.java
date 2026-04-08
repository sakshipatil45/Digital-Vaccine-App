package com.example.digitalvaccineapp.core;
import com.example.digitalvaccineapp.shared.User;

import com.example.digitalvaccineapp.shared.Vaccination;

import com.example.digitalvaccineapp.core.ApiResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("api/vaccinations/add-vaccination")
    Call<ApiResponse<Void>> addVaccination(@Body Vaccination vaccination);

    @GET("api/vaccinations/get-vaccinations")
    Call<ApiResponse<List<Vaccination>>> getVaccinations();

    @GET("api/users/profile")
    Call<ApiResponse<User>> getProfile();

    @PUT("api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Body User user);

    @DELETE("api/vaccinations/delete-vaccination/{id}")
    Call<ApiResponse<Void>> deleteVaccination(@Path("id") String id);



}
