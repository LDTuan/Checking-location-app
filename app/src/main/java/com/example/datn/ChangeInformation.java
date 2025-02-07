package com.example.datn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ChangeInformation extends AppCompatActivity {
    private Button btnSave, btnCancel;
    private EditText fullName;
    private EditText phoneNumber;
    private EditText address;
    private EditText citizen;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_information);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        btnSave = findViewById(R.id.btnSave1);
        btnCancel = findViewById(R.id.btnCancel);
        fullName = findViewById(R.id.edFullName1);
        phoneNumber = findViewById(R.id.edPhoneNumber1);
        address = findViewById(R.id.edAddress1);
        citizen = findViewById(R.id.edCitizen1);


        loadUserInfo();

        // Cancel
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeInformation.this, Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        //Save
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValidInput()) {
                    updateUserInfo();
                }
            }
        });
    }

    // Check input
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

    // Load user with userID
    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Hiển thị thông tin hiện tại lên EditText
                                fullName.setText(documentSnapshot.getString("FullName"));
                                phoneNumber.setText(documentSnapshot.getString("PhoneNumber"));
                                address.setText(documentSnapshot.getString("Address"));
                                citizen.setText(documentSnapshot.getString("CitizenNumber"));
                            } else {
                                Toast.makeText(ChangeInformation.this, "User information not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChangeInformation.this, "Error getting user information" + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            Map<String, Object> updatedUser = new HashMap<>();
            updatedUser.put("FullName", fullName.getText().toString().trim());
            updatedUser.put("PhoneNumber", phoneNumber.getText().toString().trim());
            updatedUser.put("Address", address.getText().toString().trim());
            updatedUser.put("CitizenNumber", citizen.getText().toString().trim());


            db.collection("users").document(uid)
                    .update(updatedUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ChangeInformation.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ChangeInformation.this, Home.class);
                            // Đảm bảo không lưu lại activity hiện tại trong stack
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Đóng activity hiện tại sau khi chuyển hướng
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChangeInformation.this, "Update information failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}

