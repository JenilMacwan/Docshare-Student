package com.example.student; // Use your actual package name

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

// Use SuppressLint for SYSTEM_UI_FLAG_FULLSCREEN if targeting lower APIs,
// but WindowInsetsControllerCompat is preferred for API 30+
@SuppressLint("CustomSplashScreen") // Suppress lint warning for custom splash screen
public class SplashActivity extends AppCompatActivity {

    private static final long ANIMATION_DURATION = 1500; // ms (1.5 seconds)
    private static final float START_SCALE = 0.8f; // Start slightly smaller

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Make it fullscreen (Optional but common for splashes) ---
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        // --- End Fullscreen ---

        setContentView(R.layout.activity_splash);

        ImageView splashLogo = findViewById(R.id.splashLogo);


        // --- Prepare Animation ---
        // Start invisible and slightly smaller
        splashLogo.setAlpha(0f);
        splashLogo.setScaleX(START_SCALE);
        splashLogo.setScaleY(START_SCALE);

        // --- Start Animation ---
        splashLogo.animate()
                .alpha(1f) // Fade in
                .scaleX(1f) // Scale to normal size
                .scaleY(1f) // Scale to normal size
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator()) // Smooth start/end
                .withEndAction(() -> {
                    // --- Animation Finished - Navigate to Main Activity ---
                    navigateToMain();
                })
                .start();


        // --- Fallback Timer (Optional but good practice) ---
        // If animation somehow fails or takes too long, navigate after a max delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToMainIfNotAlready, ANIMATION_DURATION + 500); // Slightly longer than anim
    }

    private boolean navigated = false; // Flag to prevent double navigation

    private void navigateToMain() {
        if (!navigated) {
            navigated = true;
            // Replace MainActivity.class with your actual main activity
            Intent intent = new Intent(SplashActivity.this, login.class);
            startActivity(intent);
            // Finish SplashActivity so user can't navigate back to it
            finish();
        }
    }
    private void navigateToMainIfNotAlready() {
        // This is called by the fallback timer
        navigateToMain();
    }

    @Override
    public void onBackPressed() {
        // Prevent user from backing out of splash screen
        // super.onBackPressed(); // Comment this out or leave method empty
    }
}