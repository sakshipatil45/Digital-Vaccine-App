package com.example.digitalvaccineapp.shared;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.example.digitalvaccineapp.shared.CertificateSummary;
import com.example.digitalvaccineapp.shared.Vaccination;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.List;

public class CertificateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        ImageView ivQr = findViewById(R.id.ivQrCode);
        TextView tvName = findViewById(R.id.tvCertName);
        TextView tvDetails = findViewById(R.id.tvCertDetails);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // Fetch User and Vaccinations in parallel/sequence
        db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
            String userName = userDoc.exists() ? userDoc.getString("name") : "User";

            db.collection("vaccinations").whereEqualTo("userId", userId).get().addOnSuccessListener(shots -> {
                int maxDose = 0;
                String latestVaccine = "N/A";
                String latestId = null;

                for (QueryDocumentSnapshot doc : shots) {
                    Long doseL = doc.getLong("doseNumber");
                    int dose = (doseL != null) ? doseL.intValue() : 0;
                    if (dose > maxDose) {
                        maxDose = dose;
                        latestVaccine = doc.getString("vaccineName");
                        latestId = doc.getId();
                    }
                }

                String status = maxDose >= 2 ? "Fully Vaccinated" : (maxDose == 1 ? "Partially Vaccinated" : "Not Vaccinated");
                
                tvName.setText("Name: " + userName);
                String verifiedOn = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
                String details = "Vaccine: " + latestVaccine + "\nDose: " + maxDose + "\nStatus: " + status + "\nVerified on: " + verifiedOn;
                tvDetails.setText(details);

                generateQr(userName + " | " + status + " | " + latestVaccine + " | Dose " + maxDose, ivQr);
                
                if (latestId != null) {
                    final String vaccinationId = latestId;
                    findViewById(R.id.btnDownloadCert).setOnClickListener(v -> {
                        // Note: PDF generation still requires the backend server running
                        String url = "http://192.168.47.16:5000/api/vaccinations/download-certificate/" + vaccinationId;
                        android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                        startActivity(browserIntent);
                        Toast.makeText(CertificateActivity.this, "Connecting to local server for PDF...", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    findViewById(R.id.btnDownloadCert).setEnabled(false);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(CertificateActivity.this, "Error fetching records: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
