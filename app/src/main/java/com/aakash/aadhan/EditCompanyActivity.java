package com.aakash.aadhan;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import cn.pedant.SweetAlert.SweetAlertDialog;
import android.view.View;
import android.widget.EditText;

// Add this import
import com.aakash.aadhan.Company;

public class EditCompanyActivity extends AppCompatActivity implements EditCompanyAdapter.OnEditClickListener {

    private RecyclerView recyclerView;
    private EditCompanyAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_company);

        recyclerView = findViewById(R.id.recyclerViewCompanies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        loadCompanies();
    }

    private void loadCompanies() {
        db.collection("companies")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Company> companies = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Company company = document.toObject(Company.class);
                    company.setId(document.getId());
                    companies.add(company);
                }
                adapter = new EditCompanyAdapter(companies, this);
                recyclerView.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                // Handle the error
            });
    }

    @Override
    public void onEditClick(Company company) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_company, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        etName.setText(company.getName());

        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Edit Company")
            .setCustomView(dialogView)
            .setConfirmText("Save")
            .setConfirmClickListener(sDialog -> {
                // Get updated data from dialog
                String updatedName = etName.getText().toString().trim();
                
                // Update Firestore
                db.collection("companies").document(company.getId())
                    .update("name", updatedName)
                    .addOnSuccessListener(aVoid -> {
                        sDialog.dismissWithAnimation();
                        loadCompanies(); // Reload the list
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error
                        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Failed to update company: " + e.getMessage())
                            .show();
                    });
            })
            .show();
    }
}
