package com.aakash.aadhan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.aakash.aadhan.model.Company;
import com.aakash.aadhan.adapter.CompanyAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

public class HomeFragment extends Fragment implements CompanyAdapter.OnCompanyClickListener {

    private RecyclerView recyclerViewCompanies;
    private CompanyAdapter companyAdapter;
    private List<Company> allCompanies;
    private List<Company> filteredCompanies;
    private FloatingActionButton fabFilter;
    private String currentCampusFilter = "All";
    private String currentTechFilter = "All";
    private String currentCityFilter = "All";
    private Map<String, Integer> techOptions = new HashMap<>();
    private Map<String, Integer> cityOptions = new HashMap<>();
    private TextView textViewNoCompanies;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewCompanies = view.findViewById(R.id.recyclerViewCompanies);
        fabFilter = view.findViewById(R.id.fabFilter);
        textViewNoCompanies = view.findViewById(R.id.textViewNoCompanies);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allCompanies = new ArrayList<>();
        filteredCompanies = new ArrayList<>();
        companyAdapter = new CompanyAdapter(filteredCompanies, this);

        recyclerViewCompanies.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCompanies.setAdapter(companyAdapter);

        fabFilter.setOnClickListener(v -> showFilterDialog());

        loadCompaniesFromFirestore();
    }

    private void loadCompaniesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("companies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allCompanies.clear();
                        techOptions.clear();
                        cityOptions.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Company company = document.toObject(Company.class);
                            allCompanies.add(company);
                            
                            // Count tech options
                            String tech = company.tech;
                            techOptions.put(tech, techOptions.getOrDefault(tech, 0) + 1);
                            
                            // Count city options
                            String city = company.city;
                            cityOptions.put(city, cityOptions.getOrDefault(city, 0) + 1);
                        }
                        applyFilters();
                    } else {
                        // Handle errors
                    }
                });
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter Companies");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        AppCompatSpinner spinnerCampus = dialogView.findViewById(R.id.spinnerCampus);
        AppCompatSpinner spinnerTech = dialogView.findViewById(R.id.spinnerTech);
        AppCompatSpinner spinnerCity = dialogView.findViewById(R.id.spinnerCity);

        setupSpinner(spinnerCampus, Arrays.asList("All", "On Campus", "Off Campus"), currentCampusFilter);
        setupSpinner(spinnerTech, getTechOptions(), currentTechFilter);
        setupSpinner(spinnerCity, getCityOptions(), currentCityFilter);

        builder.setPositiveButton("Apply", (dialog, which) -> {
            currentCampusFilter = spinnerCampus.getSelectedItem().toString();
            currentTechFilter = spinnerTech.getSelectedItem().toString();
            currentCityFilter = spinnerCity.getSelectedItem().toString();
            applyFilters();
        });

        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Clear Filters", (dialog, which) -> {
            currentCampusFilter = "All";
            currentTechFilter = "All";
            currentCityFilter = "All";
            applyFilters();
        });

        builder.show();
    }

    private void setupSpinner(Spinner spinner, List<String> options, String currentSelection) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int selectionIndex = options.indexOf(currentSelection);
        if (selectionIndex != -1) {
            spinner.setSelection(selectionIndex);
        }
    }

    private List<String> getTechOptions() {
        List<String> options = new ArrayList<>();
        options.add("All");
        for (Map.Entry<String, Integer> entry : techOptions.entrySet()) {
            options.add(entry.getKey() + " (" + entry.getValue() + ")");
        }
        return options;
    }

    private List<String> getCityOptions() {
        List<String> options = new ArrayList<>();
        options.add("All");
        for (Map.Entry<String, Integer> entry : cityOptions.entrySet()) {
            options.add(entry.getKey() + " (" + entry.getValue() + ")");
        }
        return options;
    }

    private void applyFilters() {
        filteredCompanies.clear();
        for (Company company : allCompanies) {
            if (matchesFilters(company)) {
                filteredCompanies.add(company);
            }
        }
        companyAdapter.notifyDataSetChanged();
        updateNoCompaniesVisibility();
    }

    private void updateNoCompaniesVisibility() {
        if (filteredCompanies.isEmpty()) {
            textViewNoCompanies.setVisibility(View.VISIBLE);
            recyclerViewCompanies.setVisibility(View.GONE);
        } else {
            textViewNoCompanies.setVisibility(View.GONE);
            recyclerViewCompanies.setVisibility(View.VISIBLE);
        }
    }

    private boolean matchesFilters(Company company) {
        boolean matchesCampus = currentCampusFilter.equals("All") || 
                                company.option.equalsIgnoreCase(currentCampusFilter);
        
        boolean matchesTech = currentTechFilter.equals("All") || 
                              company.tech.equalsIgnoreCase(currentTechFilter.replaceAll(" \\(\\d+\\)$", ""));
        
        boolean matchesCity = currentCityFilter.equals("All") || 
                              company.city.equalsIgnoreCase(currentCityFilter.replaceAll(" \\(\\d+\\)$", ""));
        
        return matchesCampus && matchesTech && matchesCity;
    }

    @Override
    public void onCompanyClick(Company company) {
        Intent intent = new Intent(getActivity(), CompanyDetailActivity.class);
        intent.putExtra("company", company);
        startActivity(intent);
    }
}
