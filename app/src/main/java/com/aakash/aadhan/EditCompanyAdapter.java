package com.aakash.aadhan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EditCompanyAdapter extends RecyclerView.Adapter<EditCompanyAdapter.ViewHolder> {

    private List<Company> companies;
    private OnEditClickListener listener;

    public EditCompanyAdapter(List<Company> companies, OnEditClickListener listener) {
        this.companies = companies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_company, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Company company = companies.get(position);
        holder.tvName.setText(company.getName());
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(company));
    }

    @Override
    public int getItemCount() {
        return companies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    public interface OnEditClickListener {
        void onEditClick(Company company);
    }
}