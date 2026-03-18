package com.example.digitalvaccineapp.network;

import com.example.digitalvaccineapp.models.ApiResponse;
import com.example.digitalvaccineapp.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("api/vaccinations")
    Call<ApiResponse<Void>> addVaccination(@Body Vaccination vaccination);

    @GET("api/vaccinations")
    Call<ApiResponse<List<Vaccination>>> getVaccinations();

    @GET("api/users/profile")
    Call<ApiResponse<User>> getProfile();

    @PUT("api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Body User user);

    @DELETE("api/vaccinations/{id}")
    Call<ApiResponse<Void>> deleteVaccination(@Path("id") String id);

    @GET("api/vaccinations/certificate-summary")
    Call<ApiResponse<CertificateSummary>> getCertificateSummary();
}
