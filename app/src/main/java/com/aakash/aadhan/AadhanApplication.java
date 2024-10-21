package com.aakash.aadhan;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class AadhanApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
