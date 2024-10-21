package com.aakash.aadhan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProfileFragment extends Fragment {

    private ImageView studentImage;
    private TextView studentName, studentEmail, studentId, companyName;
    private Button logoutButton, adminButton;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        studentImage = view.findViewById(R.id.studentImage);
        studentName = view.findViewById(R.id.studentName);
        studentEmail = view.findViewById(R.id.studentEmail);
        studentId = view.findViewById(R.id.studentId);
        companyName = view.findViewById(R.id.companyName);
        logoutButton = view.findViewById(R.id.logoutButton);
        adminButton = view.findViewById(R.id.adminButton);

        // Get SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AadhanPrefs", Context.MODE_PRIVATE);

        // Check if the user is an admin
        String userEmail = sharedPreferences.getString("studentemail", "");
        Log.e("userEmail",userEmail);
        if ("this.aakashshah@gmail.com".equals(userEmail)) {
            adminButton.setVisibility(View.VISIBLE);
        } else {
            adminButton.setVisibility(View.GONE);
        }

        loadUserData();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdminPinDialog();
            }
        });

        return view;
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AadhanPrefs", Context.MODE_PRIVATE);
        
        String imageUrl = sharedPreferences.getString("studentImage", "");
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.default_profile_image)
            .error(R.drawable.default_profile_image)
            .circleCrop()
            .into(studentImage);

        studentName.setText(sharedPreferences.getString("studentname", ""));
        studentEmail.setText(sharedPreferences.getString("studentemail", ""));
        studentId.setText(sharedPreferences.getString("studentid", ""));
        companyName.setText(sharedPreferences.getString("companyname", ""));
    }

    private void logout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AadhanPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // You might want to clear any other app data here

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showAdminPinDialog() {
        final EditText pinInput = new EditText(getContext());
        pinInput.setHint("Enter 6-digit PIN");
        pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Admin Access")
            .setCustomView(pinInput)
            .setConfirmText("Submit")
            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    String pin = pinInput.getText().toString();
                    if (pin.length() == 6 && pin.equals("123456")) { // Replace "123456" with your actual PIN
                        sDialog.dismissWithAnimation();
                        openAdminActivity();
                    } else {
                        sDialog.setTitleText("Invalid PIN")
                               .setContentText("Please try again")
                               .setConfirmText("OK")
                               .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    }
                }
            })
            .show();
    }

    private void openAdminActivity() {
        Intent intent = new Intent(getActivity(), AdminActivity.class);
        startActivity(intent);
    }
}
