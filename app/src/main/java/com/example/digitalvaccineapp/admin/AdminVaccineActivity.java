package com.example.digitalvaccineapp.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class AdminVaccineActivity extends AppCompatActivity {

    private RecyclerView rvVaccineMaster;
    private ProgressBar pbVaccineMaster;
    private View llEmptyVaccines;
    private FloatingActionButton fabAddVaccine;
    private FirebaseFirestore db;
    private List<Vaccine> vaccineList;
    private VaccineAdapter adapter;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_vaccine);

        db = FirebaseFirestore.getInstance();
        rvVaccineMaster = findViewById(R.id.rvVaccineMaster);
        pbVaccineMaster = findViewById(R.id.pbVaccineMaster);
        llEmptyVaccines = findViewById(R.id.llEmptyVaccines);
        fabAddVaccine = findViewById(R.id.fabAddVaccine);

        Toolbar toolbar = findViewById(R.id.toolbarVaccineMaster);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vaccine Inventory");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvVaccineMaster.setLayoutManager(new LinearLayoutManager(this));
        vaccineList = new ArrayList<>();
        adapter = new VaccineAdapter(vaccineList);
        rvVaccineMaster.setAdapter(adapter);

        fabAddVaccine.setOnClickListener(v -> showVaccineDialog(null));

        // Bottom Navigation Setup
        bottomNavigationView = findViewById(R.id.adminBottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_admin_vaccines);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dash) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return false;
            } else if (id == R.id.nav_admin_vaccines) {
                return true;
            } else if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUserListActivity.class));
                return false;
            } else if (id == R.id.nav_admin_profile) {
                startActivity(new Intent(this, AdminProfileActivity.class));
                return false;
            }
            return false;
        });

        loadVaccines();
    }

    private final String[] defaultVaccines = {
            "BCG (Tuberculosis)", "OPV (Oral Polio)", "HepB (Hepatitis B)",
            "Pentavalent (DPT+HepB+Hib)", "IPV (Injected Polio)", "Rotavirus Vaccine",
            "PCV (Pneumococcal)", "Measles/MR", "JE (Japanese Encephalitis)",
            "DPT Booster", "Vitamin A", "Td (Tetanus & Diphtheria)"
    };

    private void loadVaccines() {
        pbVaccineMaster.setVisibility(View.VISIBLE);
        db.collection("vaccines_master")
                .orderBy("recommendedMonths")
                .addSnapshotListener((snapshots, e) -> {
                    pbVaccineMaster.setVisibility(View.GONE);
                    vaccineList.clear();
                    java.util.Set<String> addedNames = new java.util.HashSet<>();

                    // 1. Add Custom/Database Vaccines first (Source of Truth)
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Vaccine v = doc.toObject(Vaccine.class);
                            v.setId(doc.getId());
                            vaccineList.add(v);
                            addedNames.add(v.getName().toLowerCase());
                        }
                    }

                    // 2. Add Predefined Vaccines from array if they don't exist in DB
                    for (String name : defaultVaccines) {
                        // Extract base name for comparison (e.g., "BCG" from "BCG (Tuberculosis)")
                        String baseName = name.split(" ")[0].toLowerCase();
                        boolean alreadyExists = false;
                        for (String existing : addedNames) {
                            if (existing.contains(baseName)) {
                                alreadyExists = true;
                                break;
                            }
                        }

                        if (!alreadyExists) {
                            Vaccine v = new Vaccine();
                            v.setName(name);
                            v.setAgeGroup("General");
                            v.setDoseInfo("Standard");
                            v.setDescription("Predefined system vaccine");
                            vaccineList.add(v);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (vaccineList.isEmpty()) {
                        llEmptyVaccines.setVisibility(View.VISIBLE);
                        rvVaccineMaster.setVisibility(View.GONE);
                    } else {
                        llEmptyVaccines.setVisibility(View.GONE);
                        rvVaccineMaster.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showVaccineDialog(Vaccine existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_vaccine, null);
        EditText etName = view.findViewById(R.id.etVacName);
        EditText etMonths = view.findViewById(R.id.etVacMonths);
        EditText etDose = view.findViewById(R.id.etVacDose);
        EditText etDesc = view.findViewById(R.id.etVacDesc);
        Spinner spGroup = view.findViewById(R.id.spVacGroup);

        String[] groups = { "Infant", "Child", "Teen", "Adult" };
        spGroup.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups));

        if (existing != null) {
            etName.setText(existing.getName());
            etMonths.setText(String.valueOf(existing.getRecommendedMonths()));
            etDose.setText(existing.getDoseInfo());
            etDesc.setText(existing.getDescription());
            for (int i = 0; i < groups.length; i++) {
                if (groups[i].equals(existing.getAgeGroup())) {
                    spGroup.setSelection(i);
                    break;
                }
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? "Add New Vaccine" : "Edit Vaccine")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String months = etMonths.getText().toString();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(months))
                        return;

                    Vaccine v = (existing == null) ? new Vaccine() : existing;
                    v.setName(name);
                    v.setRecommendedMonths(Integer.parseInt(months));
                    v.setDoseInfo(etDose.getText().toString());
                    v.setDescription(etDesc.getText().toString());
                    v.setAgeGroup(spGroup.getSelectedItem().toString());

                    if (existing == null) {
                        db.collection("vaccines_master").add(v);
                    } else {
                        db.collection("vaccines_master").document(existing.getId()).set(v);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class VaccineAdapter extends RecyclerView.Adapter<VaccineAdapter.VH> {
        private List<Vaccine> list;

        public VaccineAdapter(List<Vaccine> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_vaccine_master, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int p) {
            Vaccine v = list.get(p);
            h.name.setText(v.getName());
            h.months.setText(v.getRecommendedMonths() + "m");
            h.group.setText("Group: " + v.getAgeGroup());
            h.desc.setText(v.getDescription());
            h.edit.setOnClickListener(view -> showVaccineDialog(v));
            h.delete.setOnClickListener(view -> {
                new MaterialAlertDialogBuilder(AdminVaccineActivity.this)
                        .setTitle("Delete Vaccine?")
                        .setMessage("This will remove it from the system inventory.")
                        .setPositiveButton("Delete",
                                (d, w) -> db.collection("vaccines_master").document(v.getId()).delete())
                        .setNegativeButton("Keep", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView name, months, group, desc;
            ImageButton edit, delete;

            public VH(@NonNull View v) {
                super(v);
                name = v.findViewById(R.id.tvVaccineMasterName);
                months = v.findViewById(R.id.tvVaccineMasterMonths);
                group = v.findViewById(R.id.tvVaccineMasterGroup);
                desc = v.findViewById(R.id.tvVaccineMasterDesc);
                edit = v.findViewById(R.id.btnEditVaccine);
                delete = v.findViewById(R.id.btnDeleteVaccine);
            }
        }
    }
}
