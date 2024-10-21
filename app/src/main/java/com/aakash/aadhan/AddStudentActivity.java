package com.aakash.aadhan;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Locale;

public class AddStudentActivity extends AppCompatActivity {

    private EditText etName, etRollNumber, etTechnology;
    private Button btnAddStudent;
    private FirebaseFirestore db;
    private AutoCompleteTextView spinnerCompany;
    private ArrayList<String> companyList;
    private ArrayAdapter<String> companyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        etName = findViewById(R.id.etName);
        etRollNumber = findViewById(R.id.etRollNumber);
        etTechnology = findViewById(R.id.etTechnology);
        btnAddStudent = findViewById(R.id.btnSubmit);
        spinnerCompany = findViewById(R.id.spinnerCompany);

        if (btnAddStudent == null) {
            showErrorDialog("Button not found in layout");
            return;
        }

        db = FirebaseFirestore.getInstance();

        btnAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStudent();
            }
        });

        companyList = new ArrayList<>();
        companyAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, companyList);
        spinnerCompany.setAdapter(companyAdapter);

        loadCompanies();
    }

    private void loadCompanies() {
        showLoadingDialog();
        db.collection("companies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    companyList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String companyName = document.getString("name");
                        if (companyName != null) {
                            companyList.add(companyName.toUpperCase(Locale.ROOT));
                        }
                    }
                    Collections.sort(companyList, String.CASE_INSENSITIVE_ORDER);
                    companyAdapter.notifyDataSetChanged();
                    dismissLoadingDialog();
                })
                .addOnFailureListener(e -> {
                    dismissLoadingDialog();
                    showErrorDialog("Error loading companies: " + e.getMessage());
                });
    }

    private void addStudent() {
        String name = etName.getText().toString().trim();
        String rollNumber = etRollNumber.getText().toString().trim();
        String companyName = spinnerCompany.getText().toString().trim().toUpperCase(Locale.ROOT);
        String technology = etTechnology.getText().toString().trim();

        if (name.isEmpty() || rollNumber.isEmpty() || companyName.isEmpty() || technology.isEmpty()) {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Please fill all fields")
                    .show();
            return;
        }

        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("rollNumber", rollNumber);
        student.put("companyName", companyName);
        student.put("technology", technology);
        student.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        showLoadingDialog();

        db.collection("students")
                .add(student)
                .addOnSuccessListener(documentReference -> {
                    dismissLoadingDialog();
                    showSuccessDialog("Student added successfully");
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    dismissLoadingDialog();
                    showErrorDialog("Error adding student: " + e.getMessage());
                });
    }

    private void clearFields() {
        etName.setText("");
        etRollNumber.setText("");
        etTechnology.setText("");
    }

    private SweetAlertDialog loadingDialog;

    private void showLoadingDialog() {
        loadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingDialog.setTitleText("Loading");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismissWithAnimation();
        }
    }

    private void showSuccessDialog(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText(message)
                .show();
    }

    private void showErrorDialog(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(message)
                .show();
    }
}
