package com.example.digitalvaccineapp.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.google.firebase.auth.FirebaseAuth;
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

        // Normally we'd fetch this from the Intent extras or a global User singleton
        String userName = "Vaccine User";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            userName = email != null ? email.split("@")[0] : "User";
        }
        
        tvName.setText("Name: " + userName);
        
        // Mocking the overall vaccine status for the certificate
        String statusText = "Vaccine: COVID-19 (2 Doses) \nStatus: Fully Vaccinated \nVerified by DigitalVaccineApp";
        tvDetails.setText(statusText);

        // Generate QR Code containing this proof text
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(userName + " | " + statusText, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            ivQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }
        
        findViewById(R.id.btnDownloadCert).setOnClickListener(v -> {
            Toast.makeText(this, "Downloading certificate... (Mock PDF)", Toast.LENGTH_SHORT).show();
        });
    }
}
