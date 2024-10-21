package com.aakash.aadhan;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NestedScrollView scrollView;
    private ProgressBar progressBar;
    private StudentAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        scrollView = view.findViewById(R.id.scrollView);
        progressBar = view.findViewById(R.id.progressBar);

        adapter = new StudentAdapter();
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        FloatingActionButton fabWhatsApp = view.findViewById(R.id.fabWhatsApp);
        fabWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWhatsapp("8866172310");
            }
        });

        loadData();

        return view;
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("students")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Student> students = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Student student = document.toObject(Student.class);
                        students.add(student);
                    }
                    
                    // Sort students by roll number
                    Collections.sort(students, new Comparator<Student>() {
                        @Override
                        public int compare(Student s1, Student s2) {
                            return s1.getRollNumber().compareTo(s2.getRollNumber());
                        }
                    });
                    
                    adapter.setStudents(students);
                } else {
                    // Handle possible errors
                }
                progressBar.setVisibility(View.GONE);
            });
    }

    private void openWhatsapp(String whatsappNo) {
        String formattedNumber = whatsappNo.startsWith("+91") ? whatsappNo : "+91" + whatsappNo;
        Uri uri = Uri.parse("smsto:" + formattedNumber);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.setPackage("com.whatsapp");

        PackageManager packageManager = requireContext().getPackageManager();
        if (intent.resolveActivity(packageManager) == null) {
            Uri uri1 = Uri.parse("smsto:" + formattedNumber);
            Intent intent1 = new Intent(Intent.ACTION_SENDTO, uri1);
            intent1.setPackage("com.whatsapp.w4b");

            if (intent1.resolveActivity(packageManager) == null) {
                String url = "https://api.whatsapp.com/send?phone=" + formattedNumber + "&text=Hello,%20From%20Aadhan%20App";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } else {
                startActivity(intent1);
            }
        } else {
            startActivity(Intent.createChooser(intent, ""));
        }
    }
}
