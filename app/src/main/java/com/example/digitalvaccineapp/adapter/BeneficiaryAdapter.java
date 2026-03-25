package com.example.digitalvaccineapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.Beneficiary;

import java.util.List;

public class BeneficiaryAdapter extends RecyclerView.Adapter<BeneficiaryAdapter.BeneficiaryViewHolder> {

    private List<Beneficiary> beneficiaryList;
    private OnBeneficiaryClickListener listener;

    public interface OnBeneficiaryClickListener {
        void onItemClick(Beneficiary beneficiary);
    }

    public BeneficiaryAdapter(List<Beneficiary> beneficiaryList, OnBeneficiaryClickListener listener) {
        this.beneficiaryList = beneficiaryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BeneficiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beneficiary, parent, false);
        return new BeneficiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BeneficiaryViewHolder holder, int position) {
        Beneficiary beneficiary = beneficiaryList.get(position);
        
        holder.tvName.setText(beneficiary.getName());
        holder.tvDetails.setText(beneficiary.getVillage() + " • " + beneficiary.getAge() + " yrs");
        holder.tvCategoryBadge.setText(beneficiary.getCategory());
        
        String icon = "👤";
        if ("Child".equalsIgnoreCase(beneficiary.getCategory())) icon = "👶";
        else if ("Pregnant Woman".equalsIgnoreCase(beneficiary.getCategory())) icon = "🤰";
        else if ("Adult".equalsIgnoreCase(beneficiary.getCategory())) icon = "🧑";
        holder.tvCategoryIcon.setText(icon);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(beneficiary);
        });
    }

    @Override
    public int getItemCount() {
        return beneficiaryList.size();
    }

    public static class BeneficiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvCategoryIcon, tvCategoryBadge;

        public BeneficiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBeneficiaryName);
            tvDetails = itemView.findViewById(R.id.tvBeneficiaryDetails);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
        }
    }
}
