package com.example.autogarbagesortapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupActivity extends AppCompatActivity {

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private Button mSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mUsernameEditText = findViewById(R.id.signupUsernameEditText);
        mPasswordEditText = findViewById(R.id.signupPasswordEditText);
        mConfirmPasswordEditText = findViewById(R.id.signupConfirmPasswordEditText);
        mSignupButton = findViewById(R.id.signupButton);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String confirmPassword = mConfirmPasswordEditText.getText().toString();

                // Validate input
                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform signup logic
                if (performSignup(username, password)) {
                    // Signup successful
                    Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                    // Navigate to LoginActivity
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Signup failed
                    Toast.makeText(SignupActivity.this, "Signup failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean performSignup(String username, String password) {
        // Replace this with your actual signup logic
        // For example, check if the username is already taken, store the user in a database, etc.
        // For this example, we'll just save the user in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE);
        if (sharedPreferences.contains(username)) {
            // Username already exists
            return false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(username, password);
        editor.apply();
        return true;
    }

}