package com.example.aadhan;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Retrieve the marker title passed from the MapsActivity
        String markerTitle = getIntent().getStringExtra("markerTitle");

        // Display the marker title
        TextView textView = findViewById(R.id.marker_info);
        textView.setText(markerTitle);
    }
}