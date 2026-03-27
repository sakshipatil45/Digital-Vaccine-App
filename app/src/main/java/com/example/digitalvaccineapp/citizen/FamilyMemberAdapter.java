package com.example.digitalvaccineapp.citizen;
import com.example.digitalvaccineapp.shared.RecordsActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.citizen.FamilyMember;

import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.FamilyMemberViewHolder> {

    private List<FamilyMember> memberList;

    public FamilyMemberAdapter(List<FamilyMember> memberList) {
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public FamilyMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_family_member, parent, false);
        return new FamilyMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyMemberViewHolder holder, int position) {
        FamilyMember member = memberList.get(position);
        holder.tvMemberName.setText(member.getName());
        holder.tvMemberDetails.setText(member.getRelationship() + " • " + member.getAge() + " yrs • " + member.getGender());
        
        // Open RecordsActivity filtered for this dependent
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.digitalvaccineapp.shared.RecordsActivity.class);
            intent.putExtra("filterDependent", member.getName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public void setMemberList(List<FamilyMember> newList) {
        this.memberList = newList;
        notifyDataSetChanged();
    }

    static class FamilyMemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName;
        TextView tvMemberDetails;

        public FamilyMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberDetails = itemView.findViewById(R.id.tvMemberDetails);
        }
    }
}
