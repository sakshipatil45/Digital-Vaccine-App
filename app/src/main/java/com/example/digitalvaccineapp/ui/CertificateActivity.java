package com.example.digitalvaccineapp.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.example.digitalvaccineapp.network.RetrofitClient;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class CertificateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        ImageView ivQr = findViewById(R.id.ivQrCode);
        TextView tvName = findViewById(R.id.tvCertName);
        TextView tvDetails = findViewById(R.id.tvCertDetails);

        com.example.digitalvaccineapp.network.RetrofitClient.getApiService().getCertificateSummary().enqueue(new retrofit2.Callback<com.example.digitalvaccineapp.models.ApiResponse<com.example.digitalvaccineapp.models.CertificateSummary>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.digitalvaccineapp.models.ApiResponse<com.example.digitalvaccineapp.models.CertificateSummary>> call, retrofit2.Response<com.example.digitalvaccineapp.models.ApiResponse<com.example.digitalvaccineapp.models.CertificateSummary>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    com.example.digitalvaccineapp.models.CertificateSummary summary = response.body().getData();
                    
                    tvName.setText("Name: " + summary.getName());
                    String details = "Vaccine: " + summary.getVaccine() + "\nDose: " + summary.getDose() + "\nStatus: " + summary.getStatus() + "\nVerified on: " + summary.getVerifiedOn();
                    tvDetails.setText(details);

                    generateQr(summary.getName() + " | " + summary.getStatus() + " | " + summary.getVaccine(), ivQr);
                    
                    if (summary.getVaccinationId() != null) {
                        findViewById(R.id.btnDownloadCert).setOnClickListener(v -> {
                            String url = "http://10.95.27.238:5000/api/vaccinations/download-certificate/" + summary.getVaccinationId();
                            android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                            startActivity(browserIntent);
                            Toast.makeText(CertificateActivity.this, "Opening certificate in browser...", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Toast.makeText(CertificateActivity.this, "No vaccination records found for certificate", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.digitalvaccineapp.models.ApiResponse<com.example.digitalvaccineapp.models.CertificateSummary>> call, Throwable t) {
                Toast.makeText(CertificateActivity.this, "Connection Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void generateQr(String data, ImageView imageView) {
        com.google.zxing.MultiFormatWriter writer = new com.google.zxing.MultiFormatWriter();
        try {
            com.google.zxing.common.BitMatrix matrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);
            com.journeyapps.barcodescanner.BarcodeEncoder encoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
            android.graphics.Bitmap bitmap = encoder.createBitmap(matrix);
            imageView.setImageBitmap(bitmap);
        } catch (com.google.zxing.WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
