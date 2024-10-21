package com.aakash.aadhan.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.aakash.aadhan.R;
import com.aakash.aadhan.model.Company;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder> {

    private List<Company> companies;
    private OnCompanyClickListener listener;

    public interface OnCompanyClickListener {
        void onCompanyClick(Company company);
    }

    public CompanyAdapter(List<Company> companies, OnCompanyClickListener listener) {
        this.companies = companies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company_card, parent, false);
        return new CompanyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        Company company = companies.get(position);
        holder.bind(company);
    }

    @Override
    public int getItemCount() {
        return companies.size();
    }

    class CompanyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCompany;
        TextView textViewCompanyName, textViewCity, textViewTech;
        Chip chipOption;

        CompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCompany = itemView.findViewById(R.id.imageViewCompany);
            textViewCompanyName = itemView.findViewById(R.id.textViewCompanyName);
            textViewCity = itemView.findViewById(R.id.textViewCity);
            textViewTech = itemView.findViewById(R.id.textViewTech);
            chipOption = itemView.findViewById(R.id.chipOption);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCompanyClick(companies.get(position));
                }
            });
        }

        void bind(Company company) {
            Glide.with(itemView.getContext())
                    .load(company.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(imageViewCompany);

            textViewCompanyName.setText(company.name);

            setTextWithIcon(textViewCity, R.drawable.ic_location, company.city);
            setTextWithIcon(textViewTech, R.drawable.ic_tech, company.tech);

            chipOption.setText(company.option);
            setChipStyle(chipOption, company.option);
        }

        private void setTextWithIcon(TextView textView, int drawableRes, String text) {
            Drawable icon = ContextCompat.getDrawable(itemView.getContext(), drawableRes);
            if (icon != null) {
                int size = (int) (textView.getLineHeight() * 0.8); // 80% of the text height
                icon.setBounds(0, 0, size, size);
                textView.setCompoundDrawables(icon, null, null, null);
            }
            textView.setText(text);
        }

        private void setChipStyle(Chip chip, String option) {
            int backgroundColor;
            int textColor;

            switch (option.toLowerCase()) {
                case "on campus":
                    backgroundColor = R.color.internship_bg;
                    textColor = R.color.internship_text;
                    break;
                case "off campus":
                    backgroundColor = R.color.fulltime_bg;
                    textColor = R.color.fulltime_text;
                    break;
                default:
                    backgroundColor = R.color.default_chip_bg;
                    textColor = R.color.default_chip_text;
                    break;
            }

            chip.setChipBackgroundColorResource(backgroundColor);
            chip.setTextColor(ContextCompat.getColor(itemView.getContext(), textColor));
        }
    }
}