package com.aakash.aadhan;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        String markerTitle = getIntent().getStringExtra("markerTitle");
        TextView companyNameTextView = findViewById(R.id.companyNameTextView);
        TextView companyInfoTextView = findViewById(R.id.companyInfoTextView);

        companyNameTextView.setText(markerTitle);
        companyInfoTextView.setText(getCompanyInfo(markerTitle));
    }

    private String getCompanyInfo(String companyName) {
        // You can replace this with actual company information
        return "This is some information about " + companyName + ". You can add more details here.";
    }
}
