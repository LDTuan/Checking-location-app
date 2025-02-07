package com.example.datn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FillInformation extends AppCompatActivity {
    private Button btnNext;
    private EditText fullName;
    private EditText phoneNumber;
    private EditText address;
    private EditText citizen;
    private FirebaseFirestore db;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fill_information);

        db = FirebaseFirestore.getInstance();

        btnNext = findViewById(R.id.btnNext);
        fullName = findViewById(R.id.edFullName);
        phoneNumber = findViewById(R.id.edPhoneNumber);
        address = findViewById(R.id.edAddress);
        citizen = findViewById(R.id.edCitizen);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullname = fullName.getText().toString().trim();
                String phone = phoneNumber.getText().toString().trim();
                String adress = address.getText().toString().trim();
                String citizenNumber = citizen.getText().toString().trim();

                if (isValidInput()) {

                    Map<String, Object> user = new HashMap<>();
                    user.put("FullName", fullname);
                    user.put("PhoneNumber", phone);
                    user.put("Address", adress);
                    user.put("CitizenNumber", citizenNumber);


                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String uid = currentUser.getUid();

                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(FillInformation.this, "Information saved successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(FillInformation.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(FillInformation.this, "Save information failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(FillInformation.this, "User is not logged in", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(FillInformation.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

    private boolean isValidInput() {

        if (fullName.getText().toString().trim().isEmpty()) {
            fullName.setError("Name cannot be empty");
            fullName.requestFocus();
            return false;
        }

        String phone = phoneNumber.getText().toString().trim();
        if (phone.isEmpty()) {
            phoneNumber.setError("Phone number cannot be empty");
            phoneNumber.requestFocus();
            return false;

        } else if (!phone.matches("^0\\d{9}$")) {
            phoneNumber.setError("Invalid phone number");
            phoneNumber.requestFocus();
            return false;
        }

        if (address.getText().toString().trim().isEmpty()) {
            address.setError("Address cannot be empty");
            address.requestFocus();
            return false;
        }

        String citizenNumber = citizen.getText().toString().trim();
        if (citizenNumber.isEmpty()) {
            citizen.setError("Citizen ID cannot be empty");
            citizen.requestFocus();
            return false;

        } else if (!citizenNumber.matches("^\\d{9}$|^\\d{12}$")) {
            citizen.setError("Invalid Citizen ID");
            citizen.requestFocus();
            return false;
        }

        return true;
    }
}



