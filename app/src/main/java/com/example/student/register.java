package com.example.student;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class register extends AppCompatActivity {
    private EditText FacnameEditText;
    private EditText FacIdEditText;
    private EditText FacEmailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ProgressBar passwordStrengthProgressBar;
    private TextView passwordStrengthTextView;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        FacnameEditText = findViewById(R.id.facultyNameEditText);
        FacIdEditText = findViewById(R.id.facultyIdEditText);
        FacEmailEditText = findViewById(R.id.facultyEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);

        passwordStrengthProgressBar = findViewById(R.id.passwordStrengthProgressBar);
        passwordStrengthTextView = findViewById(R.id.passwordStrengthTextView);

        Log.d("PasswordStrengthDebug", "ProgressBar and TextView initialized: " + (passwordStrengthProgressBar != null) + ", " + (passwordStrengthTextView != null));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() { // Set OnFocusChangeListener
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    passwordStrengthProgressBar.setVisibility(View.VISIBLE); // Show on focus gain
                    passwordStrengthTextView.setVisibility(View.VISIBLE);    // Show on focus gain
                    Log.d("PasswordStrengthDebug", "Password EditText focused - Strength UI Visible");
                } else {
                    passwordStrengthProgressBar.setVisibility(View.GONE);  // Hide on focus lost
                    passwordStrengthTextView.setVisibility(View.GONE);     // Hide on focus lost
                    Log.d("PasswordStrengthDebug", "Password EditText unfocused - Strength UI Gone");
                }
            }
        });


        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                Log.d("PasswordStrengthDebug", "TextWatcher.afterTextChanged called, password: " + password);
                updatePasswordStrengthView(password);
            }
        });


        registerButton.setOnClickListener(v -> {
            Log.d("RegisterDebug", "registerButton clicked"); // ADD THIS LINE - Log button click

            String FacId = FacIdEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String Facname = FacnameEditText.getText().toString().trim();
            String FacEmail = FacEmailEditText.getText().toString().trim();


            Log.d("RegisterDebug", "FacId: " + FacId + ", Password: " + password + ", Facname: " + Facname + ", FacEmail: " + FacEmail ); // Log Input Values

            if (TextUtils.isEmpty(FacId) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter Faculty ID and Password", Toast.LENGTH_SHORT).show();
                Log.d("RegisterDebug", "Validation: Faculty ID or Password empty"); // Log validation check
            } else if (TextUtils.isEmpty(Facname) || TextUtils.isEmpty(FacEmail)) {
                Toast.makeText(this, "Please enter Faculty Name and Email", Toast.LENGTH_SHORT).show();
                Log.d("RegisterDebug", "Validation: Faculty Name or Email empty"); // Log validation check
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                Log.d("RegisterDebug", "Validation: Passwords do not match"); // Log validation check
            } else {
                Log.d("RegisterDebug", "Validation Passed - Proceeding with registration"); // Log validation passed
                mAuth.createUserWithEmailAndPassword(FacEmail, password)
                        .addOnCompleteListener(this, task -> {
                            Log.d("RegisterDebug", "createUserWithEmailAndPassword - onCompleteListener called"); // Log listener start
                            if (task.isSuccessful()) {
                                Log.d("RegisterDebug", "createUserWithEmailAndPassword - task successful"); // Log success
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    Log.d("RegisterDebug", "FirebaseUser is NOT null"); // Log FirebaseUser check
                                    // Store additional user data in Firestore
                                    Map<String, Object> UserProfiles = new HashMap<>();
                                    UserProfiles.put("name", Facname);
                                    UserProfiles.put("studentId", FacId); // Using "facultyId" as field name
                                    UserProfiles.put("email", FacEmail);


                                    db.collection("UserProfiles") // Collection name is "facultyProfiles"
                                            .document(user.getUid()) // Document ID is the user's UID
                                            .set(UserProfiles)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("RegisterDebug", "Firestore set - onSuccessListener called"); // Log Firestore success
                                                // Data successfully stored in Firestore
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(verificationTask -> {
                                                            Log.d("RegisterDebug", "sendEmailVerification - onCompleteListener called"); // Log verification email listener
                                                            if (verificationTask.isSuccessful()) {
                                                                Log.d("RegisterDebug", "sendEmailVerification - task successful"); // Log verification email success
                                                                Toast.makeText(register.this, "Verification email sent. Please verify your email.", Toast.LENGTH_LONG).show();
                                                                Intent intent = new Intent(register.this, login.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                Log.w("RegisterDebug", "sendEmailVerification - task failed", verificationTask.getException()); // Log verification email failure
                                                                Toast.makeText(register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("RegisterDebug", "Firestore set - onFailureListener called", e); // Log Firestore failure with exception
                                                // Failed to store data in Firestore
                                                Toast.makeText(register.this, "Registration successful, but failed to save profile data.", Toast.LENGTH_LONG).show();
                                            });
                                }
                            } else {
                                Log.w("RegisterDebug", "createUserWithEmailAndPassword - task NOT successful", task.getException()); // Log auth failure with exception
                                Toast.makeText(register.this, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            Log.d("RegisterDebug", "registerButton onClick method finished"); // Log button click handler finished
        });
    }

    private void updatePasswordStrengthView(String password) {
        if (password.length() == 0) {
            passwordStrengthTextView.setText("");
            passwordStrengthProgressBar.setProgress(0);
            return;
        }

        PasswordStrength strength = PasswordStrength.calculateStrength(password);

        passwordStrengthTextView.setText(strength.getText(this));
        passwordStrengthTextView.setTextColor(ContextCompat.getColor(this, strength.getColor()));
        passwordStrengthProgressBar.setProgress(strength.getProgressPercentage());
        passwordStrengthProgressBar.setProgressTintList(ContextCompat.getColorStateList(this, strength.getColor()));

    }

    private boolean isValidPassword(String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public enum PasswordStrength {
        WEAK(R.string.weak, android.R.color.holo_red_dark, 25),
        MEDIUM(R.string.medium, android.R.color.holo_orange_dark, 50),
        STRONG(R.string.strong, android.R.color.holo_green_light, 75),
        VERY_STRONG(R.string.very_strong, android.R.color.holo_green_dark, 100);

        private int textId;
        private int colorId;
        private int progressPercentage;

        PasswordStrength(int textId, int colorId, int progressPercentage) {
            this.textId = textId;
            this.colorId = colorId;
            this.progressPercentage = progressPercentage;
        }

        public int getTextId() {
            return textId;
        }

        public int getColor() {
            return colorId;
        }

        public int getProgressPercentage() {
            return progressPercentage;
        }

        public String getText(register activity) {
            Log.d("PasswordStrengthEnumDebug", "getText() called, textId: " + textId + ", String from resources: " + activity.getString(textId));
            return activity.getString(textId);
        }

        public static PasswordStrength calculateStrength(String password) {
            Log.d("PasswordStrengthEnumDebug", "calculateStrength called for password: " + password);
            int score = 0;

            if (password.length() >= 8) score++;
            if (password.matches(".*[A-Z].*")) score++;
            if (password.matches(".*[0-9].*")) score++;
            if (password.matches(".*[@#$%^&+=!].*")) score++;

            if (score <= 1) {
                Log.d("PasswordStrengthEnumDebug", "Strength: WEAK, Score: " + score);
                return WEAK;
            }
            if (score == 2) {
                Log.d("PasswordStrengthEnumDebug", "Strength: MEDIUM, Score: " + score);
                return MEDIUM;
            }
            if (score == 3) {
                Log.d("PasswordStrengthEnumDebug", "Strength: STRONG, Score: " + score);
                return STRONG;
            }
            Log.d("PasswordStrengthEnumDebug", "Strength: VERY_STRONG, Score: " + score);
            return VERY_STRONG;

        }
    }
}