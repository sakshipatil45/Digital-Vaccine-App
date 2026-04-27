package com.example.digitalvaccineapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.admin.Beneficiary;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryAdapter extends RecyclerView.Adapter<BeneficiaryAdapter.BeneficiaryViewHolder> implements Filterable {

    private List<Beneficiary> beneficiaryList;
    private List<Beneficiary> beneficiaryListFull;
    private OnBeneficiaryClickListener listener;

    public interface OnBeneficiaryClickListener {
        void onItemClick(Beneficiary beneficiary);
    }

    public BeneficiaryAdapter(List<Beneficiary> beneficiaryList, OnBeneficiaryClickListener listener) {
        this.beneficiaryList = beneficiaryList;
        this.beneficiaryListFull = new ArrayList<>(beneficiaryList);
        this.listener = listener;
    }

    public void updateData(List<Beneficiary> newData) {
        this.beneficiaryList = newData;
        this.beneficiaryListFull = new ArrayList<>(newData);
        notifyDataSetChanged();
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
        
        String name = beneficiary.getName() != null ? beneficiary.getName() : "Unknown";

        String age = beneficiary.getAge() != null ? beneficiary.getAge() : "?";

        holder.tvName.setText(name);
        holder.tvDetails.setText(age + " yrs");
        holder.tvCategoryBadge.setText(beneficiary.getCategory());
        
        String icon = "👤";
        String cat = beneficiary.getCategory();
        if (cat != null) {
            if (cat.contains("year")) icon = "👶";
            else if (cat.equals("Pregnant Women")) icon = "🤰";
            else if (cat.contains("18+")) icon = "🧑";
        }
        holder.tvCategoryIcon.setText(icon);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(beneficiary);
        });
    }

    @Override
    public int getItemCount() {
        return beneficiaryList.size();
    }

    @Override
    public Filter getFilter() {
        return beneficiaryFilter;
    }

    private Filter beneficiaryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Beneficiary> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(beneficiaryListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Beneficiary item : beneficiaryListFull) {
                    String name = item.getName() != null ? item.getName().toLowerCase() : "";

                    
                    if (name.contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            beneficiaryList.clear();
            beneficiaryList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

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
