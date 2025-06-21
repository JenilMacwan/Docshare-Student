package com.example.student;

import com.google.firebase.analytics.FirebaseAnalytics;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.student.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private GeminiApiService geminiApiService;
    private ProgressBar progressBar;
    private final String GEMINI_API_KEY = "AIzaSyAwbz9L2QwLfH7ZWw_Z_q4AlIJBoBhQCO8"; //replace with your actual API KEY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("x-goog-api-key", GEMINI_API_KEY)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        });
        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geminiApiService = retrofit.create(GeminiApiService.class);

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputDialog();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_aboutus)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    navController.navigate(R.id.nav_home);
                } else if (id == R.id.nav_gallery) {
                    navController.navigate(R.id.nav_gallery);
                } else if (id == R.id.nav_aboutus) {
                    navController.navigate(R.id.nav_aboutus);
                } else if (id == R.id.nav_slideshow) {
                    showLogoutDialog();
                }

                drawer.closeDrawers();
                return true;
            }
        });
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your query");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String query = input.getText().toString();
            getGeminiResponse(query);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void getGeminiResponse(String query) {
        progressBar.setVisibility(View.VISIBLE);
        Call<GeminiResponse> call = geminiApiService.getResponse(new GeminiRequest(query)); // Removed GEMINI_API_KEY argument
        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d("GeminiResponse", "Response: " + response);
                    if (response.isSuccessful() && response.body() != null && response.body().getCandidates() != null && !response.body().getCandidates().isEmpty() && response.body().getCandidates().get(0).getContent()!= null && response.body().getCandidates().get(0).getContent().getParts() != null && !response.body().getCandidates().get(0).getContent().getParts().isEmpty() && response.body().getCandidates().get(0).getContent().getParts().get(0).getText() != null) {
                        try {
                            GeminiResponse geminiResponse = response.body();
                            Log.d("GeminiResponse", "Response Body: " + geminiResponse);
                            String geminiAnswer = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
                            Log.d("GeminiResponse", "Answer: " + geminiAnswer);
                            showResponseDialog(geminiAnswer);
                        } catch (Exception e) {
                            Log.e("GeminiResponse", "Error parsing response: " + e.getMessage(), e);
                            showResponseDialog("Error parsing response: " + e.getMessage());
                        }
                    } else {
                        Log.e("GeminiResponse", "Error fetching response: " + (response.body() != null ? response.body().toString() : ""));
                        showResponseDialog("Error fetching response: " + (response.body() != null ? response.body().toString() : ""));
                    }
                });
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("GeminiResponse", "Network Error: " + t.getMessage(), t);
                    showResponseDialog("Network Error: " + t.getMessage());
                });
            }
        });
    }

    private void showResponseDialog(String response) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Gemini Response");
            final TextView responseTextView = new TextView(MainActivity.this);
            responseTextView.setText(response);
            responseTextView.setTextIsSelectable(true);
            builder.setView(responseTextView);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}