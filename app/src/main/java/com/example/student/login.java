package com.example.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class login extends AppCompatActivity {
    private EditText facultyIdEditText;
    private EditText facultyPasswordEditText;
    private Button facultyLoginButton;
    private Button facultyRegisterButton;
    private TextView forgotPasswordTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        facultyIdEditText = findViewById(R.id.StuIdEditText);
        facultyPasswordEditText = findViewById(R.id.StuPasswordEditText);
        facultyLoginButton = findViewById(R.id.StuLoginButton);
        facultyRegisterButton = findViewById(R.id.StuRegisterButton);
        forgotPasswordTextView = findViewById(R.id.forgotPassTextView);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        facultyLoginButton.setOnClickListener(v -> {
            String facultyEmail = facultyIdEditText.getText().toString().trim();
            String password = facultyPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(facultyEmail) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter Email and Password", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(facultyEmail, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && !user.isEmailVerified()) {
                                    // Email not verified
                                    showVerificationDialog(user);
                                } else {
                                    // Login successful or email already verified
                                    storeUserDataAndNavigate();
                                }
                            } else {
                                // Login failed
                                Toast.makeText(login.this, "Incorrect ID/Password", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        facultyRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, register.class);
            startActivity(intent);
        });
        forgotPasswordTextView.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });

    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password?");
        builder.setMessage("Enter your registered E-mail");

        final EditText emailInput = new EditText(this);
        emailInput.setHint("Your Email");
        builder.setView(emailInput);

        builder.setPositiveButton("Reset Password", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(login.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(login.this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(login.this, "Failed to send password reset email. Please check if you entered a registered email.", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showVerificationDialog(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Your email address has not been verified. Would you like to resend the verification email?");
        builder.setPositiveButton("Resend", (dialog, which) -> {
            user.sendEmailVerification()
                    .addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            Toast.makeText(login.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(login.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void storeUserDataAndNavigate(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference currentUserDb = mDatabase.child("users").child(user.getUid());
            currentUserDb.child("FacId").setValue(facultyIdEditText.getText().toString().trim());
            currentUserDb.child("Facname").setValue(""); // You can add code to get the name here if you want.
        }
        Intent intent = new Intent(login.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}