package com.aakash.aadhan;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.aakash.aadhan.api.NotificationApi;
import com.aakash.aadhan.model.NotificationRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class SendNotificationActivity extends AppCompatActivity {

    private static final String TAG = "SendNotificationActivity";
    private static final String BASE_URL = "https://aadhan-api.vercel.app/";

    private TextInputEditText etTitle, etMessage, etImage;
    private MaterialButton btnSend;
    private NotificationApi notificationApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        etImage = findViewById(R.id.etImage);
        btnSend = findViewById(R.id.btnSend);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        notificationApi = retrofit.create(NotificationApi.class);

        btnSend.setOnClickListener(v -> sendNotification());
    }

    private void sendNotification() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String image = etImage.getText().toString().trim();

        Log.d(TAG, "sendNotification: Title: " + title);
        Log.d(TAG, "sendNotification: Message: " + message);
        Log.d(TAG, "sendNotification: Image URL: " + image);

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill title and message fields", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationRequest request = new NotificationRequest(title, message, image);
        Log.d(TAG, "sendNotification: Request: " + request.toString());

        Gson gson = new Gson();
        String jsonRequest = gson.toJson(request);
        Log.d(TAG, "sendNotification: JSON Request: " + jsonRequest);

        notificationApi.sendNotification(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Notification sent successfully. Response code: " + response.code());
                    Toast.makeText(SendNotificationActivity.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "onResponse: Error sending notification. Response code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "onResponse: Error body: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: Error reading error body", e);
                    }
                    Toast.makeText(SendNotificationActivity.this, "Error sending notification: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "onFailure: Network error", t);
                Toast.makeText(SendNotificationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
