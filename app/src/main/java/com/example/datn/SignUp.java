package com.example.datn;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {
    private Button SignUp;
    private Button back;
    private FirebaseAuth mAuth;
    private EditText username;
    private EditText pass;
    private EditText rePass;
    private EditText OTPCode;
    private TextView SendOTP;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);


        mAuth = FirebaseAuth.getInstance();


        SendOTP = findViewById(R.id.txtSend);
        OTPCode = findViewById(R.id.edOTP);
        back = findViewById(R.id.btnBack);
        username = findViewById(R.id.edUserNameSU);
        pass = findViewById(R.id.edPassWordSU);
        rePass = findViewById(R.id.edRePassWord);
        SignUp = findViewById(R.id.btnSignUp);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = username.getText().toString().trim();
                String password = pass.getText().toString().trim();
                String rePassword = rePass.getText().toString().trim();

                // Validate input data
                if (email.isEmpty()) {
                    username.setError("Please enter your email!");
                    username.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    username.setError("Invalid email format");
                    username.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    pass.setError("Please enter your password");
                    pass.requestFocus();
                    return;
                }

                if (password.length() < 6) {
                    pass.setError("Password must be at least 6 characters long");
                    pass.requestFocus();
                    return;
                }

                String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$";
                if (!password.matches(passwordPattern)) {
                    pass.setError("Password must contain at least 1 letter, 1 number, and 1 special character");
                    pass.requestFocus();
                    return;
                }

                if (rePassword.isEmpty()) {
                    rePass.setError("Please confirm your password");
                    rePass.requestFocus();
                    return;
                }

                if (!password.equals(rePassword)) {
                    rePass.setError("Password confirmation does not match");
                    rePass.requestFocus();
                    return;
                }


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(SignUp.this, "Registration successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUp.this, FillInformation.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                                    Toast.makeText(SignUp.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}

