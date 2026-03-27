package com.example.digitalvaccineapp.shared;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.VaccineInfoAdapter;
import com.example.digitalvaccineapp.shared.VaccineInfo;
import java.util.ArrayList;
import java.util.List;

public class VaccineInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_info);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.rvVaccineInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<VaccineInfo> infoList = new ArrayList<>();
        populateData(infoList);

        VaccineInfoAdapter adapter = new VaccineInfoAdapter(infoList);
        recyclerView.setAdapter(adapter);
    }

    private void populateData(List<VaccineInfo> list) {
        list.add(new VaccineInfo("BCG", "Protects against tuberculosis. Usually given at birth.", "Single dose", "Infants", R.drawable.ic_vax));
        list.add(new VaccineInfo("Hepatitis B", "Protects against liver infection caused by Hep B virus.", "3 doses", "All ages", R.drawable.ic_vax));
        list.add(new VaccineInfo("Polio (IPV/OPV)", "Protects against poliomyelitis which causes paralysis.", "4 doses + boosters", "Children", R.drawable.ic_vax));
        list.add(new VaccineInfo("DTP", "Combined vaccine for Diphtheria, Tetanus, and Pertussis.", "5 doses", "Children", R.drawable.ic_vax));
        list.add(new VaccineInfo("MMR", "Combined vaccine for Measles, Mumps, and Rubella.", "2 doses", "Children", R.drawable.ic_vax));
        list.add(new VaccineInfo("COVID-19", "Protects against severe symptoms of the SARS-CoV-2 virus.", "2 doses + boosters", "All ages", R.drawable.ic_vax));
        list.add(new VaccineInfo("Influenza (Flu)", "Seasonal vaccine to protect against various flu strains.", "Annual", "All ages", R.drawable.ic_vax));
        list.add(new VaccineInfo("Typhoid", "Protects against typhoid fever caused by Salmonella Typhi.", "Single dose + boosters", "All ages", R.drawable.ic_vax));
    }
}
