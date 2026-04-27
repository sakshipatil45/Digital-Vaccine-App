package com.example.digitalvaccineapp.citizen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.admin.Vaccine;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class VaccinationScheduleActivity extends AppCompatActivity {

    private RecyclerView rvSchedule;
    private ProgressBar pbSchedule;
    private TabLayout tabLayout;
    private FirebaseFirestore db;
    private List<Vaccine> fullList, filteredList;
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination_schedule);

        db = FirebaseFirestore.getInstance();
        rvSchedule = findViewById(R.id.rvSchedule);
        pbSchedule = findViewById(R.id.pbSchedule);
        tabLayout = findViewById(R.id.tabLayoutSchedule);

        Toolbar toolbar = findViewById(R.id.toolbarSchedule);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vaccination Schedule");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        fullList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ScheduleAdapter(filteredList);
        rvSchedule.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterByGroup(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        loadMasterSchedule();
    }

    private void loadMasterSchedule() {
        pbSchedule.setVisibility(View.VISIBLE);
        db.collection("vaccines_master")
                .orderBy("recommendedMonths")
                .get()
                .addOnCompleteListener(task -> {
                    pbSchedule.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        fullList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            fullList.add(doc.toObject(Vaccine.class));
                        }
                        filterByGroup("Infant"); // Default tab
                    }
                });
    }

    private void filterByGroup(String group) {
        filteredList.clear();
        for (Vaccine v : fullList) {
            if (group.equalsIgnoreCase(v.getAgeGroup())) {
                filteredList.add(v);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {
        private List<Vaccine> list;

        public ScheduleAdapter(List<Vaccine> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int p) {
            Vaccine v = list.get(p);
            h.t1.setText(v.getName() + " (" + v.getDoseInfo() + ")");
            h.t2.setText("Recommended at " + v.getRecommendedMonths() + " months\n" + v.getDescription());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;

            public VH(@NonNull View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
                t1.setTextColor(getResources().getColor(R.color.black));
                t1.setTextSize(16);
            }
        }
    }
}
