package com.example.digitalvaccineapp.citizen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.citizen.FamilyMemberAdapter;
import com.example.digitalvaccineapp.citizen.FamilyMember;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FamilyMembersActivity extends AppCompatActivity {

    private RecyclerView rvFamilyMembers;
    private LinearLayout llEmptyState;
    private ExtendedFloatingActionButton fabAddFamilyMember;
    private android.widget.ProgressBar progressBar;
    private FamilyMemberAdapter adapter;
    private List<FamilyMember> memberList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_members);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarFamily);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvFamilyMembers = findViewById(R.id.rvFamilyMembers);
        llEmptyState = findViewById(R.id.llEmptyState);
        fabAddFamilyMember = findViewById(R.id.fabAddFamilyMember);
        progressBar = findViewById(R.id.progressBar);

        rvFamilyMembers.setLayoutManager(new LinearLayoutManager(this));
        memberList = new ArrayList<>();
        adapter = new FamilyMemberAdapter(memberList);
        rvFamilyMembers.setAdapter(adapter);

        fabAddFamilyMember.setOnClickListener(v -> {
            startActivity(new Intent(FamilyMembersActivity.this, AddFamilyMemberActivity.class));
        });
        
        // Hide FAB for view-only Citizens
        fabAddFamilyMember.setVisibility(View.GONE);

        loadFamilyMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFamilyMembers();
    }

    private void loadFamilyMembers() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        db.collection("family_members")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        memberList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String rel = document.getString("relationship");
                            if (rel == null) rel = document.getString("category");
                            
                            FamilyMember member = new FamilyMember(
                                    document.getId(),
                                    document.getString("name"),
                                    document.getString("age"),
                                    document.getString("gender"),
                                    rel
                            );
                            memberList.add(member);
                        }
                        adapter.notifyDataSetChanged();

                        if (memberList.isEmpty()) {
                            rvFamilyMembers.setVisibility(View.GONE);
                            llEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            rvFamilyMembers.setVisibility(View.VISIBLE);
                            llEmptyState.setVisibility(View.GONE);
                        }
                    } else {
                        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content),
                                "Sync failed", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
}
