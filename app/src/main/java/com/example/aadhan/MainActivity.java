package com.example.aadhan;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        // Load the animation
        Animation popupAnimation = AnimationUtils.loadAnimation(this, R.anim.popup_animation);

        // Set an AnimationListener
        popupAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation ended, start new activity
                Intent intent = new Intent(MainActivity.this, Home.class);
                startActivity(intent);
                finish(); // Optional: finish the current activity
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation repeated
            }
        });

        // Start the animation
        imageView.startAnimation(popupAnimation);
    }
}
