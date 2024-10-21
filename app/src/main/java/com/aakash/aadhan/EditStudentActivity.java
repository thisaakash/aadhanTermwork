package com.aakash.aadhan;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import cn.pedant.SweetAlert.SweetAlertDialog;

// Add this import
import com.aakash.aadhan.Student;

public class EditStudentActivity extends AppCompatActivity implements EditStudentAdapter.OnEditClickListener {

    private RecyclerView recyclerView;
    private EditStudentAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        recyclerView = findViewById(R.id.recyclerViewStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        loadStudents();
    }

    private void loadStudents() {
        db.collection("students")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Student> students = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Student student = document.toObject(Student.class);
                    student.setId(document.getId());
                    students.add(student);
                }
                adapter = new EditStudentAdapter(students, this);
                recyclerView.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                // Handle the error
            });
    }

    @Override
    public void onEditClick(Student student) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_student, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etRollNumber = dialogView.findViewById(R.id.etRollNumber);
        EditText etCompanyName = dialogView.findViewById(R.id.etCompanyName);
        EditText etTechnology = dialogView.findViewById(R.id.etTechnology);

        if (etName == null || etRollNumber == null || etCompanyName == null || etTechnology == null) {
            Log.e("EditStudentActivity", "One or more EditText views not found in dialog layout");
            return;
        }

        etName.setText(student.getName() != null ? student.getName() : "");
        etRollNumber.setText(student.getRollNumber() != null ? student.getRollNumber() : "");
        etCompanyName.setText(student.getCompanyName() != null ? student.getCompanyName() : "");
        etTechnology.setText(student.getTechnology() != null ? student.getTechnology() : "");

        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Edit Student")
            .setCustomView(dialogView)
            .setConfirmText("Save")
            .setConfirmClickListener(sDialog -> {
                // Get updated data from dialog
                String updatedName = etName.getText().toString().trim();
                String updatedRollNumber = etRollNumber.getText().toString().trim();
                String updatedCompanyName = etCompanyName.getText().toString().trim();
                String updatedTechnology = etTechnology.getText().toString().trim();
                
                // Update Firestore
                db.collection("students").document(student.getId())
                    .update(
                        "name", updatedName,
                        "rollNumber", updatedRollNumber,
                        "companyName", updatedCompanyName,
                        "technology", updatedTechnology
                    )
                    .addOnSuccessListener(aVoid -> {
                        sDialog.dismissWithAnimation();
                        loadStudents(); // Reload the list
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error
                        Log.e("EditStudentActivity", "Failed to update student", e);
                        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Failed to update student: " + e.getMessage())
                            .show();
                    });
            })
            .show();
    }
}
