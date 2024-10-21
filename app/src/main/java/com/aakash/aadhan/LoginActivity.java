package com.aakash.aadhan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.aakash.aadhan.api.AadhanApi;
import com.aakash.aadhan.model.LoginRequest;
import com.aakash.aadhan.model.LoginResponse;
import cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONObject;
import org.json.JSONException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private SweetAlertDialog pDialog;
    private String phoneNumber;
    private LoginResponse loginResponse;
    private int storedOtp;
    private SweetAlertDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                
                if (validateInput(email, password)) {
                    performLogin(email, password);
                }
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            showErrorDialog("Email is required");
            return false;
        }
        if (password.isEmpty()) {
            showErrorDialog("Password is required");
            return false;
        }
        return true;
    }

    private void showErrorDialog(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText(message)
            .show();
    }

    private void performLogin(String email, String password) {
        showProgressDialog();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://aadhan-api.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AadhanApi api = retrofit.create(AadhanApi.class);
        LoginRequest loginRequest = new LoginRequest(email, password);

        Call<LoginResponse> call = api.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                dismissProgressDialog();
                if (response.isSuccessful() && response.body() != null) {
                    loginResponse = response.body();
                    showPhoneNumberConfirmationDialog();
                } else {
                    showErrorDialog("Login failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                dismissProgressDialog();
                showErrorDialog("Network error. Please check your connection.");
            }
        });
    }

    private void showProgressDialog() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.primary));
        pDialog.setTitleText("Logging in");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismissWithAnimation();
        }
    }

    private void showPhoneNumberConfirmationDialog() {
        String maskedPhone = maskPhoneNumber(loginResponse.getStudentphone());
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Confirm Phone Number")
            .setContentText("OTP will be sent to this number: " + maskedPhone)
            .setConfirmText("Send OTP")
            .setCancelText("Cancel")
            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.dismissWithAnimation();
                    sendOtp(loginResponse.getStudentphone());
                }
            })
            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.dismissWithAnimation();
                    // Handle cancellation (e.g., go back to login screen or exit)
                }
            })
            .show();
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return phoneNumber;
        }
        String lastFourDigits = phoneNumber.substring(phoneNumber.length() - 4);
        String maskedPart = new String(new char[phoneNumber.length() - 4]).replace("\0", "x");
        return maskedPart + lastFourDigits;
    }

    private void sendOtp(final String phoneNumber) {
        showWaitingDialog("Sending OTP...");

        String url = "https://kalyanlivesatta.com/api-resend-otp";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("app_key", "SgRTXsywVmWteEBLlYWecwgbDiHwlh");
            jsonBody.put("env_type", "Prod");
            jsonBody.put("mobile", phoneNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
            new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean status = response.getBoolean("status");
                        String msg = response.getString("msg");
                        if (status) {
                            storedOtp = response.getInt("otp");
                            updateWaitingDialog("OTP sent successfully. Please wait...");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dismissWaitingDialog();
                                    showOtpVerificationDialog();
                                }
                            }, 2000);
                        } else {
                            dismissWaitingDialog();
                            showErrorDialog("Failed to send OTP: " + msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dismissWaitingDialog();
                        showErrorDialog("Error processing OTP response");
                    }
                }
            },
            new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dismissWaitingDialog();
                    showErrorDialog("Error: " + error.getMessage());
                }
            });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void showWaitingDialog(String message) {
        waitingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.primary));
        waitingDialog.setTitleText(message);
        waitingDialog.setCancelable(false);
        waitingDialog.show();
    }

    private void updateWaitingDialog(String message) {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.setTitleText(message);
        }
    }

    private void dismissWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismissWithAnimation();
        }
    }

    private void showOtpVerificationDialog() {
        final EditText otpInput = new EditText(this);
        otpInput.setHint("Enter OTP");
        otpInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Enter OTP")
            .setCustomView(otpInput)
            .setConfirmText("Verify")
            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    String otp = otpInput.getText().toString();
                    if (verifyOtp(otp)) {
                        sDialog.dismissWithAnimation();
                        saveUserData(loginResponse);
                        showSuccessDialog();
                    } else {
                        showErrorDialog("Invalid OTP. Please try again.");
                    }
                }
            })
            .show();
    }

    private boolean verifyOtp(String otp) {
        try {
            int enteredOtp = Integer.parseInt(otp);
            return enteredOtp == storedOtp;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showSuccessDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Success!")
            .setContentText("You have successfully logged in.")
            .setConfirmText("OK")
            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.dismissWithAnimation();
                    navigateToMainActivity();
                }
            })
            .show();
    }

    private void saveUserData(LoginResponse loginResponse) {
        SharedPreferences sharedPreferences = getSharedPreferences("AadhanPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("companyname", loginResponse.getCompanyname());
        editor.putString("studentImage", loginResponse.getStudentImage());
        editor.putString("studentemail", loginResponse.getStudentemail());
        editor.putString("studentid", loginResponse.getStudentid());
        editor.putString("studentname", loginResponse.getStudentname());
        editor.apply();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
