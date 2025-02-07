package com.example.datn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class MainActivity extends AppCompatActivity {
    private Button btnSignUp1;
    private EditText Username;
    private EditText passWord;
    private Button Login;
    private FirebaseAuth mAuth;
    // If you are logged in, go to the home screen
    @Override

    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        btnSignUp1 = findViewById(R.id.btnSignUp1);
        Username = findViewById(R.id.edUserName);
        passWord = findViewById(R.id.edPassWord);
        Login = findViewById(R.id.btnLOGIN);

        btnSignUp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Username.getText().toString().trim();
                String password = passWord.getText().toString().trim();
                // Kiểm tra hợp lệ dữ liệu nhập vào
                if (email.isEmpty()) {
                    Username.setError("Please enter your email!");
                    Username.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Username.setError("Invalid email!");
                    Username.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passWord.setError("Please enter your password!");
                    passWord.requestFocus();
                    return;
                }

                if (password.length() < 6) {
                    passWord.setError("Password must be at least 6 characters long!");
                    passWord.requestFocus();
                    return;
                }

                String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$";
                if (!password.matches(passwordPattern)) {
                    passWord.setError("Password must contain at least 1 letter, 1 number, and 1 special character!");
                    passWord.requestFocus();
                    return;
                }


                // Authenticate Login with Firebase Auth
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, Home.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed!";
                                   // Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                     if (errorMessage != null && errorMessage.contains("The supplied auth credential is incorrect, malformed or has expired")) {
                                         Toast.makeText(MainActivity.this, "Incorrect account", Toast.LENGTH_LONG).show();
                                     }
                                }
                            }
                        });
            }
        });
    }
}
