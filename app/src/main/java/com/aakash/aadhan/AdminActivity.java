package com.aakash.aadhan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private Button btnAddCompany, btnAddStudent, btnEditStudent, btnEditCompany, btnSendNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnAddCompany = findViewById(R.id.btnAddCompany);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnEditStudent = findViewById(R.id.btnEditStudent);
        btnEditCompany = findViewById(R.id.btnEditCompany);
        btnSendNotification = findViewById(R.id.btnSendNotification);

        btnAddCompany.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, AddCompanyActivity.class)));
        btnAddStudent.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, AddStudentActivity.class)));
        btnEditStudent.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, EditStudentActivity.class)));
        btnEditCompany.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, EditCompanyActivity.class)));
        btnSendNotification.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, SendNotificationActivity.class);
            startActivity(intent);
        });
    }
}
